package com.example.game.model

/**
 * Item types in the RPG.
 */
enum class ItemType {
    WEAPON,
    ARMOR,
    CONSUMABLE,
    MATERIAL,
    QUEST_ARTIFACT
}

/**
 * Item definition.
 */
data class Item(
    val id: String,
    val name: String,
    val description: String,
    val type: ItemType,
    val value: Int = 10,
    val attackBonus: Int = 0,
    val defenseBonus: Int = 0,
    val hpRestore: Int = 0,
    val mpRestore: Int = 0,
    val iconKey: String,
    val stackCount: Int = 1,
    val maxStack: Int = 99
) {
    fun copyWithCount(count: Int): Item = this.copy(stackCount = count)
}

/**
 * Crafting recipes for the crafting system.
 */
data class CraftingIngredient(
    val itemId: String,
    val quantity: Int
)

data class CraftingRecipe(
    val id: String,
    val resultItem: Item,
    val resultQuantity: Int,
    val ingredients: List<CraftingIngredient>,
    val description: String
)

object ItemCatalog {
    val POTION_HP = Item(
        id = "potion_hp",
        name = "Poción de Salud",
        description = "Restaura 60 puntos de salud instantáneamente.",
        type = ItemType.CONSUMABLE,
        hpRestore = 60,
        iconKey = "potion_red",
        value = 25
    )

    val POTION_MP = Item(
        id = "potion_mp",
        name = "Poción de Maná",
        description = "Restaura 45 puntos de maná mágico.",
        type = ItemType.CONSUMABLE,
        mpRestore = 45,
        iconKey = "potion_blue",
        value = 30
    )

    val ELIXIR_SUPREME = Item(
        id = "elixir_supreme",
        name = "Elixir Supremo",
        description = "Restaura por completo toda la salud y maná.",
        type = ItemType.CONSUMABLE,
        hpRestore = 999,
        mpRestore = 999,
        iconKey = "potion_gold",
        value = 150
    )

    val WOOD = Item(
        id = "wood",
        name = "Madera de Roble",
        description = "Madera resistente obtenida de árboles de bosque.",
        type = ItemType.MATERIAL,
        iconKey = "wood",
        value = 5
    )

    val IRON_ORE = Item(
        id = "iron_ore",
        name = "Mineral de Hierro",
        description = "Mineral tosco para forjar armas y armaduras.",
        type = ItemType.MATERIAL,
        iconKey = "ore",
        value = 12
    )

    val HERB = Item(
        id = "herb",
        name = "Hierba Silvestre",
        description = "Planta con propiedades medicinales regenerativas.",
        type = ItemType.MATERIAL,
        iconKey = "herb",
        value = 8
    )

    val LEATHER = Item(
        id = "leather",
        name = "Cuero Curtido",
        description = "Piel obtenida de animales salvajes para armaduras.",
        type = ItemType.MATERIAL,
        iconKey = "leather",
        value = 10
    )

    val MAGIC_CRYSTAL = Item(
        id = "magic_crystal",
        name = "Cristal Arcano",
        description = "Resonante gema imbuida de energía mágica pura.",
        type = ItemType.MATERIAL,
        iconKey = "crystal",
        value = 35
    )

    val SWORD_IRON = Item(
        id = "sword_iron",
        name = "Espada de Hierro",
        description = "Espada balanceada de caballero (+8 Ataque).",
        type = ItemType.WEAPON,
        attackBonus = 8,
        iconKey = "sword_iron",
        value = 60
    )

    val SWORD_RUNIC = Item(
        id = "sword_runic",
        name = "Espada Rúnica",
        description = "Hoja forjada con runas ancestrales (+18 Ataque).",
        type = ItemType.WEAPON,
        attackBonus = 18,
        iconKey = "sword_runic",
        value = 180
    )

    val ARMOR_LEATHER = Item(
        id = "armor_leather",
        name = "Túnica Reforzada",
        description = "Protección ligera pero resistente (+5 Defensa).",
        type = ItemType.ARMOR,
        defenseBonus = 5,
        iconKey = "armor_leather",
        value = 50
    )

    val ARMOR_STEEL = Item(
        id = "armor_steel",
        name = "Placas de Titán",
        description = "Pesada armadura de acero forjado (+14 Defensa).",
        type = ItemType.ARMOR,
        defenseBonus = 14,
        iconKey = "armor_steel",
        value = 160
    )

    val ANCIENT_ARTIFACT = Item(
        id = "ancient_artifact",
        name = "Orbe del Sol Ancestral",
        description = "Reliquia legendaria oculta en las Ruinas que despierta al Titán.",
        type = ItemType.QUEST_ARTIFACT,
        attackBonus = 10,
        defenseBonus = 10,
        iconKey = "artifact_sun",
        value = 1000
    )

    val RECIPES = listOf(
        CraftingRecipe(
            id = "craft_potion_hp",
            resultItem = POTION_HP,
            resultQuantity = 1,
            ingredients = listOf(CraftingIngredient("herb", 2)),
            description = "Destilar 2 Hierbas Silvestres para crear una Poción de Salud."
        ),
        CraftingRecipe(
            id = "craft_potion_mp",
            resultItem = POTION_MP,
            resultQuantity = 1,
            ingredients = listOf(CraftingIngredient("magic_crystal", 1)),
            description = "Canalizar la energía de 1 Cristal Arcano en una Poción de Maná."
        ),
        CraftingRecipe(
            id = "craft_sword_iron",
            resultItem = SWORD_IRON,
            resultQuantity = 1,
            ingredients = listOf(
                CraftingIngredient("wood", 2),
                CraftingIngredient("iron_ore", 3)
            ),
            description = "Forjar una sólida Espada de Hierro básica."
        ),
        CraftingRecipe(
            id = "craft_sword_runic",
            resultItem = SWORD_RUNIC,
            resultQuantity = 1,
            ingredients = listOf(
                CraftingIngredient("iron_ore", 4),
                CraftingIngredient("magic_crystal", 2)
            ),
            description = "Forjar una letal Espada Rúnica imbuida de magia."
        ),
        CraftingRecipe(
            id = "craft_armor_steel",
            resultItem = ARMOR_STEEL,
            resultQuantity = 1,
            ingredients = listOf(
                CraftingIngredient("leather", 3),
                CraftingIngredient("iron_ore", 4)
            ),
            description = "Forjar pesadas Placas de Titán con refuerzos de cuero."
        ),
        CraftingRecipe(
            id = "craft_elixir_supreme",
            resultItem = ELIXIR_SUPREME,
            resultQuantity = 1,
            ingredients = listOf(
                CraftingIngredient("potion_hp", 1),
                CraftingIngredient("potion_mp", 1),
                CraftingIngredient("magic_crystal", 1)
            ),
            description = "Combinar pociones y un cristal arcano en un Elixir Supremo."
        )
    )
}
