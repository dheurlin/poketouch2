package xyz.heurlin.poketouch.emulator.libretro

import java.io.InputStream

class GambatteFrontend {
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

    fun loadRom(rom: InputStream) {
        val bytes = rom.buffered().use {
            it.readBytes()
        }
        val res = coreLoadGame(bytes);
        println("Load game results: $res");
    }

    fun readRomBytes(bank: Int, gameAddress: Int, numBytes: Int): ByteArray {
        val dest = ByteArray(numBytes)
        readRomBytes_(bank.toByte(), gameAddress, dest);
        return dest;
    }
}