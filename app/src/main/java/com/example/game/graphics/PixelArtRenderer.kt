package com.example.game.graphics

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.game.model.DecoType
import com.example.game.model.Direction
import com.example.game.model.TileType
import java.util.concurrent.ConcurrentHashMap

/**
 * Authentic Pokémon Game Boy & Game Boy Color (Gen 1 & Gen 2) Pixel Art Generator.
 * Recreates the exact, iconic handheld visual aesthetic of Pokémon Red, Blue, Yellow, Gold & Silver:
 * - 16x16 / 32x32 retro tiles: Route 1 tall grass, trees, ledges, dirt paths, water waves
 * - Iconic Pokémon Trainer "Red" with red cap, white logo, backpack, and 4-way walk cycles
 * - Trainer battle back-sprite (view from behind throwing a Poké Ball)
 * - Authentic Pokémon creatures (Pikaspark, Flameling, Aquaturtle, Rockfist, Shadowghoul, Titan Dragon)
 * - Ground item Poké Balls (red/white with black equator band and center button)
 * - Razor-sharp 1-pixel black outlines and signature Game Boy Color palettes
 */
object PixelArtRenderer {
    private val spriteCache = ConcurrentHashMap<String, ImageBitmap>()

    // Authentic Game Boy Color Pokémon Palettes
    private const val C_TRANS = 0x00000000
    private const val C_BLACK = 0xFF080808.toInt()
    private const val C_WHITE = 0xFFF8F8F8.toInt()

    // Trainer Colors (Red cap, white collar, blue jacket, jeans, backpack)
    private const val C_TRAINER_RED = 0xFFE03838.toInt()
    private const val C_TRAINER_DARK_RED = 0xFF981818.toInt()
    private const val C_TRAINER_BLUE = 0xFF2850A8.toInt()
    private const val C_TRAINER_DARK_BLUE = 0xFF102868.toInt()
    private const val C_SKIN = 0xFFF8B890.toInt()
    private const val C_SKIN_SHADOW = 0xFFD88860.toInt()
    private const val C_HAIR_DARK = 0xFF201818.toInt()
    private const val C_BACKPACK = 0xFFE89820.toInt()
    private const val C_BACKPACK_DARK = 0xFFB06810.toInt()
    private const val C_JEANS = 0xFF204070.toInt()

    // Pokémon Route 1 Nature Palette (Iconic Game Boy Color Greens)
    private const val C_GRASS_BASE = 0xFF58B058.toInt()
    private const val C_GRASS_LIGHT = 0xFF88D880.toInt()
    private const val C_GRASS_DARK = 0xFF186818.toInt()
    private const val C_TALL_GRASS_BLADE = 0xFF104810.toInt()

    // Dirt Path Palette
    private const val C_DIRT_BASE = 0xFFD8A868.toInt()
    private const val C_DIRT_LIGHT = 0xFFF0C888.toInt()
    private const val C_DIRT_DARK = 0xFF885830.toInt()

    // Water Palette
    private const val C_WATER_BASE = 0xFF4888E0.toInt()
    private const val C_WATER_LIGHT = 0xFF88C8F8.toInt()
    private const val C_WATER_DARK = 0xFF184898.toInt()

    // Mountain & Ledge Palette
    private const val C_ROCK_BASE = 0xFF908078.toInt()
    private const val C_ROCK_LIGHT = 0xFFB8A898.toInt()
    private const val C_ROCK_DARK = 0xFF584840.toInt()

    // Ruins / Cave Palette
    private const val C_RUINS_BASE = 0xFF786888.toInt()
    private const val C_RUINS_LIGHT = 0xFFA090B0.toInt()
    private const val C_RUINS_DARK = 0xFF483858.toInt()

    // Signature Accent Colors
    private const val C_PKMN_YELLOW = 0xFFF8B800.toInt()
    private const val C_PKMN_RED = 0xFFE03030.toInt()
    private const val C_PKMN_PURPLE = 0xFF8048A8.toInt()
    private const val C_PKMN_ORANGE = 0xFFF07020.toInt()

    fun getTileBitmap(tileType: TileType): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_tile_${tileType.name}") {
            createPokemonTile(tileType).asImageBitmap()
        }
    }

    fun getDecoBitmap(decoType: DecoType): ImageBitmap? {
        if (decoType == DecoType.NONE) return null
        return spriteCache.computeIfAbsent("pkmn_deco_${decoType.name}") {
            createPokemonDeco(decoType).asImageBitmap()
        }
    }

    fun getPlayerBitmap(direction: Direction, walkFrame: Int, isAttacking: Boolean): ImageBitmap {
        val key = if (isAttacking) "pkmn_trainer_throw_${direction.name}" else "pkmn_trainer_${direction.name}_$walkFrame"
        return spriteCache.computeIfAbsent(key) {
            createPokemonTrainer(direction, walkFrame, isAttacking).asImageBitmap()
        }
    }

    fun getTrainerBackSprite(isThrowing: Boolean): ImageBitmap {
        val key = if (isThrowing) "pkmn_trainer_back_throw" else "pkmn_trainer_back_idle"
        return spriteCache.computeIfAbsent(key) {
            createPokemonTrainerBackSprite(isThrowing).asImageBitmap()
        }
    }

    fun getEnemyOverworldBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_ow_$spriteKey") {
            createPokemonMonster(spriteKey, size = 32).asImageBitmap()
        }
    }

    fun getEnemyBattleBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_bt_$spriteKey") {
            createPokemonMonster(spriteKey, size = 64).asImageBitmap()
        }
    }

    fun getAnimalBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_animal_$spriteKey") {
            createPokemonAnimal(spriteKey).asImageBitmap()
        }
    }

    fun getNpcBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_npc_$spriteKey") {
            createPokemonNpc(spriteKey).asImageBitmap()
        }
    }

    fun getChestBitmap(isOpen: Boolean): ImageBitmap {
        val key = if (isOpen) "pkmn_ball_open" else "pkmn_ball_closed"
        return spriteCache.computeIfAbsent(key) {
            createPokemonItemBall(isOpen).asImageBitmap()
        }
    }

    fun getItemIconBitmap(iconKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("pkmn_icon_$iconKey") {
            createPokemonItemIcon(iconKey).asImageBitmap()
        }
    }

    // ==========================================
    // POKÉMON TILES (32x32 with Game Boy styling)
    // ==========================================

    private fun createPokemonTile(tileType: TileType): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32)

        when (tileType) {
            TileType.GRASS -> {
                // Classic Pokémon Route Grass: base emerald with alternating blade tufts
                pixels.fill(C_GRASS_BASE)
                // Distinct blade patterns at (6, 6), (22, 8), (12, 20), (26, 24)
                val bladeTufts = listOf(6 to 6, 22 to 8, 12 to 20, 26 to 24)
                for ((bx, by) in bladeTufts) {
                    for (dy in 0..4) {
                        for (dx in -1..1) {
                            val px = (bx + dx).coerceIn(0, 31)
                            val py = (by + dy).coerceIn(0, 31)
                            pixels[py * 32 + px] = if (dy == 0) C_GRASS_LIGHT else C_GRASS_DARK
                        }
                    }
                }
                // Subtle 16x16 grid corner dots
                pixels[0 * 32 + 0] = C_GRASS_DARK
                pixels[16 * 32 + 16] = C_GRASS_DARK
            }
            TileType.DIRT, TileType.SAND -> {
                // Pokémon dirt path / sand with round pebble accents
                val base = if (tileType == TileType.DIRT) C_DIRT_BASE else 0xFFE0C870.toInt()
                val dark = if (tileType == TileType.DIRT) C_DIRT_DARK else 0xFFB89840.toInt()
                pixels.fill(base)
                // Subtle pebble specks
                for (y in 0 until 32 step 8) {
                    for (x in 0 until 32 step 8) {
                        if ((x * 3 + y * 7) % 5 == 0) {
                            pixels[y * 32 + x] = dark
                            pixels[y * 32 + ((x + 1) % 32)] = C_WHITE
                        }
                    }
                }
            }
            TileType.WATER, TileType.DEEP_WATER -> {
                // Iconic Pokémon animated blue water with horizontal wave crests
                pixels.fill(C_WATER_BASE)
                // Wave ripple lines at y=6, 12, 20, 26
                for (w in listOf(6, 18)) {
                    for (x in 4..14) pixels[w * 32 + x] = C_WHITE
                    for (x in 4..14) pixels[(w + 1) * 32 + x] = C_WATER_LIGHT
                    for (x in 18..28) pixels[(w + 4) * 32 + x] = C_WHITE
                    for (x in 18..28) pixels[(w + 5) * 32 + x] = C_WATER_LIGHT
                }
            }
            TileType.ROCK_GROUND, TileType.CLIFF -> {
                // Pokémon Rock Tunnel / Ledge cliff
                pixels.fill(C_ROCK_BASE)
                // Ledge lip border at top
                for (x in 0 until 32) {
                    pixels[0 * 32 + x] = C_GRASS_BASE
                    pixels[1 * 32 + x] = C_BLACK
                    pixels[31 * 32 + x] = C_ROCK_DARK
                }
                for (y in 2 until 31) {
                    pixels[y * 32 + 0] = C_ROCK_DARK
                    pixels[y * 32 + 31] = C_ROCK_LIGHT
                }
            }
            TileType.SWAMP_MUD -> {
                pixels.fill(0xFF485840.toInt())
                // Murky bubbles
                for (i in listOf(8 to 10, 20 to 18, 14 to 26)) {
                    val (bx, by) = i
                    pixels[by * 32 + bx] = 0xFF688050.toInt()
                    pixels[(by + 1) * 32 + bx] = 0xFF283820.toInt()
                }
            }
            TileType.RUINS_STONE -> {
                // Pokémon Ruins of Alph lavender stone with grid bricks
                pixels.fill(C_RUINS_BASE)
                for (x in 0 until 32) {
                    pixels[0 * 32 + x] = C_RUINS_LIGHT
                    pixels[15 * 32 + x] = C_RUINS_DARK
                    pixels[31 * 32 + x] = C_RUINS_DARK
                }
                for (y in 0..15) pixels[y * 32 + 15] = C_RUINS_DARK
                for (y in 16..31) pixels[y * 32 + 7] = C_RUINS_DARK
                for (y in 16..31) pixels[y * 32 + 23] = C_RUINS_DARK
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    // ==========================================
    // POKÉMON DECORATIONS (Trees, Flowers, Rocks)
    // ==========================================

    private fun createPokemonDeco(decoType: DecoType): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        when (decoType) {
            DecoType.TREE_OAK, DecoType.TREE_PINE -> {
                // Iconic Pokémon round fluffy tree!
                // 1. Trunk (y: 20 to 30, x: 12 to 19)
                for (y in 20..30) {
                    for (x in 12..19) {
                        pixels[y * 32 + x] = if (x == 12 || x == 19) C_BLACK else if (x in 13..15) C_DIRT_LIGHT else C_DIRT_DARK
                    }
                }

                // 2. Round layered green foliage canopy (y: 2 to 22, x: 4 to 27)
                for (y in 2..22) {
                    for (x in 4..27) {
                        val dx = (x - 15.5f)
                        val dy = (y - 12f)
                        val distSq = dx * dx + dy * dy
                        if (distSq < 110) {
                            val isOutline = distSq > 88
                            if (isOutline) {
                                pixels[y * 32 + x] = C_BLACK
                            } else {
                                // Pokémon circular highlight on top-left
                                val isHighlight = (x in 8..15 && y in 4..10) || ((dx + 3) * (dx + 3) + (dy + 3) * (dy + 3) < 25)
                                val isShadow = (x > 18 || y > 16)
                                pixels[y * 32 + x] = if (isHighlight) C_GRASS_LIGHT else if (isShadow) C_GRASS_DARK else C_GRASS_BASE
                            }
                        }
                    }
                }
            }
            DecoType.BUSH -> {
                // Cuttable Pokémon shrub / Tall Grass Patch
                for (y in 10..28) {
                    for (x in 4..27) {
                        val isGrassBlade = (x % 3 == 0) && y in 10..18
                        if (isGrassBlade) {
                            pixels[y * 32 + x] = C_TALL_GRASS_BLADE
                        } else {
                            val dx = x - 15.5f
                            val dy = y - 20f
                            if (dx * dx + dy * dy < 70) {
                                val isOutline = dx * dx + dy * dy > 50
                                pixels[y * 32 + x] = if (isOutline) C_BLACK else if (dx < 0 && dy < 0) C_GRASS_LIGHT else C_GRASS_BASE
                            }
                        }
                    }
                }
            }
            DecoType.FLOWER_RED, DecoType.FLOWER_BLUE -> {
                // Pokémon 4-petal blossom patch
                val petalColor = if (decoType == DecoType.FLOWER_RED) C_PKMN_RED else C_WATER_LIGHT
                val centers = listOf(10 to 14, 20 to 18, 14 to 24)
                for ((cx, cy) in centers) {
                    // Center yellow
                    pixels[cy * 32 + cx] = C_PKMN_YELLOW
                    // Petals
                    pixels[(cy - 1) * 32 + cx] = petalColor
                    pixels[(cy + 1) * 32 + cx] = petalColor
                    pixels[cy * 32 + (cx - 1)] = petalColor
                    pixels[cy * 32 + (cx + 1)] = petalColor
                    // Outline dots
                    pixels[(cy - 1) * 32 + (cx - 1)] = C_BLACK
                    pixels[(cy - 1) * 32 + (cx + 1)] = C_BLACK
                    pixels[(cy + 1) * 32 + (cx - 1)] = C_BLACK
                    pixels[(cy + 1) * 32 + (cx + 1)] = C_BLACK
                }
            }
            DecoType.ROCK_BOULDER -> {
                // Pokémon Strength Boulder
                for (y in 8..28) {
                    for (x in 6..25) {
                        val dx = x - 15.5f
                        val dy = y - 18f
                        if (dx * dx + dy * dy < 80) {
                            val isOutline = dx * dx + dy * dy > 60
                            pixels[y * 32 + x] = if (isOutline) C_BLACK else if (dx < -1 && dy < -1) C_ROCK_LIGHT else if (dx > 2 || dy > 2) C_ROCK_DARK else C_ROCK_BASE
                        }
                    }
                }
            }
            DecoType.RUINS_PILLAR -> {
                // Ancient stone pillar with Unown style inscriptions
                for (y in 4..28) {
                    for (x in 10..21) {
                        val isEdge = x == 10 || x == 21 || y == 4 || y == 28
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else if (x < 15) C_RUINS_LIGHT else C_RUINS_DARK
                    }
                }
                // Eye glyph
                pixels[12 * 32 + 15] = C_BLACK
                pixels[12 * 32 + 16] = C_PKMN_YELLOW
                pixels[16 * 32 + 15] = C_BLACK
            }
            DecoType.RUINS_ALTAR -> {
                // Sacred Stone Shrine pedestal with glowing Master Poké-Orb
                for (y in 14..28) {
                    for (x in 6..25) {
                        val isBorder = x == 6 || x == 25 || y == 14 || y == 28
                        pixels[y * 32 + x] = if (isBorder) C_BLACK else if (y < 18) C_RUINS_LIGHT else C_RUINS_DARK
                    }
                }
                // Glowing Sun Orb in center
                for (y in 4..12) {
                    for (x in 12..19) {
                        val d = (x - 15.5f) * (x - 15.5f) + (y - 8f) * (y - 8f)
                        if (d < 16) {
                            pixels[y * 32 + x] = if (d > 10) C_BLACK else if (d < 4) C_WHITE else C_PKMN_YELLOW
                        }
                    }
                }
            }
            else -> {
                // Default bush
                for (y in 14..28) {
                    for (x in 8..23) pixels[y * 32 + x] = C_GRASS_DARK
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    // ==========================================
    // POKÉMON TRAINER "RED" SPRITE (32x32)
    // ==========================================

    private fun createPokemonTrainer(direction: Direction, walkFrame: Int, isAttacking: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        val stepOffset = if (walkFrame == 1) 2 else if (walkFrame == 2) -2 else 0

        when (direction) {
            Direction.DOWN -> {
                // 1. CAP (Red with white semi-circle logo on front)
                for (y in 4..9) {
                    for (x in 11..20) {
                        val isEdge = x == 11 || x == 20 || y == 4
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else C_TRAINER_RED
                    }
                }
                // Cap Visor (front shade)
                for (x in 10..21) pixels[10 * 32 + x] = C_BLACK
                // White logo on cap
                pixels[6 * 32 + 15] = C_WHITE
                pixels[6 * 32 + 16] = C_WHITE
                pixels[7 * 32 + 15] = C_WHITE
                pixels[7 * 32 + 16] = C_WHITE

                // 2. HAIR tufts on sides
                pixels[9 * 32 + 10] = C_HAIR_DARK
                pixels[10 * 32 + 9] = C_HAIR_DARK
                pixels[9 * 32 + 21] = C_HAIR_DARK
                pixels[10 * 32 + 22] = C_HAIR_DARK

                // 3. FACE & ANIME EYES
                for (y in 11..15) {
                    for (x in 11..20) {
                        val isEdge = x == 11 || x == 20 || y == 15
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else C_SKIN
                    }
                }
                // Big anime eyes
                pixels[12 * 32 + 13] = C_BLACK
                pixels[13 * 32 + 13] = C_BLACK
                pixels[12 * 32 + 18] = C_BLACK
                pixels[13 * 32 + 18] = C_BLACK
                // Catchlight
                pixels[12 * 32 + 14] = C_WHITE
                pixels[12 * 32 + 19] = C_WHITE

                // 4. JACKET / T-SHIRT (Red/Blue with white collar)
                for (y in 16..22) {
                    for (x in 9..22) {
                        val isEdge = x == 9 || x == 22 || y == 22
                        if (isEdge) {
                            pixels[y * 32 + x] = C_BLACK
                        } else if (x in 14..17) {
                            pixels[y * 32 + x] = if (y in 16..17) C_WHITE else C_BLACK // undershirt
                        } else {
                            pixels[y * 32 + x] = C_TRAINER_RED
                        }
                    }
                }
                // Backpack straps (yellow/orange)
                pixels[17 * 32 + 11] = C_BACKPACK
                pixels[18 * 32 + 11] = C_BACKPACK
                pixels[17 * 32 + 20] = C_BACKPACK
                pixels[18 * 32 + 20] = C_BACKPACK

                // 5. JEANS & SNEAKERS with walk stepping
                for (y in 23..28) {
                    // Left leg
                    val ly = (y + if (stepOffset > 0) -1 else 0)
                    for (x in 11..14) pixels[ly * 32 + x] = if (x == 11 || x == 14) C_BLACK else if (y < 26) C_JEANS else C_WHITE
                    // Right leg
                    val ry = (y + if (stepOffset < 0) -1 else 0)
                    for (x in 17..20) pixels[ry * 32 + x] = if (x == 17 || x == 20) C_BLACK else if (y < 26) C_JEANS else C_WHITE
                }
                // Red sneaker soles
                pixels[28 * 32 + 12] = C_TRAINER_RED
                pixels[28 * 32 + 13] = C_TRAINER_RED
                pixels[28 * 32 + 18] = C_TRAINER_RED
                pixels[28 * 32 + 19] = C_TRAINER_RED
            }
            Direction.UP -> {
                // Facing Away: Back of red cap and big yellow backpack
                for (y in 4..10) {
                    for (x in 11..20) {
                        val isEdge = x == 11 || x == 20 || y == 4
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else C_TRAINER_RED
                    }
                }
                // Hair nape
                for (x in 12..19) pixels[11 * 32 + x] = C_HAIR_DARK
                for (x in 13..18) pixels[12 * 32 + x] = C_HAIR_DARK

                // Large yellow backpack on back!
                for (y in 13..21) {
                    for (x in 10..21) {
                        val isEdge = x == 10 || x == 21 || y == 13 || y == 21
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else C_BACKPACK
                    }
                }
                pixels[16 * 32 + 15] = C_BLACK // backpack zipper
                pixels[16 * 32 + 16] = C_BLACK

                // Jeans & sneakers
                for (y in 22..28) {
                    val ly = (y + if (stepOffset > 0) -1 else 0)
                    for (x in 11..14) pixels[ly * 32 + x] = if (x == 11 || x == 14) C_BLACK else if (y < 26) C_JEANS else C_WHITE
                    val ry = (y + if (stepOffset < 0) -1 else 0)
                    for (x in 17..20) pixels[ry * 32 + x] = if (x == 17 || x == 20) C_BLACK else if (y < 26) C_JEANS else C_WHITE
                }
            }
            Direction.LEFT, Direction.RIGHT -> {
                val isRight = (direction == Direction.RIGHT)
                fun mapX(x: Int): Int = if (isRight) (31 - x) else x

                // Profile view: Cap visor sticking out
                for (y in 4..9) {
                    for (x in 12..20) {
                        val isEdge = x == 12 || x == 20 || y == 4
                        pixels[y * 32 + mapX(x)] = if (isEdge) C_BLACK else C_TRAINER_RED
                    }
                }
                // Visor extending forward to left
                for (x in 8..13) pixels[10 * 32 + mapX(x)] = C_BLACK

                // Face profile
                for (y in 10..15) {
                    for (x in 11..18) {
                        val isEdge = x == 11 || x == 18 || y == 15
                        pixels[y * 32 + mapX(x)] = if (isEdge) C_BLACK else C_SKIN
                    }
                }
                // Eye
                pixels[12 * 32 + mapX(13)] = C_BLACK

                // Backpack on back (right side of character when facing left)
                for (y in 14..21) {
                    for (x in 18..23) {
                        val isEdge = x == 18 || x == 23 || y == 14 || y == 21
                        pixels[y * 32 + mapX(x)] = if (isEdge) C_BLACK else C_BACKPACK
                    }
                }

                // Body jacket
                for (y in 16..22) {
                    for (x in 11..18) {
                        val isEdge = x == 11 || y == 22
                        pixels[y * 32 + mapX(x)] = if (isEdge) C_BLACK else C_TRAINER_RED
                    }
                }

                // Stepping legs (side profile stride)
                val legSpread = if (walkFrame == 1) 3 else if (walkFrame == 2) -3 else 0
                for (y in 23..28) {
                    for (x in 12..15) {
                        pixels[y * 32 + mapX(x + legSpread / 2)] = if (y < 26) C_JEANS else C_WHITE
                        pixels[y * 32 + mapX(x - legSpread / 2)] = if (y < 26) C_JEANS else C_WHITE
                    }
                }
            }
        }

        // Pokéball throw pose if attacking
        if (isAttacking) {
            val ballX = if (direction == Direction.LEFT) 6 else 24
            val ballY = 14
            for (y in -2..2) {
                for (x in -2..2) {
                    if (x * x + y * y <= 4) {
                        val c = if (x * x + y * y == 4) C_BLACK else if (y < 0) C_PKMN_RED else C_WHITE
                        pixels[(ballY + y) * 32 + (ballX + x)] = c
                    }
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    // ==========================================
    // POKÉMON TRAINER BATTLE BACK-SPRITE (64x64)
    // ==========================================

    private fun createPokemonTrainerBackSprite(isThrowing: Boolean): Bitmap {
        val size = 64
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size) { C_TRANS }

        // Authentic Game Boy Color Trainer back-sprite (Red cap from behind, jacket, backpack, poised to battle)
        // 1. Cap back (y: 6 to 18, x: 22 to 42)
        for (y in 6..18) {
            for (x in 22..42) {
                val dx = x - 32f
                val dy = y - 12f
                if (dx * dx / 100 + dy * dy / 36 <= 1) {
                    val isEdge = dx * dx / 100 + dy * dy / 36 >= 0.8f || y == 6
                    pixels[y * size + x] = if (isEdge) C_BLACK else C_TRAINER_RED
                }
            }
        }
        // Visor lip visible in profile
        for (x in 18..24) pixels[16 * size + x] = C_BLACK

        // Hair at nape of neck
        for (y in 19..24) {
            for (x in 24..40) {
                if (x % 3 == 0 || y == 24) pixels[y * size + x] = C_HAIR_DARK
            }
        }

        // 2. Yellow Adventurer Backpack (y: 25 to 44, x: 20 to 44)
        for (y in 25..44) {
            for (x in 20..44) {
                val isEdge = x == 20 || x == 44 || y == 25 || y == 44
                pixels[y * size + x] = if (isEdge) C_BLACK else if (x > 38 || y > 38) C_BACKPACK_DARK else C_BACKPACK
            }
        }
        // Backpack straps & buckle
        for (y in 28..42) {
            pixels[y * size + 26] = C_BLACK
            pixels[y * size + 38] = C_BLACK
        }

        // 3. Shoulders and Red Jacket (y: 26 to 50, x: 12 to 52)
        for (y in 26..50) {
            // Left arm / shoulder
            for (x in 12..20) {
                if (pixels[y * size + x] == C_TRANS) {
                    val isEdge = x == 12 || y == 50
                    pixels[y * size + x] = if (isEdge) C_BLACK else C_TRAINER_RED
                }
            }
            // Right arm / throwing arm
            for (x in 44..52) {
                if (pixels[y * size + x] == C_TRANS) {
                    val isEdge = x == 52 || y == 50
                    pixels[y * size + x] = if (isEdge) C_BLACK else C_TRAINER_RED
                }
            }
        }

        // 4. Pokéball in hand if throwing pose
        if (isThrowing) {
            val bx = 48
            val by = 20
            for (y in -5..5) {
                for (x in -5..5) {
                    val distSq = x * x + y * y
                    if (distSq <= 25) {
                        val c = if (distSq >= 20 || y == 0) C_BLACK else if (y < 0) C_PKMN_RED else C_WHITE
                        pixels[(by + y) * size + (bx + x)] = c
                    }
                }
            }
            // Pokéball center button
            pixels[by * size + bx] = C_WHITE
            pixels[by * size + (bx - 1)] = C_WHITE
        }

        // 5. Jeans & Belt (y: 50 to 62)
        for (y in 50..62) {
            for (x in 20..44) {
                val isEdge = x == 20 || x == 44 || y == 62
                pixels[y * size + x] = if (isEdge) C_BLACK else if (y in 50..52) C_BLACK else C_JEANS
            }
        }

        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        return bmp
    }

    // ==========================================
    // POKÉMON CREATURES (32x32 Overworld & 64x64 Battle)
    // ==========================================

    private fun createPokemonMonster(spriteKey: String, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size) { C_TRANS }
        val s = size / 32

        val (cPrimary, cSecondary, cEye) = when {
            spriteKey.contains("goblin") -> Triple(C_PKMN_YELLOW, 0xFFD89800.toInt(), C_BLACK) // Electric Mouse (Pikaspark)
            spriteKey.contains("wolf") -> Triple(C_PKMN_ORANGE, C_PKMN_RED, C_WHITE) // Fire Lizard (Flameling)
            spriteKey.contains("croc") -> Triple(C_WATER_LIGHT, C_WATER_DARK, C_PKMN_RED) // Water Turtle (Aquaturtle)
            spriteKey.contains("golem") -> Triple(C_ROCK_BASE, C_ROCK_DARK, C_WHITE) // Rock Golem (Rockfist)
            spriteKey.contains("scorpion") -> Triple(0xFF985020.toInt(), C_BLACK, C_PKMN_YELLOW) // Bug / Ground
            spriteKey.contains("wraith") -> Triple(C_PKMN_PURPLE, 0xFF482068.toInt(), C_PKMN_RED) // Ghost (Shadowghoul)
            spriteKey.contains("boss") -> Triple(0xFF282838.toInt(), 0xFF6020A0.toInt(), 0xFFFF2040.toInt()) // Legendary Titan
            else -> Triple(C_PKMN_YELLOW, C_PKMN_ORANGE, C_BLACK)
        }

        val centerX = size / 2
        val centerY = size / 2
        val radius = 10 * s

        // Draw iconic round Pokémon silhouette with solid 1px black outline
        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - centerX
                val dy = y - centerY
                val distSq = dx * dx + dy * dy

                if (distSq < radius * radius) {
                    val isEdge = distSq > (radius - 2 * s) * (radius - 2 * s)
                    pixels[y * size + x] = if (isEdge) C_BLACK else if (dx < 0 && dy < 0) cPrimary else cSecondary
                }
            }
        }

        // Signature Pokémon Features:
        // 1. Pikachu style: Pointed black-tipped ears & red cheeks
        if (spriteKey.contains("goblin")) {
            // Left ear
            for (i in 0..(6 * s)) {
                val ex = centerX - 6 * s - i
                val ey = centerY - radius - i
                if (ex in 0 until size && ey in 0 until size) {
                    pixels[ey * size + ex] = if (i > 3 * s) C_BLACK else cPrimary
                }
            }
            // Right ear
            for (i in 0..(6 * s)) {
                val ex = centerX + 6 * s + i
                val ey = centerY - radius - i
                if (ex in 0 until size && ey in 0 until size) {
                    pixels[ey * size + ex] = if (i > 3 * s) C_BLACK else cPrimary
                }
            }
            // Red cheeks
            val cheekY = centerY + 1 * s
            for (cy in cheekY..(cheekY + 2 * s)) {
                for (cx in (centerX - 7 * s)..(centerX - 5 * s)) if (cx in 0 until size && cy in 0 until size) pixels[cy * size + cx] = C_PKMN_RED
                for (cx in (centerX + 5 * s)..(centerX + 7 * s)) if (cx in 0 until size && cy in 0 until size) pixels[cy * size + cx] = C_PKMN_RED
            }
        }

        // 2. Charmander / Fire tail flame
        if (spriteKey.contains("wolf")) {
            val tailX = centerX + radius - 2 * s
            val tailY = centerY - 4 * s
            for (y in -3 * s..3 * s) {
                for (x in -3 * s..3 * s) {
                    if (x * x + y * y < 8 * s * s) {
                        val px = tailX + x
                        val py = tailY + y
                        if (px in 0 until size && py in 0 until size) {
                            pixels[py * size + px] = if (x * x + y * y < 3 * s * s) C_PKMN_YELLOW else C_PKMN_RED
                        }
                    }
                }
            }
        }

        // 3. Gengar / Ghost sinister smile
        if (spriteKey.contains("wraith")) {
            val mouthY = centerY + 3 * s
            for (mx in (centerX - 5 * s)..(centerX + 5 * s)) {
                if (mouthY in 0 until size && mx in 0 until size) pixels[mouthY * size + mx] = C_WHITE
                if ((mouthY + 1 * s) in 0 until size && mx in 0 until size && (mx % (2 * s) == 0)) pixels[(mouthY + 1 * s) * size + mx] = C_BLACK
            }
        }

        // 4. Legendary Titan Horns & Aura
        if (spriteKey.contains("boss")) {
            for (i in 0..(10 * s)) {
                val lx = centerX - 8 * s - i
                val ly = centerY - radius - i
                val rx = centerX + 8 * s + i
                if (lx in 0 until size && ly in 0 until size) pixels[ly * size + lx] = C_PKMN_PURPLE
                if (rx in 0 until size && ly in 0 until size) pixels[ly * size + rx] = C_PKMN_PURPLE
            }
            // Glowing core
            for (y in (centerY - 3 * s)..(centerY + 3 * s)) {
                for (x in (centerX - 3 * s)..(centerX + 3 * s)) {
                    if (x in 0 until size && y in 0 until size) pixels[y * size + x] = C_PKMN_RED
                }
            }
        }

        // Big Anime Eyes with white catchlights
        val eyeY = (centerY - 3 * s)
        val eyeX1 = (centerX - 4 * s)
        val eyeX2 = (centerX + 3 * s)
        for (ey in eyeY until eyeY + 3 * s) {
            for (ex in eyeX1 until eyeX1 + 2 * s) if (ex in 0 until size && ey in 0 until size) pixels[ey * size + ex] = cEye
            for (ex in eyeX2 until eyeX2 + 2 * s) if (ex in 0 until size && ey in 0 until size) pixels[ey * size + ex] = cEye
        }
        // White specular reflections
        if (eyeX1 in 0 until size && eyeY in 0 until size) pixels[eyeY * size + eyeX1] = C_WHITE
        if (eyeX2 in 0 until size && eyeY in 0 until size) pixels[eyeY * size + eyeX2] = C_WHITE

        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        return bmp
    }

    private fun createPokemonAnimal(spriteKey: String): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        val bodyColor = when (spriteKey) {
            "sheep" -> C_WHITE // Mareep
            "deer" -> C_DIRT_BASE // Stantler
            else -> C_WATER_LIGHT // Poliwag
        }

        // Pokémon fluffy body with 1px black outline
        for (y in 12..24) {
            for (x in 8..23) {
                val dx = x - 15.5f
                val dy = y - 18f
                if (dx * dx + dy * dy < 48) {
                    val isEdge = dx * dx + dy * dy > 34
                    pixels[y * 32 + x] = if (isEdge) C_BLACK else bodyColor
                }
            }
        }
        // Head
        for (y in 8..15) {
            for (x in 20..27) {
                val dx = x - 23.5f
                val dy = y - 11.5f
                if (dx * dx + dy * dy < 16) {
                    val isEdge = dx * dx + dy * dy > 10
                    pixels[y * 32 + x] = if (isEdge) C_BLACK else bodyColor
                }
            }
        }
        // Eye
        pixels[10 * 32 + 25] = C_BLACK

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createPokemonNpc(spriteKey: String): Bitmap {
        // Professor Oak / Pokémon Scholar
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        // White Lab Coat
        for (y in 14..28) {
            for (x in 10..21) {
                val isEdge = x == 10 || x == 21 || y == 28
                pixels[y * 32 + x] = if (isEdge) C_BLACK else C_WHITE
            }
        }
        // Red tie
        pixels[16 * 32 + 15] = C_PKMN_RED
        pixels[17 * 32 + 15] = C_PKMN_RED

        // Hair (Grey scholar hair)
        for (y in 4..10) {
            for (x in 11..20) {
                val isEdge = x == 11 || x == 20 || y == 4
                pixels[y * 32 + x] = if (isEdge) C_BLACK else 0xFFB0B0C0.toInt()
            }
        }
        // Face
        for (y in 10..14) {
            for (x in 12..19) pixels[y * 32 + x] = C_SKIN
        }
        // Eyes
        pixels[12 * 32 + 14] = C_BLACK
        pixels[12 * 32 + 17] = C_BLACK

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    // ==========================================
    // POKÉBALL GROUND ITEM (Red/White with Black Band)
    // ==========================================

    private fun createPokemonItemBall(isOpen: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        val cx = 15.5f
        val cy = 16.5f
        val r = 8f

        if (!isOpen) {
            // Closed Pokéball: Top half red, bottom half white, black equator band, white center button
            for (y in 8..24) {
                for (x in 8..24) {
                    val dx = x - cx
                    val dy = y - cy
                    val dSq = dx * dx + dy * dy
                    if (dSq <= r * r) {
                        val isEdge = dSq > (r - 1.5f) * (r - 1.5f)
                        if (isEdge) {
                            pixels[y * 32 + x] = C_BLACK
                        } else if (dy in -1.5f..1.5f) {
                            pixels[y * 32 + x] = C_BLACK // horizontal black band
                        } else if (dy < 0) {
                            pixels[y * 32 + x] = C_PKMN_RED // top red hemisphere
                        } else {
                            pixels[y * 32 + x] = C_WHITE // bottom white hemisphere
                        }
                    }
                }
            }
            // Center white Pokéball release button
            for (y in 15..17) {
                for (x in 14..16) pixels[y * 32 + x] = C_WHITE
            }
            pixels[16 * 32 + 15] = C_BLACK
        } else {
            // Opened Pokéball with sparkling stars
            for (y in 8..15) {
                for (x in 8..24) if ((x - cx) * (x - cx) + (y - 12) * (y - 12) < 25) pixels[y * 32 + x] = C_PKMN_RED
            }
            for (y in 19..26) {
                for (x in 8..24) if ((x - cx) * (x - cx) + (y - 21) * (y - 21) < 25) pixels[y * 32 + x] = C_WHITE
            }
            // Sparkle stars
            pixels[6 * 32 + 15] = C_PKMN_YELLOW
            pixels[5 * 32 + 15] = C_PKMN_YELLOW
            pixels[7 * 32 + 15] = C_PKMN_YELLOW
            pixels[6 * 32 + 14] = C_PKMN_YELLOW
            pixels[6 * 32 + 16] = C_PKMN_YELLOW
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createPokemonItemIcon(iconKey: String): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANS }

        when {
            iconKey.contains("potion") -> {
                // Potion spray bottle (blue/purple)
                for (y in 12..26) {
                    for (x in 10..21) {
                        val isEdge = x == 10 || x == 21 || y == 12 || y == 26
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else if (iconKey.contains("mp")) C_WATER_LIGHT else C_PKMN_RED
                    }
                }
                // Spray nozzle
                for (y in 6..11) {
                    pixels[y * 32 + 15] = C_BLACK
                    pixels[y * 32 + 16] = C_WHITE
                }
            }
            iconKey.contains("artifact") -> {
                // Master Ball / Legendary Crest
                for (y in 6..26) {
                    for (x in 6..26) {
                        val dSq = (x - 16) * (x - 16) + (y - 16) * (y - 16)
                        if (dSq <= 64) {
                            pixels[y * 32 + x] = if (dSq > 45) C_BLACK else if (y < 16) C_PKMN_PURPLE else C_WHITE
                        }
                    }
                }
                pixels[16 * 32 + 16] = C_PKMN_YELLOW
            }
            else -> {
                // Default Poké Item
                for (y in 10..22) {
                    for (x in 10..22) pixels[y * 32 + x] = C_PKMN_YELLOW
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }
}
