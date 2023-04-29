package xyz.heurlin.poketouch.emulator.libretro

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultCaller.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.compose.ui.graphics.Path
import androidx.core.content.ContextCompat
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerState
import xyz.heurlin.poketouch.DpadDirection
import xyz.heurlin.poketouch.components.findActivity
import xyz.heurlin.poketouch.util.withPermission
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.Manifest
import kotlin.concurrent.thread

class GambatteFrontend(
    private val context: Context,
    private val screenView: IScreenView,
    private val controllerState: ControllerState,
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

    var backPressed = false

    companion object {
        init {
            System.loadLibrary("poketouch")
        }
    }

    init {
        retroInit()
    }

    private external fun retroInit(): Unit
    private external fun coreLoadGame(bytes: ByteArray): Boolean
    private external fun readRomBytes_(bank: Byte, gameAddress: Int, dest: ByteArray)

    private external fun setInput(
        a: Boolean,
        b: Boolean,
        start: Boolean,
        select: Boolean,
        up: Boolean,
        down: Boolean,
        left: Boolean,
        right: Boolean
    )

    private external fun retroRun(): Int;

    private external fun serializeSize(): Long

    private external fun serializeState(dest: ByteArray): Boolean

    private external fun deserializeState(data: ByteArray): Boolean

    private fun getSaveState(): ByteArray {
        val dest = ByteArray(serializeSize().toInt())
        val res = serializeState(dest)
        if (!res) {
            throw Error("Failed to generate savestate: core returned error")
        }
        return dest
    }

    private fun audioBatchCallback(data: ByteBuffer, frames: Long): Long {
        // TODO Popping when audio goes to zero; implement slight fade-out to prevent?
        return audio.write(data, frames.toInt() * 2 * 2, AudioTrack.WRITE_BLOCKING).toLong()
    }

    private fun videoRefreshCallback(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        return screenView.videoRefresh(buffer, width, height, pitch)
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
            val data = ByteArray(serializeSize().toInt())
            FileInputStream(file).use {
                it.read(data)
            }
            deserializeState(data)
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
        setInput(
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

    fun run(): Thread {
        return thread {
            while (true) {
                retroRun()
                screenView.videoRender()
                setJoypadInput()
            }
        }
    }

    fun loadRom(rom: InputStream) {
        val bytes = rom.buffered().use {
            it.readBytes()
        }
        val res = coreLoadGame(bytes);
        println("Load game results: $res");
    }
}