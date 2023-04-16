package xyz.heurlin.poketouch.exampleData

import xyz.heurlin.poketouch.types.MovePP
import xyz.heurlin.poketouch.types.PokemonMove
import xyz.heurlin.poketouch.types.PokemonType

object ExampleMoves {
    val moves = listOf(
        PokemonMove("Tackle", MovePP(10, 10), PokemonType.Normal),
        PokemonMove("Razor Leaf", MovePP(10, 10), PokemonType.Grass),
        PokemonMove("Dragon Rage", MovePP(10, 10), PokemonType.Dragon),
        PokemonMove("Waterfall", MovePP(10, 10), PokemonType.Water),
    )
}