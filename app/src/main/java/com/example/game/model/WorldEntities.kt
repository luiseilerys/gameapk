package com.example.game.model

/**
 * Direction facing for 4-directional animations.
 */
enum class Direction {
    DOWN,
    UP,
    LEFT,
    RIGHT
}

/**
 * Player state in overworld.
 */
data class Player(
    var worldX: Float = 0f,
    var worldY: Float = 0f,
    var direction: Direction = Direction.DOWN,
    var isMoving: Boolean = false,
    var isAttacking: Boolean = false,
    var walkAnimTimer: Float = 0f,
    var level: Int = 1,
    var xp: Int = 0,
    var xpToNextLevel: Int = 100,
    var currentHp: Int = 120,
    var maxHp: Int = 120,
    var currentMp: Int = 50,
    var maxMp: Int = 50,
    var baseStrength: Int = 14,
    var baseDefense: Int = 8,
    var baseSpeed: Int = 12,
    var gold: Int = 50,
    var equippedWeapon: Item = ItemCatalog.SWORD_IRON,
    var equippedArmor: Item = ItemCatalog.ARMOR_LEATHER,
    var inventory: MutableList<Item> = mutableListOf(
        ItemCatalog.POTION_HP.copyWithCount(3),
        ItemCatalog.POTION_MP.copyWithCount(2),
        ItemCatalog.WOOD.copyWithCount(5),
        ItemCatalog.HERB.copyWithCount(4)
    ),
    var hasArtifact: Boolean = false,
    var hasDefeatedBoss: Boolean = false,
    var invulnerableTimer: Float = 0f
) {
    val totalAttack: Int get() = baseStrength + equippedWeapon.attackBonus
    val totalDefense: Int get() = baseDefense + equippedArmor.defenseBonus
}

/**
 * Enemy in the overworld.
 */
data class EnemyEntity(
    val id: String,
    var worldX: Float,
    var worldY: Float,
    val name: String,
    val biome: Biome,
    val spriteKey: String,
    val maxHp: Int,
    var currentHp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val xpReward: Int,
    val goldReward: Int,
    val possibleDrops: List<Item>,
    val isBoss: Boolean = false,
    val aggroRadius: Float = 4.5f,
    var isAggroed: Boolean = false,
    var patrolTimer: Float = 0f,
    var patrolDirX: Float = 0f,
    var patrolDirY: Float = 0f,
    var isAlive: Boolean = true
)

/**
 * Passive animals (deer, sheep, frog) that flee from the player.
 */
data class AnimalEntity(
    val id: String,
    var worldX: Float,
    var worldY: Float,
    val name: String,
    val spriteKey: String,
    val drops: List<Item>,
    var fleeTimer: Float = 0f,
    var dirX: Float = 0f,
    var dirY: Float = 0f,
    var isAlive: Boolean = true
)

/**
 * Interactive chests in the overworld.
 */
data class ChestEntity(
    val id: String,
    val worldX: Float,
    val worldY: Float,
    var isOpen: Boolean = false,
    val loot: List<Item>,
    val goldAmount: Int
)

/**
 * NPC with dialogues, quest clues, and trade.
 */
data class NpcEntity(
    val id: String,
    val worldX: Float,
    val worldY: Float,
    val name: String,
    val title: String,
    val spriteKey: String,
    val dialogues: List<String>
)

/**
 * Particle or slash effect in overworld.
 */
data class OverworldEffect(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val worldX: Float,
    val worldY: Float,
    val effectType: String,
    var duration: Float = 0.4f,
    var maxDuration: Float = 0.4f
)
