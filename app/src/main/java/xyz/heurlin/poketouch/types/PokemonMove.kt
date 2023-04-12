package xyz.heurlin.poketouch.types

data class PokemonMove(val name: String, val pp: MovePP, val type: PokemonType)
data class MovePP(val total: Int, val current: Int)
