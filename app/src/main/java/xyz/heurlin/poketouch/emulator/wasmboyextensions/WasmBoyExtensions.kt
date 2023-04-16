package xyz.heurlin.poketouch.emulator.wasmboyextensions

import WasmBoy
import xyz.heurlin.poketouch.emulator.Offsets

val WasmBoy.SwitchableCartridgeRomLocation
    get() = 0x4000

fun WasmBoy.getBytes(gameOffset: Int, numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val offset = getWasmBoyOffsetFromGameBoyOffset(gameOffset)
    memory.position(offset)
    memory.get(bytes)
    return bytes
}

fun WasmBoy.putByte(gameOffset: Int, byte: Byte) {
    memory.put(
        getWasmBoyOffsetFromGameBoyOffset(gameOffset),
        byte
    )
}

fun WasmBoy.getBytesFromBank(bank: Int, gbOffset: Int, numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val romBankOffset =
        (0x4000 * bank + (gbOffset - SwitchableCartridgeRomLocation));
    val offset = romBankOffset + cartridgE_ROM_LOCATION
    memory.position(offset)
    memory.get(bytes)
    return bytes
}
