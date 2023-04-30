package xyz.heurlin.poketouch.emulator

import WasmBoy
import xyz.heurlin.poketouch.emulator.libretro.ILibretroExtensionBridge

class BreakpointManager(private val ext: ILibretroExtensionBridge) {
    enum class Address(val bank: Int, val offset: Int) {
        StartBattle(0x0f, 0x74c1),
        ListMoves(0x14, 0x4d6f),
        ExitBattle(0x0f, 0x769e),
        Load2DMenuData(0x00, 0x1bb1),
        BattleMenu(0x0f, 0x6139),
        BattleMenu_next(0x0f, 0x6175),
        LoadBattleMenu(0x09, 0x4ef2),
        MoveSelectionScreen(0x0f, 0x64bc),
        MoveSelectionScreen_use_move(0x0f, 0x65d9),
        MoveSelectionScreen_use_move_not_b(0x0f, MoveSelectionScreen_use_move.offset + 2),
        MoveSelectionScreen_battle_player_moves(0x0f, 0x658e),
        ListMoves_moves_loop(0x14, 0x4d74)
    }

    private val addressMapping =
        Address.values().associateBy { address -> Pair(address.bank, address.offset) }

    fun setPCBreakPoint(addr: Address) {
        ext.setPCBreakpoint(addr.bank.toByte(), addr.offset)
    }

    fun clearPCBreakPoints() {
        ext.clearPCBreakpoints()
    }

    private fun getRomBank(): Int {
        return ext.readZeropage(Offsets.hROMBank, 1)[0].toUByte().toInt()
    }

    fun hitBreakPoint(): Address? {
        val currentPosition = Pair(getRomBank(), ext.getProgramCounter())
        return addressMapping[currentPosition]
    }
}