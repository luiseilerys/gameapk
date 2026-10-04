package com.example.game.model

/**
 * Biomes in the procedural world of Aethelgard.
 * Each biome features a distinct color palette, terrain tiles, unique enemies and resources.
 */
enum class Biome(
    val displayName: String,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    FOREST("Bosque Esmeralda", "Frondosos árboles, animales salvajes y manadas de lobos.", 0xFF2E7D32, 0xFF1B5E20),
    PLAINS("Praderas Doradas", "Extensas llanuras verdes donde pastan ciervos pacíficos.", 0xFF558B2F, 0xFF33691E),
    DESERT("Desierto de Ceniza", "Tierras áridas y abrasadoras habitadas por escorpiones gigantes.", 0xFFC5A059, 0xFF8D6E63),
    MOUNTAIN("Picos Rocosos", "Montañas escarpadas custodiadas por antiguos gólems de roca.", 0xFF78909C, 0xFF455A64),
    SWAMP("Pantano Brumoso", "Ciénagas húmedas repletas de lodo, ranas venenosas y cocodrilos.", 0xFF3E5A44, 0xFF28392B),
    RUINS("Ruinas Olvidadas", "Vestigios de una civilización arcana que resguardan el artefacto sagrado.", 0xFF544A63, 0xFF31263E)
}

/**
 * Terrain tiles generated across the world.
 */
enum class TileType(
    val isWalkable: Boolean,
    val movementCost: Float = 1.0f
) {
    GRASS(true),
    DIRT(true),
    SAND(true),
    WATER(false),
    DEEP_WATER(false),
    ROCK_GROUND(true),
    SWAMP_MUD(true, movementCost = 1.4f),
    RUINS_STONE(true),
    CLIFF(false)
}

/**
 * Static or interactive decorative objects within tiles.
 */
enum class DecoType(
    val blocksMovement: Boolean
) {
    NONE(false),
    TREE_OAK(true),
    TREE_PINE(true),
    TREE_PALM(true),
    DEAD_TREE(true),
    ROCK_BOULDER(true),
    BUSH(false),
    FLOWER_RED(false),
    FLOWER_BLUE(false),
    RUINS_PILLAR(true),
    RUINS_ALTAR(true),
    CACTUS(true)
}
