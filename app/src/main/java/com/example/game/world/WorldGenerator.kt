package com.example.game.world

import com.example.game.model.AnimalEntity
import com.example.game.model.Biome
import com.example.game.model.ChestEntity
import com.example.game.model.DecoType
import com.example.game.model.EnemyEntity
import com.example.game.model.Item
import com.example.game.model.ItemCatalog
import com.example.game.model.NpcEntity
import com.example.game.model.TileType
import java.util.Random

/**
 * Procedural World Generator creating continuous, seamless chunks using Simplex noise.
 */
class WorldGenerator(val seed: Long = 133742L) {
    private val elevationNoise = SimplexNoise(seed)
    private val moistureNoise = SimplexNoise(seed + 101L)
    private val detailNoise = SimplexNoise(seed + 999L)

    // Dedicated legendary artifact coordinates
    val artifactChunkX = 10
    val artifactChunkY = 8

    /**
     * Determine the biome for a given world tile coordinate.
     */
    fun getBiomeAt(worldTileX: Int, worldTileY: Int): Biome {
        // Legendary artifact ruin zone
        val chunkX = Math.floorDiv(worldTileX, CHUNK_SIZE)
        val chunkY = Math.floorDiv(worldTileY, CHUNK_SIZE)
        if (chunkX == artifactChunkX && chunkY == artifactChunkY) {
            return Biome.RUINS
        }

        val scale = 0.005f
        val elevation = elevationNoise.fbm(worldTileX * scale, worldTileY * scale, octaves = 3)
        val moisture = moistureNoise.fbm(worldTileX * scale, worldTileY * scale, octaves = 3)

        return when {
            elevation > 0.72f -> Biome.MOUNTAIN
            moisture < 0.28f -> Biome.DESERT
            moisture > 0.70f && elevation < 0.50f -> Biome.SWAMP
            elevation > 0.58f && moisture < 0.45f -> Biome.RUINS
            moisture > 0.48f -> Biome.FOREST
            else -> Biome.PLAINS
        }
    }

    /**
     * Generates a complete 32x32 chunk.
     */
    fun generateChunk(chunkX: Int, chunkY: Int): Chunk {
        val chunkSeed = (seed xor (chunkX.toLong() shl 32) xor chunkY.toLong())
        val rng = Random(chunkSeed)

        val isArtifactChunk = (chunkX == artifactChunkX && chunkY == artifactChunkY)
        val centerBiome = if (isArtifactChunk) Biome.RUINS else getBiomeAt(chunkX * CHUNK_SIZE + 16, chunkY * CHUNK_SIZE + 16)

        val tiles = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { TileType.GRASS } }
        val decos = Array(CHUNK_SIZE) { Array(CHUNK_SIZE) { DecoType.NONE } }

        val enemies = mutableListOf<EnemyEntity>()
        val animals = mutableListOf<AnimalEntity>()
        val chests = mutableListOf<ChestEntity>()
        val npcs = mutableListOf<NpcEntity>()

        // 1. Generate Tiles & Decos
        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_SIZE) {
                val worldTileX = chunkX * CHUNK_SIZE + x
                val worldTileY = chunkY * CHUNK_SIZE + y
                val biome = if (isArtifactChunk) Biome.RUINS else getBiomeAt(worldTileX, worldTileY)
                val detail = detailNoise.eval(worldTileX * 0.1f, worldTileY * 0.1f)

                when (biome) {
                    Biome.FOREST -> {
                        tiles[x][y] = if (detail < 0.25f) TileType.DIRT else TileType.GRASS
                        if (rng.nextFloat() < 0.12f) {
                            decos[x][y] = if (rng.nextBoolean()) DecoType.TREE_OAK else DecoType.TREE_PINE
                        } else if (rng.nextFloat() < 0.08f) {
                            decos[x][y] = if (rng.nextBoolean()) DecoType.BUSH else DecoType.FLOWER_RED
                        }
                    }
                    Biome.PLAINS -> {
                        tiles[x][y] = TileType.GRASS
                        if (rng.nextFloat() < 0.03f) {
                            decos[x][y] = DecoType.TREE_OAK
                        } else if (rng.nextFloat() < 0.10f) {
                            decos[x][y] = if (rng.nextBoolean()) DecoType.FLOWER_BLUE else DecoType.FLOWER_RED
                        } else if (rng.nextFloat() < 0.02f) {
                            decos[x][y] = DecoType.ROCK_BOULDER
                        }
                    }
                    Biome.DESERT -> {
                        tiles[x][y] = TileType.SAND
                        if (rng.nextFloat() < 0.04f) {
                            decos[x][y] = DecoType.CACTUS
                        } else if (rng.nextFloat() < 0.02f) {
                            decos[x][y] = DecoType.ROCK_BOULDER
                        }
                    }
                    Biome.MOUNTAIN -> {
                        tiles[x][y] = if (detail > 0.45f) TileType.ROCK_GROUND else TileType.CLIFF
                        if (tiles[x][y] == TileType.ROCK_GROUND && rng.nextFloat() < 0.08f) {
                            decos[x][y] = DecoType.ROCK_BOULDER
                        } else if (rng.nextFloat() < 0.03f) {
                            decos[x][y] = DecoType.TREE_PINE
                        }
                    }
                    Biome.SWAMP -> {
                        tiles[x][y] = if (detail < 0.35f) TileType.WATER else TileType.SWAMP_MUD
                        if (tiles[x][y] == TileType.SWAMP_MUD) {
                            if (rng.nextFloat() < 0.07f) decos[x][y] = DecoType.DEAD_TREE
                            else if (rng.nextFloat() < 0.05f) decos[x][y] = DecoType.BUSH
                        }
                    }
                    Biome.RUINS -> {
                        tiles[x][y] = if (detail < 0.70f) TileType.RUINS_STONE else TileType.DIRT
                        if (rng.nextFloat() < 0.09f) {
                            decos[x][y] = DecoType.RUINS_PILLAR
                        } else if (rng.nextFloat() < 0.04f) {
                            decos[x][y] = DecoType.ROCK_BOULDER
                        }
                    }
                }
            }
        }

        // Special handling for the Artifact Chunk
        if (isArtifactChunk) {
            // Build an ancient stone sanctuary chamber in center
            for (x in 10..22) {
                for (y in 10..22) {
                    tiles[x][y] = TileType.RUINS_STONE
                    decos[x][y] = DecoType.NONE
                }
            }
            // Pillars framing altar
            decos[11][11] = DecoType.RUINS_PILLAR
            decos[21][11] = DecoType.RUINS_PILLAR
            decos[11][21] = DecoType.RUINS_PILLAR
            decos[21][21] = DecoType.RUINS_PILLAR

            // The sacred altar
            decos[16][16] = DecoType.RUINS_ALTAR

            // Chest with legendary artifact
            chests.add(
                ChestEntity(
                    id = "artifact_chest_${chunkX}_${chunkY}",
                    worldX = (chunkX * CHUNK_SIZE + 16).toFloat(),
                    worldY = (chunkY * CHUNK_SIZE + 17).toFloat(),
                    loot = listOf(ItemCatalog.ANCIENT_ARTIFACT, ItemCatalog.ELIXIR_SUPREME),
                    goldAmount = 500
                )
            )

            // Guardian Wraiths protecting the artifact
            enemies.add(
                createEnemy(
                    id = "guardian_wraith_1",
                    worldX = (chunkX * CHUNK_SIZE + 13).toFloat(),
                    worldY = (chunkY * CHUNK_SIZE + 16).toFloat(),
                    biome = Biome.RUINS,
                    levelTier = 3
                )
            )
            enemies.add(
                createEnemy(
                    id = "guardian_wraith_2",
                    worldX = (chunkX * CHUNK_SIZE + 19).toFloat(),
                    worldY = (chunkY * CHUNK_SIZE + 16).toFloat(),
                    biome = Biome.RUINS,
                    levelTier = 3
                )
            )
        } else {
            // Normal chunk entity population
            // 2. Chests (15% chance per chunk)
            if (rng.nextFloat() < 0.20f) {
                val cx = rng.nextInt(CHUNK_SIZE - 4) + 2
                val cy = rng.nextInt(CHUNK_SIZE - 4) + 2
                if (tiles[cx][cy].isWalkable && decos[cx][cy] == DecoType.NONE) {
                    val chestLoot = when (centerBiome) {
                        Biome.RUINS -> listOf(ItemCatalog.SWORD_RUNIC, ItemCatalog.POTION_HP, ItemCatalog.MAGIC_CRYSTAL)
                        Biome.MOUNTAIN -> listOf(ItemCatalog.ARMOR_STEEL, ItemCatalog.IRON_ORE.copyWithCount(3))
                        Biome.DESERT -> listOf(ItemCatalog.POTION_MP.copyWithCount(2), ItemCatalog.MAGIC_CRYSTAL)
                        else -> listOf(ItemCatalog.POTION_HP.copyWithCount(2), ItemCatalog.HERB.copyWithCount(3))
                    }
                    chests.add(
                        ChestEntity(
                            id = "chest_${chunkX}_${chunkY}_${cx}_${cy}",
                            worldX = (chunkX * CHUNK_SIZE + cx).toFloat(),
                            worldY = (chunkY * CHUNK_SIZE + cy).toFloat(),
                            loot = chestLoot,
                            goldAmount = rng.nextInt(35) + 15
                        )
                    )
                }
            }

            // 3. Enemies (1-3 enemies per chunk, except at initial spawn (0,0))
            if (chunkX != 0 || chunkY != 0) {
                val enemyCount = rng.nextInt(2) + 1
                for (i in 0 until enemyCount) {
                    val ex = rng.nextInt(CHUNK_SIZE - 6) + 3
                    val ey = rng.nextInt(CHUNK_SIZE - 6) + 3
                    if (tiles[ex][ey].isWalkable && decos[ex][ey] == DecoType.NONE) {
                        val enemy = createEnemy(
                            id = "enemy_${chunkX}_${chunkY}_$i",
                            worldX = (chunkX * CHUNK_SIZE + ex).toFloat(),
                            worldY = (chunkY * CHUNK_SIZE + ey).toFloat(),
                            biome = centerBiome,
                            levelTier = (Math.abs(chunkX) + Math.abs(chunkY)).coerceIn(1, 4)
                        )
                        enemies.add(enemy)
                    }
                }
            }

            // 4. Passive Animals (deer, sheep, frogs in friendly/wild biomes)
            if (centerBiome == Biome.FOREST || centerBiome == Biome.PLAINS || centerBiome == Biome.SWAMP) {
                val animalCount = rng.nextInt(3)
                for (i in 0 until animalCount) {
                    val ax = rng.nextInt(CHUNK_SIZE - 4) + 2
                    val ay = rng.nextInt(CHUNK_SIZE - 4) + 2
                    if (tiles[ax][ay].isWalkable && decos[ax][ay] == DecoType.NONE) {
                        val animalName = when (centerBiome) {
                            Biome.SWAMP -> "Rana Esmeralda"
                            Biome.FOREST -> "Ciervo Veloz"
                            else -> "Carnero Lanudo"
                        }
                        val animalKey = when (centerBiome) {
                            Biome.SWAMP -> "frog"
                            Biome.FOREST -> "deer"
                            else -> "sheep"
                        }
                        animals.add(
                            AnimalEntity(
                                id = "animal_${chunkX}_${chunkY}_$i",
                                worldX = (chunkX * CHUNK_SIZE + ax).toFloat(),
                                worldY = (chunkY * CHUNK_SIZE + ay).toFloat(),
                                name = animalName,
                                spriteKey = animalKey,
                                drops = listOf(ItemCatalog.LEATHER.copyWithCount(2), ItemCatalog.HERB)
                            )
                        )
                    }
                }
            }

            // 5. NPCs (Spawn an Elder NPC in chunk (0,0) and wandering scholars elsewhere)
            if (chunkX == 0 && chunkY == 0) {
                npcs.add(
                    NpcEntity(
                        id = "npc_elder_alden",
                        worldX = 4f,
                        worldY = 4f,
                        name = "Sabio Alden",
                        title = "Guardián de la Orden",
                        spriteKey = "npc_wizard",
                        dialogues = listOf(
                            "¡Saludos, valiente aventurero! El equilibrio de Aethelgard se desmorona.",
                            "En las Ruinas Olvidadas hacia el este (Sector 10, 8) reposa el Orbe del Sol Ancestral.",
                            "Pero ten cuidado: el Titán del Abismo despertará al desellar el orbe. ¡Equípate y forja armas poderosas!"
                        )
                    )
                )
            } else if (rng.nextFloat() < 0.12f) {
                val nx = rng.nextInt(CHUNK_SIZE - 6) + 3
                val ny = rng.nextInt(CHUNK_SIZE - 6) + 3
                if (tiles[nx][ny].isWalkable && decos[nx][ny] == DecoType.NONE) {
                    npcs.add(
                        NpcEntity(
                            id = "npc_${chunkX}_${chunkY}",
                            worldX = (chunkX * CHUNK_SIZE + nx).toFloat(),
                            worldY = (chunkY * CHUNK_SIZE + ny).toFloat(),
                            name = "Explorador Roland",
                            title = "Viajero de Tierras Lejanas",
                            spriteKey = "npc_scout",
                            dialogues = listOf(
                                "El clima en esta región es impredecible. Recoge hierbas y minerales para fabricar pociones y equipo.",
                                "Usa el botón B para esquivar o canalizar habilidades arcanas en combate.",
                                "He oído que el Titán ancestral aguarda más allá de las brumas una vez obtenido el artefacto."
                            )
                        )
                    )
                }
            }
        }

        return Chunk(
            chunkX = chunkX,
            chunkY = chunkY,
            primaryBiome = centerBiome,
            tiles = tiles,
            decos = decos,
            enemies = enemies,
            animals = animals,
            chests = chests,
            npcs = npcs,
            isDungeonArtifactChunk = isArtifactChunk
        )
    }

    private fun createEnemy(id: String, worldX: Float, worldY: Float, biome: Biome, levelTier: Int): EnemyEntity {
        val (name, spriteKey, baseHp, baseAtk, baseDef, baseSpd, xp, gold, drops) = when (biome) {
            Biome.FOREST -> EnemyTemplate("Lobo Sombrío", "enemy_wolf", 50, 14, 6, 14, 30, 15, listOf(ItemCatalog.LEATHER, ItemCatalog.HERB))
            Biome.PLAINS -> EnemyTemplate("Goblin Saqueador", "enemy_goblin", 45, 12, 5, 12, 25, 20, listOf(ItemCatalog.WOOD, ItemCatalog.POTION_HP))
            Biome.DESERT -> EnemyTemplate("Escorpión de Dunas", "enemy_scorpion", 70, 18, 10, 11, 45, 25, listOf(ItemCatalog.IRON_ORE, ItemCatalog.POTION_MP))
            Biome.MOUNTAIN -> EnemyTemplate("Gólem Rocoso", "enemy_golem", 110, 22, 16, 7, 70, 40, listOf(ItemCatalog.IRON_ORE.copyWithCount(2), ItemCatalog.MAGIC_CRYSTAL))
            Biome.SWAMP -> EnemyTemplate("Cocodrilo Fangoso", "enemy_croc", 85, 20, 12, 10, 55, 30, listOf(ItemCatalog.LEATHER, ItemCatalog.HERB))
            Biome.RUINS -> EnemyTemplate("Espectro Errante", "enemy_wraith", 95, 24, 14, 13, 80, 50, listOf(ItemCatalog.MAGIC_CRYSTAL, ItemCatalog.ELIXIR_SUPREME))
        }

        val multiplier = 1.0f + (levelTier - 1) * 0.25f
        return EnemyEntity(
            id = id,
            worldX = worldX,
            worldY = worldY,
            name = name,
            biome = biome,
            spriteKey = spriteKey,
            maxHp = (baseHp * multiplier).toInt(),
            currentHp = (baseHp * multiplier).toInt(),
            attack = (baseAtk * multiplier).toInt(),
            defense = (baseDef * multiplier).toInt(),
            speed = baseSpd,
            xpReward = (xp * multiplier).toInt(),
            goldReward = (gold * multiplier).toInt(),
            possibleDrops = drops
        )
    }

    private data class EnemyTemplate(
        val name: String,
        val spriteKey: String,
        val hp: Int,
        val atk: Int,
        val def: Int,
        val spd: Int,
        val xp: Int,
        val gold: Int,
        val drops: List<Item>
    )
}
