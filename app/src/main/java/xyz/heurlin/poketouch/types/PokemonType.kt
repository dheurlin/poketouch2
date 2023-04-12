package xyz.heurlin.poketouch.types
import androidx.compose.ui.graphics.Color
import xyz.heurlin.poketouch.ui.theme.*

enum class PokemonType {
    Normal {
        override val color: Color
            get() = PokeTypeNormal
    },
    Fighting {
        override val color: Color
            get() = PokeTypeFighting
    },
    Flying {
        override val color: Color
            get() = PokeTypeFlying
    },
    Poison {
        override val color: Color
            get() = PokeTypePoison
    },
    Ground {
        override val color: Color
            get() = PokeTypeGround
    },
    Rock {
        override val color: Color
            get() = PokeTypeRock
    },
    Bug {
        override val color: Color
            get() = PokeTypeBug
    },
    Ghost {
        override val color: Color
            get() = PokeTypeGhost
    },
    Steel {
        override val color: Color
            get() = PokeTypeSteel
    },
    Fire {
        override val color: Color
            get() = PokeTypeFire
    },
    Water {
        override val color: Color
            get() = PokeTypeWater
    },
    Grass {
        override val color: Color
            get() = PokeTypeGrass
    },
    Electric {
        override val color: Color
            get() = PokeTypeElectric
    },
    Psychic {
        override val color: Color
            get() = PokeTypePsychic
    },
    Ice {
        override val color: Color
            get() = PokeTypeIce
    },
    Dragon {
        override val color: Color
            get() = PokeTypeDragon
    },
    Dark {
        override val color: Color
            get() = PokeTypeDragon
    };

    abstract val color: Color;
}