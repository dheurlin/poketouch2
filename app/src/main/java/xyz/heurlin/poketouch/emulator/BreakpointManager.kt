package xyz.heurlin.poketouch.emulator

import WasmBoy

class BreakpointManager(private val wasmBoy: WasmBoy) {
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

    private var numUsed = 0
    private val breakpointsSet = mutableSetOf<Address>()

    fun setPCBreakPoint(addr: Address) {
        when (numUsed) {
            0 -> wasmBoy.setProgramCounterBreakpoint0(addr.offset)
            1 -> wasmBoy.setProgramCounterBreakpoint1(addr.offset)
            2 -> wasmBoy.setProgramCounterBreakpoint2(addr.offset)
            3 -> wasmBoy.setProgramCounterBreakpoint3(addr.offset)
            4 -> wasmBoy.setProgramCounterBreakpoint4(addr.offset)
            5 -> wasmBoy.setProgramCounterBreakpoint5(addr.offset)
            6 -> wasmBoy.setProgramCounterBreakpoint6(addr.offset)
            7 -> wasmBoy.setProgramCounterBreakpoint7(addr.offset)
            8 -> wasmBoy.setProgramCounterBreakpoint8(addr.offset)
            9 -> wasmBoy.setProgramCounterBreakpoint9(addr.offset)
            else -> throw IndexOutOfBoundsException("All breakpoints already used!")
        }
        breakpointsSet.add(addr)
        numUsed += 1
    }

    fun clearPCBreakPoints() {
        wasmBoy.setProgramCounterBreakpoint0(-1)
        wasmBoy.setProgramCounterBreakpoint1(-1)
        wasmBoy.setProgramCounterBreakpoint2(-1)
        wasmBoy.setProgramCounterBreakpoint3(-1)
        wasmBoy.setProgramCounterBreakpoint4(-1)
        wasmBoy.setProgramCounterBreakpoint5(-1)
        wasmBoy.setProgramCounterBreakpoint6(-1)
        wasmBoy.setProgramCounterBreakpoint7(-1)
        wasmBoy.setProgramCounterBreakpoint8(-1)
        wasmBoy.setProgramCounterBreakpoint9(-1)

        breakpointsSet.clear()
        numUsed = 0
    }

    private fun getRomBank(): Int {
        val offset = wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.hROMBank)
        return wasmBoy.memory.get(offset).toInt()
    }

    fun hitBreakPoint(): Address? {
        val currentPosition = Pair(getRomBank(), wasmBoy.programCounter)
        return addressMapping[currentPosition]
    }
}