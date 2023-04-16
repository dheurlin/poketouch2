package xyz.heurlin.poketouch.emulator

import WasmBoy
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerAction
import xyz.heurlin.poketouch.ControllerMode
import xyz.heurlin.poketouch.components.MoveButtonInput
import xyz.heurlin.poketouch.emulator.wasmboyextensions.*
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

                wasmBoy.putByte(Offsets.wBattleMenuCursorPosition, menuOption?.toByte() ?: 0)
            }
            BreakpointManager.Address.ListMoves -> {
                println("[GameLoopInterceptor]: Listing moves")
                updateControllerState(ControllerAction.ReleaseAll)

                val monStruct = getCurrentPokemonStruct()
                val moveNums = wasmBoy.getBytes(Offsets.wListMoves_MoveIndicesBuffer, 4)

                val moveNames = getMoveNames(moveNums)
                val movePPs = monStruct.slice(23..23 + 3)
                val moveTypes = moveNums.map {
                    val moveNum = getMoveStruct(it.toInt())[3]
                    getMoveStruct(it.toInt()).forEach {
                        println(it.toUByte())
                    }
//                    PokemonType.fromNumber(moveNum.toInt())
                }

                val moves: List<PokemonMove> = moveNames.mapIndexed { ix, it ->
                    PokemonMove(
                        name = it,
                        pp = MovePP(10, movePPs[ix].toInt()),
//                        type = moveTypes[ix],
                        type = PokemonType.Normal
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
                wasmBoy.putByte(Offsets.wMenuCursorY, menuOption?.toByte() ?: 0)
                wasmBoy.putByte(Offsets.wCurMoveNum, menuOption?.toByte() ?: 0)
            }
            else -> {}
        }
    }

    private fun getMoveNames(bs: ByteArray): List<String> {
        val bytes = wasmBoy.getBytesFromBank(
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

    private fun getCurrentPokemonStruct(): ByteArray {
        val currentMon = wasmBoy.getBytes(Offsets.wCurPartyMon, 1)[0]
        val monLocation = Offsets.wPartyMons + currentMon.toInt() * Offsets.PartyMonStructSize
        return wasmBoy.getBytes(monLocation, Offsets.PartyMonStructSize)
    }

    private fun getMoveStruct(moveIndex: Int): ByteArray {
        val moveOffset = moveIndex * Offsets.MoveStructLength
        return wasmBoy.getBytesFromBank(
            Offsets.RomBankMoves,
            Offsets.MovesTable + moveOffset,
            Offsets.MoveStructLength
        )
    }

    /*
    MACRO move
	db \1 ; animation
	db \2 ; effect
	db \3 ; power
	db \4 ; type
	db \5 percent ; accuracy
	db \6 ; pp
	db \7 percent ; effect chance
	assert \6 <= 40, "PP must be 40 or less"
ENDM

     */
}