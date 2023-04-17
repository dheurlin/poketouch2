package xyz.heurlin.poketouch.types

/** Struct representing an entry in the Moves table */
class MoveStruct(private val bytes: ByteArray) {
    val animation get() = bytes[0].toUByte().toInt()
    val effect get() = bytes[1].toUByte().toInt()
    val power get() = bytes[2].toUByte().toInt()
    val type get() = PokemonType.fromNumber(bytes[3].toUByte().toInt())
    val accuracy get() = bytes[4].toUByte().toInt()
    val basePP get() = bytes[5].toUByte().toInt()
    val effectChange get() = bytes[6].toUByte().toInt()
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
