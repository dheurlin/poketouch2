package xyz.heurlin.poketouch.emulator.libretro

import java.nio.ByteBuffer

class LibretroBridge() : ILibretroBridge, ILibretroExtensionBridge {

    private var videoCb: (buffer: ByteBuffer, width: Int, height: Int, pitch: Long) -> Unit =
        { _: ByteBuffer, _: Int, _: Int, _: Long ->
            println("Video callback not set!")
        }

    private var audioCb: (data: ByteBuffer, frames: Long) -> Long = { _: ByteBuffer, _: Long ->
        println("Audio callback not set!")
        0
    }

    companion object {
        init {
            System.loadLibrary("poketouch")
        }
    }

    external override fun retroInit(): Unit
    external override fun coreLoadGame(bytes: ByteArray): Boolean
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
    external override fun setPCBreakpoint(bank: Byte, offset: Int)

    external override fun clearPCBreakpoints()
    external override fun getProgramCounter(): Int

    external fun readZeropageInternal(address: Byte, dest: ByteArray)
    external fun readWramInternal(bank: Byte, address: Int, dest: ByteArray)

    external override fun writeWramByte(bank: Byte, address: Int, byte: Byte)
    external fun readRomInternal(bank: Byte, address: Int, dest: ByteArray)

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

    override fun readWram(bank: Byte, address: Int, numBytes: Int): ByteArray {
        val dest = ByteArray(numBytes)
        readWramInternal(bank, address, dest)
        return dest
    }

    override fun readRom(bank: Byte, address: Int, numBytes: Int): ByteArray {
        val dest = ByteArray(numBytes)
        readRomInternal(bank, address, dest)
        return dest
    }

    override fun readZeropage(address: Byte, numBytes: Int): ByteArray {
        val dest = ByteArray(numBytes)
        readZeropageInternal(address, dest)
        return dest
    }

}