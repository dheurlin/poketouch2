package xyz.heurlin.poketouch.emulator

import WasmBoy
import android.app.Activity
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerState
import xyz.heurlin.poketouch.DpadDirection
import xyz.heurlin.poketouch.components.ScreenView
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class Emulator(
    rom: InputStream,
    private val screen: ScreenView,
    private val controllerState: ControllerState,
    private val activity: Activity
) {
    private val wasmBoy: WasmBoy = WasmBoy(ByteBuffer.allocate(20_000_000), null)
    private val AUDIO_BUF_TARGET_SIZE = 2 * 4096
    private var audioBufLen = 0

    var running = false
    private var shouldLoadState = false
    private var shouldSaveState = false
    var backPressed = false

    private lateinit var audio: AudioTrack

    private val interceptor = GameLoopInterceptor(wasmBoy)

    init {
        loadRom(rom)
        configure()
        initAudio()
    }

    private fun loadRom(rom: InputStream) {
        rom.readBytes().forEachIndexed { index, byte ->
            if (byte != 0.toByte()) {
                wasmBoy.memory.put(wasmBoy.cartridgE_ROM_LOCATION + index, byte)
            }
        }
    }

    private fun configure() {
        wasmBoy.config(
            0, // enableBootRom
            1, // preferGbc
            1, // audioBatchProcessing
            0, // graphicsBatchProcessing
            1, // timersBatchProcessing
            0, // graphicsDisableScanlineRendering
            1, // audioAccumulateSample
            1, // tileRendering
            1, // tileCaching
            0, // ?? (why 9 params???)
        )
    }

    private fun initAudio() {
        val bufsize = AudioTrack.getMinBufferSize(
            44100,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_8BIT
        )
        audio = AudioTrack(
            AudioManager.STREAM_MUSIC,
            44100,  //sample rate
            AudioFormat.CHANNEL_OUT_STEREO,  //2 channel
            AudioFormat.ENCODING_PCM_8BIT,  // 16-bit
            bufsize,
            AudioTrack.MODE_STREAM
        )
        audio.play()
    }

    // TODO Kan man göra denna asynkron på nåt sätt? För turbo speed typ
    private fun playAudio() {
        audioBufLen = wasmBoy.numberOfSamplesInAudioBuffer * 2
        val audioArray = ByteArray(audioBufLen)
        wasmBoy.memory.position(wasmBoy.audiO_BUFFER_LOCATION)
        wasmBoy.memory.get(audioArray)
        audio.write(audioArray, 0, audioBufLen)
        wasmBoy.clearAudioBuffer()
    }

    private fun setJoypadInput() {
        val b = if (backPressed) {
            backPressed = false
            1
        } else {
            0
        }
        wasmBoy.setJoypadState(
            if (controllerState.direction == DpadDirection.Up) 1 else 0,
            if (controllerState.direction == DpadDirection.Right) 1 else 0,
            if (controllerState.direction == DpadDirection.Down) 1 else 0,
            if (controllerState.direction == DpadDirection.Left) 1 else 0,

            if (controllerState.buttonsPressed[Button.A] == true) 1 else 0,
            b, // B
            0, // SELECT
            if (controllerState.buttonsPressed[Button.Start] == true) 1 else 0,
        )
    }

    // TODO Save/load sometimes crashes, something to do with multiple savestates in wasmboy ??

    fun loadState() {
        shouldLoadState = true
    }

    fun saveState() {
        shouldSaveState = true
    }

    private fun _saveState() {
        wasmBoy.saveState()

        val wasmState = ByteArray(wasmBoy.wasmboY_STATE_SIZE)
        wasmBoy.memory.position(wasmBoy.wasmboY_STATE_LOCATION)
        wasmBoy.memory.get(wasmState)

        val gbInternalMemory = ByteArray(wasmBoy.gameboY_INTERNAL_MEMORY_SIZE)
        wasmBoy.memory.position(wasmBoy.gameboY_INTERNAL_MEMORY_LOCATION)
        wasmBoy.memory.get(gbInternalMemory)

        val cartridgeRam = ByteArray(wasmBoy.cartridgE_RAM_SIZE)
        wasmBoy.memory.position(wasmBoy.cartridgE_RAM_LOCATION)
        wasmBoy.memory.get(cartridgeRam)

        val gbcPalette = ByteArray(wasmBoy.gbC_PALETTE_SIZE)
        wasmBoy.memory.position(wasmBoy.gbC_PALETTE_LOCATION)
        wasmBoy.memory.get(gbcPalette)

        val file = File(activity.applicationContext.filesDir, "state")
        if (file.isFile) {
            // TODO Support inf states?
            println("Deleting existing saveState...")
            file.delete()
        }
        FileOutputStream(file).use {
            it.write(wasmState)
            it.write(gbInternalMemory)
            it.write(cartridgeRam)
            it.write(gbcPalette)
        }

        shouldSaveState = false
    }


    private fun _loadState() {
        val file = File(activity.applicationContext.filesDir, "state")
        if (!file.isFile) {
            println("Save state not created...")
            shouldLoadState = false
            return
        }
        val wasmState = ByteArray(wasmBoy.wasmboY_STATE_SIZE)
        val gbInternalMemory = ByteArray(wasmBoy.gameboY_INTERNAL_MEMORY_SIZE)
        val cartridgeRam = ByteArray(wasmBoy.cartridgE_RAM_SIZE)
        val gbcPalette = ByteArray(wasmBoy.gbC_PALETTE_SIZE)

        FileInputStream(file).use {
            it.read(wasmState)
            it.read(gbInternalMemory)
            it.read(cartridgeRam)
            it.read(gbcPalette)
        }

        wasmBoy.memory.position(wasmBoy.wasmboY_STATE_LOCATION)
        wasmBoy.memory.put(wasmState)
        wasmBoy.memory.position(wasmBoy.gameboY_INTERNAL_MEMORY_LOCATION)
        wasmBoy.memory.put(gbInternalMemory)
        wasmBoy.memory.position(wasmBoy.cartridgE_RAM_LOCATION)
        wasmBoy.memory.put(cartridgeRam)
        wasmBoy.memory.position(wasmBoy.gbC_PALETTE_LOCATION)
        wasmBoy.memory.put(gbcPalette)

        wasmBoy.loadState()

        shouldLoadState = false
    }

    enum class Response(val code: Int) {
       BREAKPOINT(2),
       OK(-1)
    }

    fun start() {
        running = true

        thread {
            while (true) {
                if (!running) {
                    Thread.sleep(100)
                    continue
                }
                screen.drawScreen()
            }
        }

        thread {
            while (true) {
                if (wasmBoy.numberOfSamplesInAudioBuffer > 6000) continue;
                if (!running) {
                    Thread.sleep(100)
                    continue
                }

                val response = wasmBoy.executeFrame()

                if (shouldLoadState) _loadState()
                if (shouldSaveState) _saveState()

                if (response == Response.BREAKPOINT.code) {
                    interceptor.intercept()
                }
                if (response > Response.OK.code) {
                    screen.getPixelsFromEmulator(wasmBoy)
                    playAudio()
                    setJoypadInput()
                } else {
                    println("[EMULATOR Main loop] Invalid response received: $response")
                }

                // Sleep a bit depending on how full the audio buffer is
                val audioBufFill = audioBufLen.toFloat() / AUDIO_BUF_TARGET_SIZE.toFloat()
                Thread.sleep(((1000 / 60) * audioBufFill).toLong())
            }
        }
    }
}