package xyz.heurlin.poketouch.emulator

import WasmBoy
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerAction
import xyz.heurlin.poketouch.ControllerMode
import xyz.heurlin.poketouch.components.MoveButtonInput
import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType

class GameLoopInterceptor(
    private val wasmBoy: WasmBoy,
    private val updateControllerMode: (ControllerMode) -> Unit,
    private val updateControllerState: (ControllerAction) -> Unit,
) {
    private var menuOption: Int? = null
    private val breakMan = BreakpointManager(wasmBoy).apply {
        clearPCBreakPoints()
        setPCBreakPoint(BreakpointManager.Address.StartBattle)
    }

    fun intercept() {
        when (breakMan.hitBreakPoint()) {
            BreakpointManager.Address.StartBattle -> {
                println("[GameLoopInterceptor]: Battle starting!")
                breakMan.run {
                    clearPCBreakPoints()
                    setPCBreakPoint(BreakpointManager.Address.ExitBattle)
                    setPCBreakPoint(BreakpointManager.Address.BattleMenu)
                    setPCBreakPoint(BreakpointManager.Address.BattleMenu_next)
                    setPCBreakPoint(BreakpointManager.Address.ListMoves)
                    setPCBreakPoint(BreakpointManager.Address.MoveSelectionScreen_use_move_not_b)
                }
            }
            BreakpointManager.Address.ExitBattle -> {
                println("[GameLoopInterceptor]: Exiting battle")
                breakMan.run {
                    clearPCBreakPoints()
                    setPCBreakPoint(BreakpointManager.Address.StartBattle)
                }
            }
            BreakpointManager.Address.BattleMenu -> {
                println("[GameLoopInterceptor]: Opening battle menu")
                updateControllerState(ControllerAction.ReleaseAll)
                val actions = List(4) { ix ->
                    {
                        menuOption = ix + 1
                        updateControllerState(ControllerAction.ButtonPress(Button.A))
                    }
                }
                updateControllerMode(ControllerMode.ActionSelection(actions))
            }
            BreakpointManager.Address.BattleMenu_next -> {
                println("[GameLoopInterceptor]: Action chosen")

                // Unless we chose "fight"
                if (menuOption != 1) {
                    updateControllerMode(ControllerMode.Dpad)
                }

                wasmBoy.memory.put(
                    wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.wBattleMenuCursorPosition),
                    menuOption?.toByte()
                        ?: throw IllegalStateException("[GameLoopInterceptor] Action chosen: MenuOption is null!")
                )
            }
            BreakpointManager.Address.ListMoves -> {
                println("[GameLoopInterceptor]: Listing moves")
                updateControllerState(ControllerAction.ReleaseAll)

                val moveNums = getBytes(Offsets.wListMoves_MoveIndicesBuffer, 4)
                val moveNames = getMoveNames(moveNums)
                val moves: List<PokemonMove> = moveNames.map {
                    PokemonMove(
                        name = it,
                        // TODO Read these from memory
                        pp = MovePP(10, 10),
                        type = PokemonType.Normal,
                    )
                }
                val actions = List(moves.size) {
                    {
                        menuOption = it
                        updateControllerState(ControllerAction.ButtonPress(Button.A))
                    }
                }

                val moveInputs: List<MoveButtonInput> = moves.zip(actions) { move, action ->
                   MoveButtonInput.Enabled(move, action)
                } + List(4 - moveNames.size) { MoveButtonInput.Disabled }
                updateControllerMode(ControllerMode.MoveSelection(moveInputs))
            }
            BreakpointManager.Address.MoveSelectionScreen_use_move_not_b -> {
                println("[GameLoopInterceptor]: Move used")
                updateControllerState(ControllerAction.ReleaseAll)
                updateControllerMode(ControllerMode.Dpad)
                wasmBoy.memory.put(
                    wasmBoy.getWasmBoyOffsetFromGameBoyOffset(Offsets.wMenuCursorY),
                    menuOption?.toByte() ?: 0
                )
            }
            else -> {}
        }
    }


    private fun getBytes(gameOffset: Int, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        val offset = wasmBoy.getWasmBoyOffsetFromGameBoyOffset(gameOffset)
        wasmBoy.memory.position(offset)
        wasmBoy.memory.get(bytes)
        return bytes
    }

    private fun getBytesFromBank(bank: Int, gbOffset: Int, numBytes: Int): ByteArray {
        val bytes = ByteArray(numBytes)
        val romBankOffset =
            (0x4000 * bank + (gbOffset - Offsets.WasmBoySwitchableCartridgeRomLocation));
        val offset = romBankOffset + wasmBoy.cartridgE_ROM_LOCATION
        wasmBoy.memory.position(offset)
        wasmBoy.memory.get(bytes)
        return bytes
    }

    private fun getMoveNames(bs: ByteArray): List<String> {
        val bytes = getBytesFromBank(
            Offsets.RomBankNames,
            Offsets.MoveNames,
            Offsets.MoveNameLength * Offsets.NumMoves
        )

        val allStrings = Charmap.bytesToString(bytes).split("@")
        return bs.map {
            val i = it.toInt()
            if (i > 0) allStrings[it.toInt() - 1] else null
        }.filterNotNull()
    }
}