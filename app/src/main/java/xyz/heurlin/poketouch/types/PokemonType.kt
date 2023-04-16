package xyz.heurlin.poketouch.types
import androidx.compose.ui.graphics.Color
import xyz.heurlin.poketouch.ui.theme.*

enum class PokemonType(val color: Color, val number: Int) {
    Normal(color = PokeTypeNormal, number = 0),
    Fighting(color = PokeTypeFighting, number = 1),
    Flying(color = PokeTypeFlying, number = 2),
    Poison(color = PokeTypePoison, number = 3),
    Ground(color = PokeTypeGround, number = 4),
    Rock(color = PokeTypeRock, number = 5),
    Bug(color = PokeTypeBug, number = 7),
    Ghost(color = PokeTypeGhost, number = 8),
    Steel(color = PokeTypeSteel, number = 9),
    Fire(color = PokeTypeFire, number = 29),
    Water(color = PokeTypeWater, number = 30),
    Grass(color = PokeTypeGrass, number = 31),
    Electric(color = PokeTypeElectric, number = 32),
    Psychic(color = PokeTypePsychic, number = 33),
    Ice(color = PokeTypeIce, number = 34),
    Dragon(color = PokeTypeDragon, number = 35),
    Dark(color = PokeTypeDark, number = 36);


    companion object {
        private val reverseMapping = PokemonType.values().associateBy { it.number }
        fun fromNumber(number: Int): PokemonType {
            if (!reverseMapping.containsKey(number)) {
                throw IllegalArgumentException("No type with number $number exists!")
            }
            return reverseMapping[number]!!
        }
    }
}

/*
DEF PHYSICAL EQU const_value
const NORMAL 1
const FIGHTING 2
const FLYING 3
const POISON 4
const GROUND 5
const ROCK 6
const BIRD 7
const BUG 8
const GHOST 9
const STEEL 10

DEF UNUSED_TYPES EQU const_value
const_next 19
const CURSE_TYPE  29
DEF UNUSED_TYPES_END EQU const_value

DEF SPECIAL EQU const_value
const FIRE 30
const WATER 31
const GRASS 32
const ELECTRIC 33
const PSYCHIC_TYPE 34
const ICE 35
const DRAGON 36
const DARK 37
DEF TYPES_END EQU const_value
*/