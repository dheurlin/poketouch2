package xyz.heurlin.poketouch.types

/** Struct representing a Pokémon in the player party */
class PartyMonStruct(private val bytes: ByteArray) {
    // TODO Fill in as needed, from definition below
    val currentPPs get() = bytes.slice(23 .. 23 + 3).map{ it.toUByte().toInt() }
}
/*
MACRO box_struct
\1Species::        db
\1Item::           db
\1Moves::          ds NUM_MOVES
\1ID::             dw
\1Exp::            ds 3
\1StatExp::
\1HPExp::          dw
\1AtkExp::         dw
\1DefExp::         dw
\1SpdExp::         dw
\1SpcExp::         dw
\1DVs::            dw
\1PP::             ds NUM_MOVES
\1Happiness::      db
\1PokerusStatus::  db
\1CaughtData::
\1CaughtTime::
\1CaughtLevel::    db
\1CaughtGender::
\1CaughtLocation:: db
\1Level::          db
\1BoxEnd::
ENDM

MACRO party_struct
	box_struct \1
\1Status::         db
\1Unused::         db
\1HP::             dw
\1MaxHP::          dw
\1Stats:: ; big endian
\1Attack::         dw
\1Defense::        dw
\1Speed::          dw
\1SpclAtk::        dw
\1SpclDef::        dw
\1StructEnd::
ENDM
 */