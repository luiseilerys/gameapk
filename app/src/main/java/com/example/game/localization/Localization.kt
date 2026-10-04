package com.example.game.localization

import com.example.game.model.Biome

/**
 * Supported in-game languages.
 */
enum class GameLanguage(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English")
}

/**
 * Complete bilingual dictionary for all game text.
 */
object Strings {
    fun getTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "A E T H E L G A R D"
        GameLanguage.ENGLISH -> "A E T H E L G A R D"
    }

    fun getSubtitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "— El Despertar del Titán —"
        GameLanguage.ENGLISH -> "— Awakening of the Titan —"
    }

    fun getTitleDescription(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Aventura 2D de Exploración & Combate SNES"
        GameLanguage.ENGLISH -> "2D SNES Open World Exploration & Combat RPG"
    }

    fun getNewGame(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "⚔ NUEVA PARTIDA"
        GameLanguage.ENGLISH -> "⚔ NEW GAME"
    }

    fun getContinue(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "▶ CONTINUAR AVENTURA"
        GameLanguage.ENGLISH -> "▶ CONTINUE ADVENTURE"
    }

    fun getNoSave(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "(SIN PARTIDA GUARDADA)"
        GameLanguage.ENGLISH -> "(NO SAVED GAME)"
    }

    fun getAudioOptions(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "⚙ AJUSTES Y OPCIONES"
        GameLanguage.ENGLISH -> "⚙ SETTINGS & OPTIONS"
    }

    fun getLanguageSetting(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Idioma / Language"
        GameLanguage.ENGLISH -> "Language / Idioma"
    }

    fun getSfxSetting(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Efectos SFX (8-Bit)"
        GameLanguage.ENGLISH -> "SFX Sounds (8-Bit)"
    }

    fun getMusicSetting(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Música Chiptune SNES"
        GameLanguage.ENGLISH -> "Chiptune SNES Music"
    }

    fun getBack(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Volver"
        GameLanguage.ENGLISH -> "Back"
    }

    // Pause Menu
    fun getPauseTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "PAUSA"
        GameLanguage.ENGLISH -> "PAUSED"
    }

    fun getResume(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Continuar Aventura"
        GameLanguage.ENGLISH -> "Resume Adventure"
    }

    fun getInventory(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Mochila e Inventario"
        GameLanguage.ENGLISH -> "Backpack & Inventory"
    }

    fun getWorldMap(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Mapa Mundial"
        GameLanguage.ENGLISH -> "World Map"
    }

    fun getSaveGame(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Guardar Partida"
        GameLanguage.ENGLISH -> "Save Game"
    }

    fun getExitToMenu(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Salir al Menú Principal"
        GameLanguage.ENGLISH -> "Exit to Main Menu"
    }

    // HUD & Stats
    fun getLevelShort(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "NV"
        GameLanguage.ENGLISH -> "LV"
    }

    fun getGold(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Oro"
        GameLanguage.ENGLISH -> "Gold"
    }

    fun getPressAToContinue(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Presiona A para continuar ▶"
        GameLanguage.ENGLISH -> "Press A to continue ▶"
    }

    fun getBiomeName(biome: Biome, lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> biome.displayName
        GameLanguage.ENGLISH -> when (biome) {
            Biome.FOREST -> "Emerald Forest"
            Biome.PLAINS -> "Golden Plains"
            Biome.DESERT -> "Ashen Desert"
            Biome.MOUNTAIN -> "Rocky Peaks"
            Biome.SWAMP -> "Misty Swamp"
            Biome.RUINS -> "Forgotten Ruins"
        }
    }

    // Combat
    fun getRound(lang: GameLanguage, round: Int): String = when (lang) {
        GameLanguage.SPANISH -> "Ronda $round"
        GameLanguage.ENGLISH -> "Round $round"
    }

    fun getHero(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Héroe"
        GameLanguage.ENGLISH -> "Hero"
    }

    fun getAttack(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "⚔ LUCHAR"
        GameLanguage.ENGLISH -> "⚔ FIGHT"
    }

    fun getSkill(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "✨ EQUIPO"
        GameLanguage.ENGLISH -> "✨ PKMN"
    }

    fun getItem(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "🎒 MOCHILA"
        GameLanguage.ENGLISH -> "🎒 BAG"
    }

    fun getFlee(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "🏃 HUIR"
        GameLanguage.ENGLISH -> "🏃 RUN"
    }

    fun getWhatWillDo(lang: GameLanguage, name: String): String = when (lang) {
        GameLanguage.SPANISH -> "¿Qué va a hacer $name?"
        GameLanguage.ENGLISH -> "What will $name do?"
    }

    fun getSkillsSubmenu(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Habilidades Especiales"
        GameLanguage.ENGLISH -> "Special Skills"
    }

    fun getItemsSubmenu(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Objetos Consumibles"
        GameLanguage.ENGLISH -> "Consumable Items"
    }

    fun getNoConsumables(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "No tienes consumibles en tu mochila."
        GameLanguage.ENGLISH -> "No consumables in your backpack."
    }

    fun getVictory(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "¡VICTORIA!"
        GameLanguage.ENGLISH -> "VICTORY!"
    }

    fun getRewards(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Recompensas obtenidas:"
        GameLanguage.ENGLISH -> "Rewards Earned:"
    }

    fun getContinueAdventure(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Continuar Aventura ▶"
        GameLanguage.ENGLISH -> "Continue Adventure ▶"
    }

    // Inventory & Crafting
    fun getCharacterMenuTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "MENU DE PERSONAJE"
        GameLanguage.ENGLISH -> "CHARACTER MENU"
    }

    fun getTabBackpack(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Mochila (4x5)"
        GameLanguage.ENGLISH -> "Backpack (4x5)"
    }

    fun getTabCrafting(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Forja & Alquimia"
        GameLanguage.ENGLISH -> "Forge & Alchemy"
    }

    fun getTabStats(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Estadísticas"
        GameLanguage.ENGLISH -> "Attributes"
    }

    fun getEquippedWeapon(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Arma Equipada"
        GameLanguage.ENGLISH -> "Equipped Weapon"
    }

    fun getEquippedArmor(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Armadura Equipada"
        GameLanguage.ENGLISH -> "Equipped Armor"
    }

    fun getBackpackItemsCount(lang: GameLanguage, count: Int): String = when (lang) {
        GameLanguage.SPANISH -> "Objetos en Mochila ($count/20):"
        GameLanguage.ENGLISH -> "Backpack Items ($count/20):"
    }

    fun getUse(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Usar"
        GameLanguage.ENGLISH -> "Use"
    }

    fun getEquip(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Equipar"
        GameLanguage.ENGLISH -> "Equip"
    }

    fun getDiscard(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Descartar"
        GameLanguage.ENGLISH -> "Discard"
    }

    fun getCraft(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Fabricar"
        GameLanguage.ENGLISH -> "Craft"
    }

    fun getRequiredMaterials(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Materiales requeridos:"
        GameLanguage.ENGLISH -> "Required materials:"
    }

    fun getClose(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Cerrar ✕"
        GameLanguage.ENGLISH -> "Close ✕"
    }

    // World Map
    fun getWorldMapTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "MAPA MUNDIAL DE AETHELGARD"
        GameLanguage.ENGLISH -> "AETHELGARD WORLD MAP"
    }

    fun getArtifactRadar(lang: GameLanguage, dist: Int): String = when (lang) {
        GameLanguage.SPANISH -> "Distancia al Orbe del Sol en Ruinas (Sector 10, 8): $dist baldosas"
        GameLanguage.ENGLISH -> "Distance to the Sun Orb in Ruins (Sector 10, 8): $dist tiles"
    }

    // Game Over & Victory
    fun getGameOverTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "FIN DE LA PARTIDA"
        GameLanguage.ENGLISH -> "GAME OVER"
    }

    fun getGameOverDesc(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Tu fuerza ha menguado ante los peligros de Aethelgard. La leyenda del héroe aguarda otra oportunidad."
        GameLanguage.ENGLISH -> "Your strength has waned against the perils of Aethelgard. The hero's legend awaits another chance."
    }

    fun getRetry(lang: GameLanguage, hasSave: Boolean): String = when (lang) {
        GameLanguage.SPANISH -> if (hasSave) "Cargar Último Guardado" else "Reintentar Aventura"
        GameLanguage.ENGLISH -> if (hasSave) "Load Last Save" else "Retry Adventure"
    }

    fun getSupremeVictoryTitle(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "★ ¡VICTORIA SUPREMA! ★"
        GameLanguage.ENGLISH -> "★ SUPREME VICTORY! ★"
    }

    fun getSupremeVictoryDesc(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Has derrotado al temible Titán del Abismo y sellado el Orbe del Sol Ancestral. La paz retorna al reino de Aethelgard."
        GameLanguage.ENGLISH -> "You have defeated the fearsome Abyssal Titan and sealed the Ancient Sun Orb. Peace returns to the realm of Aethelgard."
    }

    fun getPlayTime(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Tiempo de Partida:"
        GameLanguage.ENGLISH -> "Play Time:"
    }

    fun getLevelReached(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Nivel Alcanzado:"
        GameLanguage.ENGLISH -> "Level Reached:"
    }

    fun getEnemiesDefeated(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Enemigos Vencidos:"
        GameLanguage.ENGLISH -> "Enemies Defeated:"
    }

    fun getChestsOpened(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Cofres Abiertos:"
        GameLanguage.ENGLISH -> "Chests Opened:"
    }

    fun getSectorsExplored(lang: GameLanguage): String = when (lang) {
        GameLanguage.SPANISH -> "Sectores Explorados:"
        GameLanguage.ENGLISH -> "Explored Chunks:"
    }
}
