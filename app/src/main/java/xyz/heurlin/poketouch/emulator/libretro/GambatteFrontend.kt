package xyz.heurlin.poketouch.emulator.libretro

import android.media.AudioFormat
import android.media.AudioTrack
import java.io.InputStream
import java.nio.ByteBuffer
import kotlin.concurrent.thread

class GambatteFrontend(private val screenView: IScreenView) {
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

    private external fun retroRun(): Int;

    private fun audioBatchCallback(data: ByteBuffer, frames: Long): Long {
        // TODO Popping when audio goes to zero; implement slight fade-out to prevent?
        return audio.write(data, frames.toInt() * 2 * 2, AudioTrack.WRITE_BLOCKING).toLong()
    }

    private fun videoRefreshCallback(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        return screenView.videoRefresh(buffer, width, height, pitch)
    }

    fun run(): Thread {
        return thread {
           while (true)  {
               retroRun()
               screenView.videoRender()
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