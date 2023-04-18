package xyz.heurlin.poketouch.emulator

import WasmBoy
import xyz.heurlin.poketouch.Button
import xyz.heurlin.poketouch.ControllerAction
import xyz.heurlin.poketouch.ControllerMode
import xyz.heurlin.poketouch.ControllerState
import xyz.heurlin.poketouch.components.MoveButtonInput
import xyz.heurlin.poketouch.emulator.wasmboyextensions.*
import xyz.heurlin.poketouch.types.*
import kotlin.math.max

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
                updateControllerMode(ControllerMode.Dpad(shouldRotate = true))
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
                        updateControllerState(ControllerAction.ReleaseAll)
                        updateControllerState(ControllerAction.ButtonPress(Button.A))
                    }
                }
                updateControllerMode(ControllerMode.ActionSelection(actions))
            }

            BreakpointManager.Address.BattleMenu_next -> {
                println("[GameLoopInterceptor]: Action chosen")

                // Unless we chose "fight"
                if (menuOption != 1) {
                    updateControllerMode(ControllerMode.Dpad())
                }

                menuOption?.let {
                    wasmBoy.putByte(Offsets.wBattleMenuCursorPosition, it.toByte())
                }
            }

            BreakpointManager.Address.ListMoves -> {
                println("[GameLoopInterceptor]: Listing moves")
                updateControllerState(ControllerAction.ReleaseAll)

                val monStruct = getCurrentPokemonStruct()

                val moveNums = wasmBoy.getBytes(Offsets.wListMoves_MoveIndicesBuffer, 4)
                val moveNames = getMoveNames(moveNums)
                val movesData = moveNums.takeWhile { it != 0.toByte() }.map {
                    getMoveStruct(it.toUByte().toInt())
                }

                val moves: List<PokemonMove> = moveNames.mapIndexed { ix, it ->
                    PokemonMove(
                        name = it,
                        // TODO this does not take into account PP Ups, that would require additional calculation
                        pp = MovePP(movesData[ix].basePP, monStruct.currentPPs[ix]),
                        type = movesData[ix].type,
                    )
                }
                val actions = List(moves.size) { {
                    menuOption = it
                    updateControllerState(ControllerAction.ReleaseAll)
                    updateControllerState(ControllerAction.ButtonPress(Button.A))
                } }

                val moveInputs: List<MoveButtonInput> = moves.zip(actions) { move, action ->
                    MoveButtonInput.Enabled(move, action)
                } + List(4 - moveNames.size) {
                    MoveButtonInput.Disabled
                }

                updateControllerMode(ControllerMode.MoveSelection(moveInputs))
            }

            BreakpointManager.Address.MoveSelectionScreen_use_move_not_b -> {
                println("[GameLoopInterceptor]: Move used")
                updateControllerState(ControllerAction.ReleaseAll)
                updateControllerMode(ControllerMode.Dpad())
                menuOption?.let {
                    wasmBoy.putByte(Offsets.wMenuCursorY, it.toByte())
                    wasmBoy.putByte(Offsets.wCurMoveNum, it.toByte())
                }
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
            val i = it.toUByte().toInt()
            if (i > 0) allStrings[i - 1] else null
        }.filterNotNull()
    }

    private fun getCurrentPokemonStruct(): PartyMonStruct {
        val currentMon = wasmBoy.getBytes(Offsets.wCurPartyMon, 1)[0]
        val monLocation = Offsets.wPartyMons + currentMon.toInt() * Offsets.PartyMonStructSize
        val bytes = wasmBoy.getBytes(monLocation, Offsets.PartyMonStructSize)

        return PartyMonStruct(bytes)
    }

    private fun getMoveStruct(moveIndex: Int): MoveStruct {
        val index = max(0, moveIndex - 1)
        val moveOffset = index * Offsets.MoveStructLength
        val bytes = wasmBoy.getBytesFromBank(
            Offsets.RomBankMoves,
            Offsets.MovesTable + moveOffset,
            Offsets.MoveStructLength
        )

        return MoveStruct(bytes)
    }

}