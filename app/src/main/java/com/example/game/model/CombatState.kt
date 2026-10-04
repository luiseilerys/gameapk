package com.example.game.model

/**
 * Status effects that can afflict combatants.
 */
enum class StatusEffect(val displayName: String, val turns: Int) {
    POISON("Veneno", 3),
    BURN("Quemadura", 2),
    ATTACK_UP("Furia (+Atk)", 3),
    DEFENSE_UP("Fortaleza (+Def)", 3)
}

/**
 * Combat skills usable by player and enemies.
 */
data class CombatSkill(
    val id: String,
    val name: String,
    val description: String,
    val mpCost: Int,
    val damageMultiplier: Float = 1.0f,
    val isMagical: Boolean = false,
    val inflictsStatus: StatusEffect? = null,
    val healAmount: Int = 0,
    val targetSelf: Boolean = false
)

object CombatSkillCatalog {
    val POWER_SLASH = CombatSkill(
        id = "power_slash",
        name = "Tajo Vorpal",
        description = "Poderoso corte físico con el filo de la espada (1.6x daño).",
        mpCost = 12,
        damageMultiplier = 1.6f
    )

    val ARCANE_BOLT = CombatSkill(
        id = "arcane_bolt",
        name = "Rayo Arcano",
        description = "Dispara una saeta elemental pura que ignora defensas (1.8x daño mágico).",
        mpCost = 18,
        damageMultiplier = 1.8f,
        isMagical = true
    )

    val HOLY_LIGHT = CombatSkill(
        id = "holy_light",
        name = "Luz Sanadora",
        description = "Invoca un haz celestial que cura 75 puntos de salud.",
        mpCost = 15,
        healAmount = 75,
        targetSelf = true
    )

    val BATTLE_CRY = CombatSkill(
        id = "battle_cry",
        name = "Grito de Guerra",
        description = "Aumenta la fuerza de ataque durante 3 turnos.",
        mpCost = 10,
        inflictsStatus = StatusEffect.ATTACK_UP,
        targetSelf = true
    )

    // Enemy skills
    val ENEMY_POISON_BITE = CombatSkill(
        id = "poison_bite",
        name = "Mordedura Tóxica",
        description = "Muerde e inyecta veneno.",
        mpCost = 8,
        damageMultiplier = 1.2f,
        inflictsStatus = StatusEffect.POISON
    )

    val ENEMY_FIRE_BREATH = CombatSkill(
        id = "fire_breath",
        name = "Aliento Ígneo",
        description = "Lanza llamaradas abrasadoras.",
        mpCost = 15,
        damageMultiplier = 1.5f,
        inflictsStatus = StatusEffect.BURN,
        isMagical = true
    )

    val BOSS_VOID_STORM = CombatSkill(
        id = "void_storm",
        name = "Tormenta de Vacío",
        description = "Devastador cataclismo dimensional que estremece la realidad.",
        mpCost = 25,
        damageMultiplier = 2.2f,
        isMagical = true
    )
}

/**
 * Combatant interface representing any entity in turn-based combat.
 */
data class Combatant(
    val id: String,
    val name: String,
    val isPlayer: Boolean,
    var currentHp: Int,
    val maxHp: Int,
    var currentMp: Int,
    val maxMp: Int,
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val spriteKey: String,
    val isBoss: Boolean = false,
    val skills: List<CombatSkill> = emptyList(),
    var activeEffects: MutableList<Pair<StatusEffect, Int>> = mutableListOf(),
    var isDefending: Boolean = false
) {
    val isAlive: Boolean get() = currentHp > 0
}

/**
 * Floating damage text for combat feedback.
 */
data class FloatingText(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val text: String,
    val colorHex: Long,
    val isCritical: Boolean = false,
    val startX: Float,
    val startY: Float,
    var offsetY: Float = 0f,
    var alpha: Float = 1.0f
)

/**
 * Turn-based battle action type.
 */
enum class BattleActionType {
    ATTACK,
    SKILL,
    ITEM,
    FLEE
}

/**
 * Full state of an active turn-based battle.
 */
data class BattleState(
    val biome: Biome,
    val playerCombatant: Combatant,
    val enemyCombatants: List<Combatant>,
    var currentTurnIndex: Int = 0,
    var turnOrder: List<Combatant> = emptyList(),
    var roundNumber: Int = 1,
    var isPlayerTurn: Boolean = false,
    var battleLog: List<String> = listOf("¡Comienza la batalla!"),
    var isTransitioning: Boolean = false,
    var isVictory: Boolean = false,
    var isDefeat: Boolean = false,
    var hasFled: Boolean = false,
    var xpEarned: Int = 0,
    var goldEarned: Int = 0,
    var itemDrops: List<Item> = emptyList(),
    var floatingTexts: List<FloatingText> = emptyList(),
    var activeAttackerId: String? = null,
    var activeTargetId: String? = null,
    var isAttackAnimating: Boolean = false
)
