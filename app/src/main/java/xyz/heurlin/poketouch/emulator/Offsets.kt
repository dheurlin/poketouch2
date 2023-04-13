package xyz.heurlin.poketouch.emulator

object Offsets {
    // In-game offsets
    const val wStringBuffer1 = 0xd073
    const val wListMoves_MoveIndicesBuffer = 0xd25e
    const val wCurMoveNum = 0xd0d5
    const val wMenuCursorY = 0xcfa9
    const val wBattleMenuCursorPosition = 0xd0d2
    const val hROMBank = 0xff9d
    const val MoveNames = 0x5f29
    const val MoveNameLength = 13
    const val NumMoves = 251
    // WasmBoy offsets
    const val WasmBoySwitchableCartridgeRomLocation = 0x4000
}