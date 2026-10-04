package com.example.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.ChiptuneAudioEngine
import com.example.game.audio.MusicTrack
import com.example.game.model.ActiveDialogue
import com.example.game.model.BattleActionType
import com.example.game.model.BattleState
import com.example.game.model.Biome
import com.example.game.model.CombatSkill
import com.example.game.model.CombatSkillCatalog
import com.example.game.model.Combatant
import com.example.game.model.CraftingRecipe
import com.example.game.model.Direction
import com.example.game.model.EnemyEntity
import com.example.game.model.FloatingText
import com.example.game.model.GameNotification
import com.example.game.model.GameScreen
import com.example.game.model.Item
import com.example.game.model.ItemCatalog
import com.example.game.model.ItemType
import com.example.game.model.Player
import com.example.game.model.StatusEffect
import com.example.game.save.GameSaveManager
import com.example.game.world.CHUNK_SIZE
import com.example.game.world.WorldManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Main ViewModel orchestrating the game loop, procedural world state,
 * hybrid combat mechanics, inventory, crafting, audio, and save system.
 */
class GameViewModel(application: Application) : AndroidViewModel(application) {

    val audio = ChiptuneAudioEngine(viewModelScope)
    val saveManager = GameSaveManager(application)
    val worldManager = WorldManager()

    // Core Game State
    val player = Player()

    private val _currentScreen = MutableStateFlow(GameScreen.MAIN_MENU)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    private val _currentBiome = MutableStateFlow(Biome.PLAINS)
    val currentBiome: StateFlow<Biome> = _currentBiome.asStateFlow()

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()

    private val _activeDialogue = MutableStateFlow<ActiveDialogue?>(null)
    val activeDialogue: StateFlow<ActiveDialogue?> = _activeDialogue.asStateFlow()

    private val _notifications = MutableStateFlow<List<GameNotification>>(emptyList())
    val notifications: StateFlow<List<GameNotification>> = _notifications.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _hasSaveGame = MutableStateFlow(saveManager.hasSavedGame())
    val hasSaveGame: StateFlow<Boolean> = _hasSaveGame.asStateFlow()

    private val _currentLanguage = MutableStateFlow(saveManager.getSavedLanguage())
    val currentLanguage: StateFlow<com.example.game.localization.GameLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: com.example.game.localization.GameLanguage) {
        _currentLanguage.value = lang
        saveManager.saveLanguage(lang)
        audio.playSfx("select")
        val msg = if (lang == com.example.game.localization.GameLanguage.SPANISH) "Idioma cambiado a Español" else "Language switched to English"
        showNotification(msg)
    }

    // Virtual Joystick Input Vector (-1f to 1f)
    var joystickX: Float = 0f
    var joystickY: Float = 0f

    // Camera center in world coordinates
    var cameraX: Float = 0f
    var cameraY: Float = 0f

    // Game Loop Job (60 FPS coroutine loop)
    private var gameLoopJob: Job? = null
    private var footstepTimer = 0f

    // Game statistics for victory screen
    var gameStartTime = System.currentTimeMillis()
    var enemiesDefeatedCount = 0
    var chestsOpenedCount = 0

    init {
        // Initialize player in starting chunk (0,0) center
        player.worldX = 16f
        player.worldY = 16f
        cameraX = player.worldX
        cameraY = player.worldY
        worldManager.updatePlayerPosition(player.worldX, player.worldY)
    }

    // ==========================================
    // GAME LIFECYCLE & SCREEN NAVIGATION
    // ==========================================

    fun startNewGame() {
        // Reset player stats
        player.worldX = 16f
        player.worldY = 16f
        player.level = 1
        player.xp = 0
        player.xpToNextLevel = 100
        player.currentHp = 120
        player.maxHp = 120
        player.currentMp = 50
        player.maxMp = 50
        player.baseStrength = 14
        player.baseDefense = 8
        player.baseSpeed = 12
        player.gold = 50
        player.hasArtifact = false
        player.hasDefeatedBoss = false
        player.equippedWeapon = ItemCatalog.SWORD_IRON
        player.equippedArmor = ItemCatalog.ARMOR_LEATHER
        player.inventory = mutableListOf(
            ItemCatalog.POTION_HP.copyWithCount(3),
            ItemCatalog.POTION_MP.copyWithCount(2),
            ItemCatalog.WOOD.copyWithCount(5),
            ItemCatalog.HERB.copyWithCount(4)
        )

        gameStartTime = System.currentTimeMillis()
        enemiesDefeatedCount = 0
        chestsOpenedCount = 0

        worldManager.updatePlayerPosition(player.worldX, player.worldY)
        cameraX = player.worldX
        cameraY = player.worldY

        _currentScreen.value = GameScreen.OVERWORLD
        _isPaused.value = false
        startGameLoop()
        updateMusicForCurrentState()
    }

    fun continueGame() {
        val result = saveManager.loadGame(player)
        if (result.success) {
            worldManager.openedChestIds.addAll(result.openedChests)
            worldManager.defeatedEnemyIds.addAll(result.defeatedEnemies)
            worldManager.updatePlayerPosition(player.worldX, player.worldY)
            cameraX = player.worldX
            cameraY = player.worldY

            _currentScreen.value = GameScreen.OVERWORLD
            _isPaused.value = false
            startGameLoop()
            updateMusicForCurrentState()
            showNotification("¡Partida cargada exitosamente!")
        } else {
            startNewGame()
        }
    }

    fun saveCurrentGame() {
        saveManager.saveGame(
            player = player,
            exploredChunksCount = worldManager.getExploredChunksCount(),
            defeatedEnemyIds = worldManager.defeatedEnemyIds,
            openedChestIds = worldManager.openedChestIds
        )
        _hasSaveGame.value = true
        audio.playSfx("select")
        showNotification("Partida guardada correctamente")
    }

    fun openInventory() {
        audio.playSfx("select")
        _currentScreen.value = GameScreen.INVENTORY
    }

    fun openWorldMap() {
        audio.playSfx("select")
        _currentScreen.value = GameScreen.WORLD_MAP
    }

    fun togglePause() {
        audio.playSfx("select")
        _isPaused.value = !_isPaused.value
    }

    fun resumeToOverworld() {
        _isPaused.value = false
        _currentScreen.value = GameScreen.OVERWORLD
        updateMusicForCurrentState()
    }

    fun returnToMainMenu() {
        gameLoopJob?.cancel()
        audio.stopMusic()
        _isPaused.value = false
        _currentScreen.value = GameScreen.MAIN_MENU
        _hasSaveGame.value = saveManager.hasSavedGame()
    }

    // ==========================================
    // 60 FPS REAL-TIME GAME LOOP
    // ==========================================

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastTime = System.nanoTime()

            while (isActive) {
                val now = System.nanoTime()
                val delta = ((now - lastTime) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastTime = now

                if (!_isPaused.value && _currentScreen.value == GameScreen.OVERWORLD) {
                    updateOverworld(delta)
                } else if (_currentScreen.value == GameScreen.BATTLE) {
                    updateBattleAnimation(delta)
                }

                // 60 FPS target (~16.6ms)
                delay(16)
            }
        }
    }

    private fun updateOverworld(delta: Float) {
        // 1. Update Player Movement & Invulnerability
        if (player.invulnerableTimer > 0f) {
            player.invulnerableTimer -= delta
        }

        val moveLen = sqrt(joystickX * joystickX + joystickY * joystickY)
        if (moveLen > 0.15f) {
            player.isMoving = true
            val speed = (4.0f + (player.baseSpeed - 10) * 0.1f) * delta
            val normX = joystickX / moveLen
            val normY = joystickY / moveLen

            // Direction facing
            player.direction = when {
                abs(normX) > abs(normY) -> if (normX > 0) Direction.RIGHT else Direction.LEFT
                else -> if (normY > 0) Direction.DOWN else Direction.UP
            }

            // Smooth walk cycle
            player.walkAnimTimer += delta * 7f

            // Collision detection with sliding
            val targetX = player.worldX + normX * speed
            val targetY = player.worldY + normY * speed

            if (worldManager.isWalkable(targetX, player.worldY)) {
                player.worldX = targetX
            }
            if (worldManager.isWalkable(player.worldX, targetY)) {
                player.worldY = targetY
            }

            // Footstep audio rhythm
            footstepTimer += delta
            if (footstepTimer > 0.38f) {
                footstepTimer = 0f
                audio.playSfx("footstep")
            }
        } else {
            player.isMoving = false
            player.walkAnimTimer = 0f
        }

        // 2. Camera Lerp smoothly centered on Player
        val lerpFactor = 0.12f
        cameraX += (player.worldX - cameraX) * lerpFactor
        cameraY += (player.worldY - cameraY) * lerpFactor

        // 3. Update dynamic loaded chunks around player
        worldManager.updatePlayerPosition(player.worldX, player.worldY)
        val currentBiomeAtPos = worldManager.getBiomeAtPlayer(player.worldX, player.worldY)
        if (_currentBiome.value != currentBiomeAtPos) {
            _currentBiome.value = currentBiomeAtPos
            updateMusicForCurrentState()
        }

        // 4. Update Entities in active chunks
        val activeChunks = worldManager.getActiveChunks()
        for (chunk in activeChunks) {
            // Update Enemies
            for (enemy in chunk.enemies) {
                if (!enemy.isAlive) continue

                val distToPlayer = distance(enemy.worldX, enemy.worldY, player.worldX, player.worldY)

                // Check hybrid combat encounter trigger
                if (distToPlayer < 0.85f && player.invulnerableTimer <= 0f) {
                    // Trigger Turn-Based Combat Transition!
                    triggerCombatTransition(enemy)
                    return
                }

                // Aggro AI: chase player if inside aggro radius
                if (distToPlayer < enemy.aggroRadius) {
                    enemy.isAggroed = true
                    val dirX = (player.worldX - enemy.worldX) / distToPlayer
                    val dirY = (player.worldY - enemy.worldY) / distToPlayer
                    val enemySpeed = 1.8f * delta
                    val nextX = enemy.worldX + dirX * enemySpeed
                    val nextY = enemy.worldY + dirY * enemySpeed
                    if (worldManager.isWalkable(nextX, nextY)) {
                        enemy.worldX = nextX
                        enemy.worldY = nextY
                    }
                } else {
                    enemy.isAggroed = false
                    // Idle patrol
                    enemy.patrolTimer -= delta
                    if (enemy.patrolTimer <= 0f) {
                        enemy.patrolTimer = (20..50).random() / 10f
                        enemy.patrolDirX = ((-1..1).random()).toFloat()
                        enemy.patrolDirY = ((-1..1).random()).toFloat()
                    }
                    val patrolSpeed = 0.6f * delta
                    val nextX = enemy.worldX + enemy.patrolDirX * patrolSpeed
                    val nextY = enemy.worldY + enemy.patrolDirY * patrolSpeed
                    if (worldManager.isWalkable(nextX, nextY)) {
                        enemy.worldX = nextX
                        enemy.worldY = nextY
                    }
                }
            }

            // Update Animals (flee from player)
            for (animal in chunk.animals) {
                if (!animal.isAlive) continue
                val dist = distance(animal.worldX, animal.worldY, player.worldX, player.worldY)
                if (dist < 3.2f) {
                    // Flee away
                    val fleeDirX = (animal.worldX - player.worldX) / dist
                    val fleeDirY = (animal.worldY - player.worldY) / dist
                    val fleeSpeed = 3.0f * delta
                    val nextX = animal.worldX + fleeDirX * fleeSpeed
                    val nextY = animal.worldY + fleeDirY * fleeSpeed
                    if (worldManager.isWalkable(nextX, nextY)) {
                        animal.worldX = nextX
                        animal.worldY = nextY
                    }
                }
            }
        }
    }

    // ==========================================
    // ACTION BUTTONS (A & B)
    // ==========================================

    fun onActionButtonAPressed() {
        if (_currentScreen.value != GameScreen.OVERWORLD) return

        // 1. If dialogue is active, advance it
        val currentDlg = _activeDialogue.value
        if (currentDlg != null) {
            audio.playSfx("select")
            if (currentDlg.currentLineIndex < currentDlg.lines.size - 1) {
                _activeDialogue.value = currentDlg.copy(currentLineIndex = currentDlg.currentLineIndex + 1)
            } else {
                _activeDialogue.value = null
            }
            return
        }

        // 2. Check for interactive NPC within 1.6 tiles
        val activeChunks = worldManager.getActiveChunks()
        for (chunk in activeChunks) {
            for (npc in chunk.npcs) {
                if (distance(player.worldX, player.worldY, npc.worldX, npc.worldY) < 1.6f) {
                    audio.playSfx("select")
                    _activeDialogue.value = ActiveDialogue(
                        speakerName = npc.name,
                        speakerTitle = npc.title,
                        spriteKey = npc.spriteKey,
                        lines = npc.dialogues
                    )
                    return
                }
            }
        }

        // 3. Check for Chest within 1.6 tiles
        for (chunk in activeChunks) {
            for (chest in chunk.chests) {
                if (!chest.isOpen && distance(player.worldX, player.worldY, chest.worldX, chest.worldY) < 1.6f) {
                    chest.isOpen = true
                    worldManager.recordChestOpened(chest.id)
                    chestsOpenedCount++
                    audio.playSfx("chest")

                    // Award loot
                    player.gold += chest.goldAmount
                    for (item in chest.loot) {
                        addItemToInventory(item)
                    }

                    if (chest.loot.any { it.id == ItemCatalog.ANCIENT_ARTIFACT.id }) {
                        player.hasArtifact = true
                        showNotification("¡HAS OBTENIDO EL ORBE DEL SOL ANCESTRAL!")
                        audio.playSfx("levelup")
                        // Spawn final boss!
                        spawnFinalBossTitan()
                    } else {
                        showNotification("¡Cofre abierto! +${chest.goldAmount} Oro y objetos.")
                    }
                    return
                }
            }
        }

        // 4. Check for huntable passive animal
        for (chunk in activeChunks) {
            for (animal in chunk.animals) {
                if (animal.isAlive && distance(player.worldX, player.worldY, animal.worldX, animal.worldY) < 1.6f) {
                    animal.isAlive = false
                    audio.playSfx("attack")
                    for (drop in animal.drops) {
                        addItemToInventory(drop)
                    }
                    showNotification("Cazaste un ${animal.name}. Recursos obtenidos.")
                    return
                }
            }
        }

        // 5. Default sword swing slash
        player.isAttacking = true
        audio.playSfx("attack")
        viewModelScope.launch {
            delay(220)
            player.isAttacking = false
        }
    }

    fun onActionButtonBPressed() {
        if (_currentScreen.value != GameScreen.OVERWORLD) return

        // Button B: Dash / Evasion burst
        audio.playSfx("flee")
        val dashDist = 2.0f
        val (dx, dy) = when (player.direction) {
            Direction.UP -> 0f to -dashDist
            Direction.DOWN -> 0f to dashDist
            Direction.LEFT -> -dashDist to 0f
            Direction.RIGHT -> dashDist to 0f
        }
        val targetX = player.worldX + dx
        val targetY = player.worldY + dy
        if (worldManager.isWalkable(targetX, targetY)) {
            player.worldX = targetX
            player.worldY = targetY
        }
        player.invulnerableTimer = 1.0f
    }

    private fun spawnFinalBossTitan() {
        // Spawn the legendary boss 5 tiles north of the artifact altar
        val boss = EnemyEntity(
            id = "final_boss_titan",
            worldX = (worldManager.generator.artifactChunkX * CHUNK_SIZE + 16).toFloat(),
            worldY = (worldManager.generator.artifactChunkY * CHUNK_SIZE + 10).toFloat(),
            name = "Titán del Abismo",
            biome = Biome.RUINS,
            spriteKey = "enemy_boss",
            maxHp = 350,
            currentHp = 350,
            attack = 30,
            defense = 18,
            speed = 15,
            xpReward = 500,
            goldReward = 1000,
            possibleDrops = listOf(ItemCatalog.ELIXIR_SUPREME),
            isBoss = true,
            aggroRadius = 10f
        )
        val altarChunk = worldManager.getChunk(worldManager.generator.artifactChunkX, worldManager.generator.artifactChunkY)
        altarChunk.enemies.add(boss)
        showNotification("¡El rugido del Titán resuena por todo el templo!")
    }

    // ==========================================
    // HYBRID COMBAT SYSTEM: TURN-BASED BATTLE
    // ==========================================

    private fun triggerCombatTransition(enemy: EnemyEntity) {
        audio.playSfx("battlestart")
        audio.playMusic(if (enemy.isBoss) MusicTrack.BOSS else MusicTrack.BATTLE)

        val playerCombatant = Combatant(
            id = "player",
            name = "Héroe de Aethelgard",
            isPlayer = true,
            currentHp = player.currentHp,
            maxHp = player.maxHp,
            currentMp = player.currentMp,
            maxMp = player.maxMp,
            attack = player.totalAttack,
            defense = player.totalDefense,
            speed = player.baseSpeed,
            spriteKey = "hero",
            skills = listOf(
                CombatSkillCatalog.POWER_SLASH,
                CombatSkillCatalog.ARCANE_BOLT,
                CombatSkillCatalog.HOLY_LIGHT,
                CombatSkillCatalog.BATTLE_CRY
            )
        )

        val enemyCombatant = Combatant(
            id = enemy.id,
            name = enemy.name,
            isPlayer = false,
            currentHp = enemy.currentHp,
            maxHp = enemy.maxHp,
            currentMp = 60,
            maxMp = 60,
            attack = enemy.attack,
            defense = enemy.defense,
            speed = enemy.speed,
            spriteKey = enemy.spriteKey,
            isBoss = enemy.isBoss,
            skills = if (enemy.isBoss) listOf(CombatSkillCatalog.BOSS_VOID_STORM) else listOf(CombatSkillCatalog.ENEMY_POISON_BITE)
        )

        // Calculate turn order sorted by speed
        val combatants = listOf(playerCombatant, enemyCombatant).sortedByDescending { it.speed }

        _battleState.value = BattleState(
            biome = enemy.biome,
            playerCombatant = playerCombatant,
            enemyCombatants = listOf(enemyCombatant),
            turnOrder = combatants,
            isPlayerTurn = (combatants.firstOrNull()?.isPlayer == true),
            battleLog = listOf("¡${enemy.name} bloquea el paso!"),
            isTransitioning = true
        )

        _currentScreen.value = GameScreen.BATTLE

        viewModelScope.launch {
            delay(500)
            _battleState.value = _battleState.value?.copy(isTransitioning = false)
            if (_battleState.value?.isPlayerTurn == false) {
                delay(600)
                executeEnemyTurn()
            }
        }
    }

    fun executePlayerAction(actionType: BattleActionType, skill: CombatSkill? = null, item: Item? = null) {
        val state = _battleState.value ?: return
        if (!state.isPlayerTurn || state.isVictory || state.isDefeat || state.isAttackAnimating) return

        val player = state.playerCombatant
        val target = state.enemyCombatants.firstOrNull { it.isAlive } ?: return

        when (actionType) {
            BattleActionType.ATTACK -> {
                audio.playSfx("attack")
                state.isAttackAnimating = true
                state.activeAttackerId = player.id
                state.activeTargetId = target.id

                val damage = calculateDamage(player.attack, target.defense)
                target.currentHp = (target.currentHp - damage).coerceAtLeast(0)

                addFloatingText("-$damage", 0xFFFFFFFF, target.isPlayer, isCritical = (Math.random() < 0.2))
                logBattle("¡Atacaste a ${target.name} infligiendo $damage de daño!")

                viewModelScope.launch {
                    delay(500)
                    state.isAttackAnimating = false
                    checkBattleResolution()
                }
            }

            BattleActionType.SKILL -> {
                if (skill == null) return
                if (player.currentMp < skill.mpCost) {
                    audio.playSfx("select")
                    showNotification("¡Maná insuficiente!")
                    return
                }

                player.currentMp -= skill.mpCost
                audio.playSfx("pickup")
                state.isAttackAnimating = true

                if (skill.targetSelf) {
                    if (skill.healAmount > 0) {
                        player.currentHp = (player.currentHp + skill.healAmount).coerceAtMost(player.maxHp)
                        addFloatingText("+${skill.healAmount}", 0xFF4CAF50, isPlayer = true)
                        logBattle("¡Usaste ${skill.name} y recuperaste ${skill.healAmount} HP!")
                    }
                    if (skill.inflictsStatus != null) {
                        player.activeEffects.add(skill.inflictsStatus to skill.inflictsStatus.turns)
                        logBattle("¡Activaste ${skill.name}! Efecto ${skill.inflictsStatus.displayName}.")
                    }
                } else {
                    val rawDmg = (player.attack * skill.damageMultiplier).toInt()
                    val damage = calculateDamage(rawDmg, if (skill.isMagical) target.defense / 2 else target.defense)
                    target.currentHp = (target.currentHp - damage).coerceAtLeast(0)
                    addFloatingText("-$damage", 0xFFFFD700, target.isPlayer, isCritical = true)
                    logBattle("¡Lanzaste ${skill.name} sobre ${target.name} (-$damage HP)!")
                }

                viewModelScope.launch {
                    delay(500)
                    state.isAttackAnimating = false
                    checkBattleResolution()
                }
            }

            BattleActionType.ITEM -> {
                if (item == null) return
                useItemInBattle(item)
            }

            BattleActionType.FLEE -> {
                val escapeChance = (player.speed.toFloat() / (player.speed + target.speed)) * 100f
                if (Math.random() * 100f < escapeChance && !target.isBoss) {
                    audio.playSfx("flee")
                    logBattle("¡Has escapado con éxito de la batalla!")
                    viewModelScope.launch {
                        delay(600)
                        resolveFleeBattle()
                    }
                } else {
                    audio.playSfx("damage")
                    logBattle("¡No pudiste escapar!")
                    viewModelScope.launch {
                        delay(600)
                        endPlayerTurn()
                    }
                }
            }
        }
    }

    private fun executeEnemyTurn() {
        val state = _battleState.value ?: return
        if (state.isVictory || state.isDefeat) return

        val enemy = state.enemyCombatants.firstOrNull { it.isAlive } ?: return
        val player = state.playerCombatant

        state.isAttackAnimating = true
        state.activeAttackerId = enemy.id
        state.activeTargetId = player.id

        // Simple Enemy AI
        val shouldUseSkill = enemy.skills.isNotEmpty() && (Math.random() < 0.35 || enemy.isBoss)
        if (shouldUseSkill) {
            val skill = enemy.skills.random()
            audio.playSfx("damage")
            val raw = (enemy.attack * skill.damageMultiplier).toInt()
            val damage = calculateDamage(raw, player.defense)
            player.currentHp = (player.currentHp - damage).coerceAtLeast(0)
            addFloatingText("-$damage", 0xFFFF5722, isPlayer = true)
            logBattle("¡${enemy.name} usó ${skill.name} contra ti (-$damage HP)!")
        } else {
            audio.playSfx("attack")
            val damage = calculateDamage(enemy.attack, player.defense)
            player.currentHp = (player.currentHp - damage).coerceAtLeast(0)
            addFloatingText("-$damage", 0xFFE53935, isPlayer = true)
            logBattle("¡${enemy.name} te asesta un golpe por $damage de daño!")
        }

        viewModelScope.launch {
            delay(600)
            state.isAttackAnimating = false
            // Sync player HP back to overworld player
            this@GameViewModel.player.currentHp = player.currentHp

            if (player.currentHp <= 0) {
                audio.playSfx("defeat")
                state.isDefeat = true
                delay(800)
                _currentScreen.value = GameScreen.GAME_OVER
            } else {
                state.isPlayerTurn = true
                state.roundNumber++
                _battleState.value = state.copy()
            }
        }
    }

    private fun checkBattleResolution() {
        val state = _battleState.value ?: return
        val enemiesAlive = state.enemyCombatants.any { it.isAlive }

        if (!enemiesAlive) {
            // Battle Victory!
            audio.playSfx("levelup")
            enemiesDefeatedCount++
            val enemy = state.enemyCombatants.first()
            worldManager.recordEnemyDefeated(enemy.id)

            val xp = if (enemy.isBoss) 600 else (35 + enemy.speed * 3)
            val gold = if (enemy.isBoss) 800 else (20 + enemy.attack * 2)
            player.gold += gold
            gainXp(xp)

            state.isVictory = true
            state.xpEarned = xp
            state.goldEarned = gold
            logBattle("¡Victoria! Obtuviste +$xp EXP y +$gold Oro.")

            if (enemy.isBoss) {
                player.hasDefeatedBoss = true
                viewModelScope.launch {
                    delay(1200)
                    _currentScreen.value = GameScreen.VICTORY
                }
            }
        } else {
            endPlayerTurn()
        }
    }

    private fun endPlayerTurn() {
        val state = _battleState.value ?: return
        state.isPlayerTurn = false
        _battleState.value = state.copy()
        viewModelScope.launch {
            delay(500)
            executeEnemyTurn()
        }
    }

    fun closeVictoryScreen() {
        _battleState.value = null
        _currentScreen.value = GameScreen.OVERWORLD
        updateMusicForCurrentState()
    }

    private fun resolveFleeBattle() {
        _battleState.value = null
        player.invulnerableTimer = 3.0f // 3 seconds invulnerability
        _currentScreen.value = GameScreen.OVERWORLD
        updateMusicForCurrentState()
        showNotification("Escapaste a salvo. ¡Tienes un breve periodo de invulnerabilidad!")
    }

    private fun updateBattleAnimation(delta: Float) {
        val state = _battleState.value ?: return
        if (state.floatingTexts.isNotEmpty()) {
            val updated = state.floatingTexts.mapNotNull { ft ->
                ft.offsetY -= delta * 30f
                ft.alpha -= delta * 1.2f
                if (ft.alpha > 0f) ft else null
            }
            state.floatingTexts = updated
            _battleState.value = state.copy()
        }
    }

    private fun calculateDamage(attack: Int, defense: Int): Int {
        val base = attack - defense
        val variation = ((Math.random() * 0.2) - 0.1).toFloat() // -10% to +10%
        val total = (base * (1.0f + variation)).toInt()
        return total.coerceAtLeast(1)
    }

    private fun addFloatingText(text: String, colorHex: Long, isPlayer: Boolean, isCritical: Boolean = false) {
        val state = _battleState.value ?: return
        val ft = FloatingText(
            text = text,
            colorHex = colorHex,
            isCritical = isCritical,
            startX = if (isPlayer) 100f else 220f,
            startY = if (isPlayer) 320f else 140f
        )
        state.floatingTexts = state.floatingTexts + ft
        _battleState.value = state.copy()
    }

    private fun logBattle(message: String) {
        val state = _battleState.value ?: return
        val updated = (state.battleLog + message).takeLast(4)
        _battleState.value = state.copy(battleLog = updated)
    }

    // ==========================================
    // INVENTORY, CRAFTING & PROGRESSION
    // ==========================================

    fun addItemToInventory(item: Item) {
        val existing = player.inventory.find { it.id == item.id }
        if (existing != null) {
            val index = player.inventory.indexOf(existing)
            player.inventory[index] = existing.copyWithCount(existing.stackCount + item.stackCount)
        } else {
            player.inventory.add(item)
        }
    }

    fun useItem(item: Item) {
        when (item.type) {
            ItemType.CONSUMABLE -> {
                audio.playSfx("pickup")
                if (item.hpRestore > 0) {
                    player.currentHp = (player.currentHp + item.hpRestore).coerceAtMost(player.maxHp)
                }
                if (item.mpRestore > 0) {
                    player.currentMp = (player.currentMp + item.mpRestore).coerceAtMost(player.maxMp)
                }
                removeItemFromInventory(item.id, 1)
                showNotification("Usaste ${item.name}.")
            }
            ItemType.WEAPON -> {
                audio.playSfx("select")
                val old = player.equippedWeapon
                player.equippedWeapon = item
                removeItemFromInventory(item.id, 1)
                addItemToInventory(old)
                showNotification("Equipaste ${item.name} (+${item.attackBonus} Atk).")
            }
            ItemType.ARMOR -> {
                audio.playSfx("select")
                val old = player.equippedArmor
                player.equippedArmor = item
                removeItemFromInventory(item.id, 1)
                addItemToInventory(old)
                showNotification("Equipaste ${item.name} (+${item.defenseBonus} Def).")
            }
            else -> {}
        }
    }

    private fun useItemInBattle(item: Item) {
        val state = _battleState.value ?: return
        val playerCombatant = state.playerCombatant
        if (item.hpRestore > 0) {
            playerCombatant.currentHp = (playerCombatant.currentHp + item.hpRestore).coerceAtMost(playerCombatant.maxHp)
            addFloatingText("+${item.hpRestore}", 0xFF4CAF50, isPlayer = true)
        }
        if (item.mpRestore > 0) {
            playerCombatant.currentMp = (playerCombatant.currentMp + item.mpRestore).coerceAtMost(playerCombatant.maxMp)
            addFloatingText("+${item.mpRestore} MP", 0xFF00E5FF, isPlayer = true)
        }
        removeItemFromInventory(item.id, 1)
        player.currentHp = playerCombatant.currentHp
        player.currentMp = playerCombatant.currentMp
        audio.playSfx("pickup")
        logBattle("¡Usaste ${item.name}!")

        viewModelScope.launch {
            delay(500)
            endPlayerTurn()
        }
    }

    fun discardItem(item: Item) {
        audio.playSfx("select")
        removeItemFromInventory(item.id, 1)
        showNotification("Descartaste ${item.name}.")
    }

    private fun removeItemFromInventory(itemId: String, quantity: Int) {
        val existing = player.inventory.find { it.id == itemId } ?: return
        if (existing.stackCount > quantity) {
            val idx = player.inventory.indexOf(existing)
            player.inventory[idx] = existing.copyWithCount(existing.stackCount - quantity)
        } else {
            player.inventory.remove(existing)
        }
    }

    fun craftRecipe(recipe: CraftingRecipe): Boolean {
        // Verify ingredients
        for (ing in recipe.ingredients) {
            val itemInInv = player.inventory.find { it.id == ing.itemId }
            if (itemInInv == null || itemInInv.stackCount < ing.quantity) {
                audio.playSfx("select")
                showNotification("No tienes suficientes materiales.")
                return false
            }
        }

        // Consume ingredients
        for (ing in recipe.ingredients) {
            removeItemFromInventory(ing.itemId, ing.quantity)
        }

        // Add crafted item
        val result = recipe.resultItem.copyWithCount(recipe.resultQuantity)
        addItemToInventory(result)
        audio.playSfx("pickup")
        showNotification("¡Fabricaste ${result.name}!")
        return true
    }

    fun gainXp(amount: Int) {
        player.xp += amount
        if (player.xp >= player.xpToNextLevel) {
            player.level++
            player.xp -= player.xpToNextLevel
            player.xpToNextLevel = (player.xpToNextLevel * 1.5f).toInt()
            player.maxHp += 20
            player.currentHp = player.maxHp
            player.maxMp += 10
            player.currentMp = player.maxMp
            player.baseStrength += 3
            player.baseDefense += 2
            player.baseSpeed += 1
            audio.playSfx("levelup")
            showNotification("¡SUBISTE DE NIVEL! Nivel ${player.level}")
        }
    }

    // ==========================================
    // AUDIO COORDINATION
    // ==========================================

    private fun updateMusicForCurrentState() {
        val track = when (_currentBiome.value) {
            Biome.RUINS -> MusicTrack.DUNGEON
            else -> MusicTrack.OVERWORLD
        }
        audio.playMusic(track)
    }

    fun showNotification(msg: String) {
        val n = GameNotification(message = msg)
        _notifications.value = _notifications.value + n
        viewModelScope.launch {
            delay(3000)
            _notifications.value = _notifications.value.filter { it.id != n.id }
        }
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt(dx * dx + dy * dy)
    }
}
