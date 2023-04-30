package xyz.heurlin.poketouch.emulator.libretro

import android.content.Context
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Environment
import xyz.heurlin.poketouch.*
import xyz.heurlin.poketouch.emulator.GameLoopInterceptor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread

class GambatteFrontend(
    private val context: Context,
    private val bridge: ILibretroExtended,
    private val screenView: IScreenView,
    private val controllerState: ControllerState,
    updateControllerMode: (ControllerMode) -> Unit,
    updateControllerState: (ControllerAction) -> Unit,
) {
    private val audio = AudioTrack.Builder()
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(32768)
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
        )
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()
        .apply {
            play()
        }

    private val interceptor = GameLoopInterceptor(bridge, updateControllerMode, updateControllerState)

    var backPressed = false

    init {
        bridge.setAudioCb(::audioBatchCallback)
        bridge.setVideoCb(::videoRefreshCallback)
        bridge.retroInit()
    }

    private fun getSaveState(): ByteArray {
        val dest = ByteArray(bridge.serializeSize().toInt())
        val res = bridge.serializeState(dest)
        if (!res) {
            throw Error("Failed to generate savestate: core returned error")
        }
        return dest
    }

    fun saveState() {
        val state = getSaveState()
        val path = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/PokeTouch2"
        val fileName = "crystal.state"
        try {
            Files.createDirectories(Paths.get(path))
            val file = File(path, fileName)
            if (file.isFile) {
                // TODO Support inf states?
                println("Deleting existing saveState...")
                file.delete()
            }
            FileOutputStream(file).use {
                it.write(state)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadState() {
        val path = "${context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)}/PokeTouch2"
        val fileName = "crystal.state"
        try {
            val file = File(path, fileName)
            if (!file.isFile) {
                println("Save state not created...")
                return
            }
            val data = ByteArray(bridge.serializeSize().toInt())
            FileInputStream(file).use {
                it.read(data)
            }
            bridge.deserializeState(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setJoypadInput() {
        val b = if (backPressed) {
            backPressed = false
            true
        } else {
            false
        }
        bridge.setInput(
            a = controllerState.buttonsPressed[Button.A] == true,
            b = b,
            start = controllerState.buttonsPressed[Button.Start] == true,
            select = controllerState.buttonsPressed[Button.Select] == true,
            up = controllerState.direction == DpadDirection.Up,
            down = controllerState.direction == DpadDirection.Down,
            left = controllerState.direction == DpadDirection.Left,
            right = controllerState.direction == DpadDirection.Right,
        )
    }

    private fun audioBatchCallback(data: ByteBuffer, frames: Long): Long {
        // TODO Popping when audio goes to zero; implement slight fade-out to prevent?
        return audio.write(data, frames.toInt() * 2 * 2, AudioTrack.WRITE_BLOCKING).toLong()
    }

    private fun videoRefreshCallback(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        return screenView.videoRefresh(buffer, width, height, pitch)
    }

    fun run(): Thread {
        return thread {
            while (true) {
                val result = bridge.retroRun()
                if (result == bridge.BREAKPOINT_HIT) {
                    interceptor.intercept()
                }
                screenView.videoRender()
                setJoypadInput()
            }
        }
    }

    fun loadRom(rom: InputStream) {
        val bytes = rom.buffered().use {
            it.readBytes()
        }
        val res = bridge.coreLoadGame(bytes);
        println("Load game results: $res");
        interceptor.setInitialBreakpoints()
    }
}