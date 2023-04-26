package xyz.heurlin.poketouch.emulator.libretro

import java.nio.ByteBuffer

interface IScreenView {
    fun videoRefresh(buffer: ByteBuffer, width: Int, height: Int, pitch: Long)
    fun videoRender()
}