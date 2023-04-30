package xyz.heurlin.poketouch.emulator.libretro

import java.nio.ByteBuffer

interface ILibretroBridge {
    fun retroInit()
    fun retroRun(): Int
    fun coreLoadGame(bytes: ByteArray): Boolean
    fun setInput(
        a: Boolean,
        b: Boolean,
        start: Boolean,
        select: Boolean,
        up: Boolean,
        down: Boolean,
        left: Boolean,
        right: Boolean
    )
    fun serializeSize(): Long
    fun serializeState(dest: ByteArray): Boolean
    fun deserializeState(data: ByteArray): Boolean
    fun setVideoCb(cb: (buffer: ByteBuffer, width: Int, height: Int, pitch: Long) -> Unit)
    fun setAudioCb(cb: (data: ByteBuffer, frames: Long) -> Long)
}