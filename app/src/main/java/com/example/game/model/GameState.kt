package com.example.game.model

/**
 * High-level screen modes in the game.
 */
enum class GameScreen {
    MAIN_MENU,
    OVERWORLD,
    BATTLE,
    INVENTORY,
    WORLD_MAP,
    GAME_OVER,
    VICTORY
}

/**
 * Active active dialogue popup.
 */
data class ActiveDialogue(
    val speakerName: String,
    val speakerTitle: String,
    val spriteKey: String,
    val lines: List<String>,
    var currentLineIndex: Int = 0
)

/**
 * Message / notification toast in game HUD.
 */
data class GameNotification(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val message: String,
    val iconKey: String? = null,
    var durationSeconds: Float = 3.0f
)
