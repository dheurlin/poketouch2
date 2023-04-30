package xyz.heurlin.poketouch.emulator.libretro

interface ILibretroExtensionBridge {
    fun setPCBreakpoint(bank: Byte, offset: Int)
    fun clearPCBreakpoints()
    fun getProgramCounter(): Int
    fun readZeropage(address: Int, numBytes: Int): ByteArray
    fun readWram(bank: Byte, address: Int, numBytes: Int): ByteArray
    fun writeWramByte(bank: Byte, address: Int, byte: Byte)
    fun readRom(bank: Byte, address: Int, numBytes: Int): ByteArray

    val BREAKPOINT_HIT: Int
        get() = -2
}

interface ILibretroExtended : ILibretroExtensionBridge, ILibretroBridge