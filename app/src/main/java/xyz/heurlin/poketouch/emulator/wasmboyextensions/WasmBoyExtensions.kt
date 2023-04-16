package xyz.heurlin.poketouch.emulator.wasmboyextensions

import WasmBoy

val WasmBoy.SwitchableCartridgeRomLocation
    get() = 0x4000

fun WasmBoy.getBytes(gameOffset: Int, numBytes: Int): ByteArray {
    val bytes = ByteArray(numBytes)
    val offset = getWasmBoyOffsetFromGameBoyOffset(gameOffset)
    memory.position(offset)
    memory.get(bytes)
    return bytes
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
