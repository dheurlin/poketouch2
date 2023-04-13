package xyz.heurlin.poketouch.emulator

import WasmBoy

class GameLoopInterceptor(private val wasmBoy: WasmBoy) {
    private var menuOption: Int? = null
    private val breakMan = BreakpointManager(wasmBoy).apply {
        clearPCBreakPoints()
        setPCBreakPoint(BreakpointManager.Address.StartBattle)
    }

    fun intercept() {
        when(breakMan.hitBreakPoint()) {
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
            }
            BreakpointManager.Address.BattleMenu_next -> {

            }
            BreakpointManager.Address.ListMoves -> {
                println("[GameLoopInterceptor]: Listing moves")
            }
            BreakpointManager.Address.MoveSelectionScreen_use_move_not_b -> {
                println("[GameLoopInterceptor]: Move used")
            }
            // TODO How to translate the following?
            // Maybe an optional callback for next break?
            // Nah, can probably just show dpad in "move used"
//            subState == SubState.BattleMoveChosen -> {
//                mainState = MainState.Battle
//                subState = SubState.BattleWaiting
//                menuOption = null
//            }
            else -> {}
        }
    }
}