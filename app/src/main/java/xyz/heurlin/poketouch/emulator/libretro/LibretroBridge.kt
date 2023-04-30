package xyz.heurlin.poketouch.emulator.libretro

import android.content.Context
import android.media.AudioTrack
import xyz.heurlin.poketouch.ControllerState
import java.nio.ByteBuffer

class LibretroBridge() : ILibretroBridge {

    private var videoCb: (buffer: ByteBuffer, width: Int, height: Int, pitch: Long) -> Unit =
        { _: ByteBuffer, _: Int, _: Int, _: Long ->
            println("Video callbak not set!")
        }

    private var audioCb: (data: ByteBuffer, frames: Long) -> Long = { _: ByteBuffer, _: Long ->
        println("Audio callbak not set!")
        0
    }

    companion object {
        init {
            System.loadLibrary("poketouch")
        }
    }

    external override fun retroInit(): Unit
    external override fun coreLoadGame(bytes: ByteArray): Boolean
    private external fun readRomBytes_(bank: Byte, gameAddress: Int, dest: ByteArray)
    external override fun setInput(
        a: Boolean,
        b: Boolean,
        start: Boolean,
        select: Boolean,
        up: Boolean,
        down: Boolean,
        left: Boolean,
        right: Boolean
    )

    external override fun retroRun(): Int;
    external override fun serializeSize(): Long
    external override fun serializeState(dest: ByteArray): Boolean
    external override fun deserializeState(data: ByteArray): Boolean

    override fun setVideoCb(cb: (buffer: ByteBuffer, width: Int, height: Int, pitch: Long) -> Unit) {
        videoCb = cb
    }

    override fun setAudioCb(cb: (data: ByteBuffer, frames: Long) -> Long) {
        audioCb = cb
    }

    private fun audioBatchCallback(data: ByteBuffer, frames: Long): Long {
        return audioCb(data, frames)
    }

    private fun videoRefreshCallback(buffer: ByteBuffer, width: Int, height: Int, pitch: Long) {
        return videoCb(buffer, width, height, pitch)
    }

}