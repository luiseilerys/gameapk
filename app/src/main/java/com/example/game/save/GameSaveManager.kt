package com.example.game.save

import android.content.Context
import android.content.SharedPreferences
import com.example.game.model.Item
import com.example.game.model.ItemCatalog
import com.example.game.model.Player
import org.json.JSONArray
import org.json.JSONObject

/**
 * Handles persistent game state saving and loading via SharedPreferences and JSON.
 */
class GameSaveManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aethelgard_save_prefs", Context.MODE_PRIVATE)

    fun hasSavedGame(): Boolean {
        return prefs.getBoolean("has_saved_game", false)
    }

    fun getSavedLanguage(): com.example.game.localization.GameLanguage {
        val code = prefs.getString("game_language", "es") ?: "es"
        return if (code == "en") com.example.game.localization.GameLanguage.ENGLISH else com.example.game.localization.GameLanguage.SPANISH
    }

    fun saveLanguage(lang: com.example.game.localization.GameLanguage) {
        prefs.edit().putString("game_language", lang.code).apply()
    }

    fun saveGame(
        player: Player,
        exploredChunksCount: Int,
        defeatedEnemyIds: Set<String>,
        openedChestIds: Set<String>
    ) {
        val editor = prefs.edit()
        editor.putBoolean("has_saved_game", true)
        editor.putFloat("player_x", player.worldX)
        editor.putFloat("player_y", player.worldY)
        editor.putInt("player_level", player.level)
        editor.putInt("player_xp", player.xp)
        editor.putInt("player_current_hp", player.currentHp)
        editor.putInt("player_max_hp", player.maxHp)
        editor.putInt("player_current_mp", player.currentMp)
        editor.putInt("player_max_mp", player.maxMp)
        editor.putInt("player_str", player.baseStrength)
        editor.putInt("player_def", player.baseDefense)
        editor.putInt("player_spd", player.baseSpeed)
        editor.putInt("player_gold", player.gold)
        editor.putBoolean("player_has_artifact", player.hasArtifact)
        editor.putBoolean("player_has_defeated_boss", player.hasDefeatedBoss)
        editor.putString("player_weapon_id", player.equippedWeapon.id)
        editor.putString("player_armor_id", player.equippedArmor.id)
        editor.putInt("explored_chunks", exploredChunksCount)

        // Serialize inventory
        val invArray = JSONArray()
        for (item in player.inventory) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("count", item.stackCount)
            invArray.put(obj)
        }
        editor.putString("inventory_json", invArray.toString())

        // Save opened chests and defeated enemies
        editor.putStringSet("opened_chests", openedChestIds)
        editor.putStringSet("defeated_enemies", defeatedEnemyIds)

        editor.apply()
    }

    fun loadGame(player: Player): SaveLoadResult {
        if (!hasSavedGame()) return SaveLoadResult(false, 0, emptySet(), emptySet())

        player.worldX = prefs.getFloat("player_x", 0f)
        player.worldY = prefs.getFloat("player_y", 0f)
        player.level = prefs.getInt("player_level", 1)
        player.xp = prefs.getInt("player_xp", 0)
        player.currentHp = prefs.getInt("player_current_hp", 120)
        player.maxHp = prefs.getInt("player_max_hp", 120)
        player.currentMp = prefs.getInt("player_current_mp", 50)
        player.maxMp = prefs.getInt("player_max_mp", 50)
        player.baseStrength = prefs.getInt("player_str", 14)
        player.baseDefense = prefs.getInt("player_def", 8)
        player.baseSpeed = prefs.getInt("player_spd", 12)
        player.gold = prefs.getInt("player_gold", 50)
        player.hasArtifact = prefs.getBoolean("player_has_artifact", false)
        player.hasDefeatedBoss = prefs.getBoolean("player_has_defeated_boss", false)

        val weaponId = prefs.getString("player_weapon_id", ItemCatalog.SWORD_IRON.id)
        player.equippedWeapon = if (weaponId == ItemCatalog.SWORD_RUNIC.id) ItemCatalog.SWORD_RUNIC else ItemCatalog.SWORD_IRON

        val armorId = prefs.getString("player_armor_id", ItemCatalog.ARMOR_LEATHER.id)
        player.equippedArmor = if (armorId == ItemCatalog.ARMOR_STEEL.id) ItemCatalog.ARMOR_STEEL else ItemCatalog.ARMOR_LEATHER

        // Load inventory
        val invJson = prefs.getString("inventory_json", null)
        if (!invJson.isNullOrEmpty()) {
            player.inventory.clear()
            val invArray = JSONArray(invJson)
            for (i in 0 until invArray.length()) {
                val obj = invArray.getJSONObject(i)
                val id = obj.getString("id")
                val count = obj.getInt("count")
                val baseItem = findItemById(id)
                if (baseItem != null) {
                    player.inventory.add(baseItem.copyWithCount(count))
                }
            }
        }

        val exploredChunks = prefs.getInt("explored_chunks", 1)
        val openedChests = prefs.getStringSet("opened_chests", emptySet()) ?: emptySet()
        val defeatedEnemies = prefs.getStringSet("defeated_enemies", emptySet()) ?: emptySet()

        return SaveLoadResult(true, exploredChunks, openedChests, defeatedEnemies)
    }

    private fun findItemById(id: String): Item? {
        return when (id) {
            ItemCatalog.POTION_HP.id -> ItemCatalog.POTION_HP
            ItemCatalog.POTION_MP.id -> ItemCatalog.POTION_MP
            ItemCatalog.ELIXIR_SUPREME.id -> ItemCatalog.ELIXIR_SUPREME
            ItemCatalog.WOOD.id -> ItemCatalog.WOOD
            ItemCatalog.IRON_ORE.id -> ItemCatalog.IRON_ORE
            ItemCatalog.HERB.id -> ItemCatalog.HERB
            ItemCatalog.LEATHER.id -> ItemCatalog.LEATHER
            ItemCatalog.MAGIC_CRYSTAL.id -> ItemCatalog.MAGIC_CRYSTAL
            ItemCatalog.SWORD_IRON.id -> ItemCatalog.SWORD_IRON
            ItemCatalog.SWORD_RUNIC.id -> ItemCatalog.SWORD_RUNIC
            ItemCatalog.ARMOR_LEATHER.id -> ItemCatalog.ARMOR_LEATHER
            ItemCatalog.ARMOR_STEEL.id -> ItemCatalog.ARMOR_STEEL
            ItemCatalog.ANCIENT_ARTIFACT.id -> ItemCatalog.ANCIENT_ARTIFACT
            else -> null
        }
    }

    fun clearSave() {
        prefs.edit().clear().apply()
    }
}

data class SaveLoadResult(
    val success: Boolean,
    val exploredChunks: Int,
    val openedChests: Set<String>,
    val defeatedEnemies: Set<String>
)
