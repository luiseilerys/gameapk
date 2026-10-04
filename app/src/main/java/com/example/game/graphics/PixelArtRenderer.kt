package com.example.game.graphics

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.game.model.DecoType
import com.example.game.model.Direction
import com.example.game.model.TileType
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance SNES 16-bit Pixel Art Sprite and Tile Generator.
 * Creates and caches crisp 32x32 and 64x64 pixel art bitmaps using authentic color palettes.
 */
object PixelArtRenderer {
    private val spriteCache = ConcurrentHashMap<String, ImageBitmap>()

    // SNES Color Palettes
    private const val C_TRANSPARENT = 0x00000000
    private const val C_BLACK = 0xFF141013.toInt()
    private const val C_WHITE = 0xFFF5F7FA.toInt()

    // Skin & Hair
    private const val C_SKIN = 0xFFF6C8A4.toInt()
    private const val C_SKIN_SHADOW = 0xFFD89B77.toInt()
    private const val C_HAIR_BROWN = 0xFF5D3A1A.toInt()

    // Hero Gear (Blue tunic, steel sword, leather boots)
    private const val C_HERO_TUNIC = 0xFF2A6FDB.toInt()
    private const val C_HERO_TUNIC_DARK = 0xFF18428F.toInt()
    private const val C_HERO_BOOTS = 0xFF654321.toInt()
    private const val C_GOLD = 0xFFFFD700.toInt()
    private const val C_STEEL = 0xFFD0D7DE.toInt()
    private const val C_STEEL_DARK = 0xFF7D8590.toInt()
    private const val C_RUBY = 0xFFE53935.toInt()

    // Terrain Colors
    private const val C_GRASS_LIGHT = 0xFF4CAF50.toInt()
    private const val C_GRASS_DARK = 0xFF2E7D32.toInt()
    private const val C_DIRT_LIGHT = 0xFF8D6E63.toInt()
    private const val C_DIRT_DARK = 0xFF5D4037.toInt()
    private const val C_SAND_LIGHT = 0xFFFBC02D.toInt()
    private const val C_SAND_DARK = 0xFFF57F17.toInt()
    private const val C_WATER_LIGHT = 0xFF29B6F6.toInt()
    private const val C_WATER_DARK = 0xFF0277BD.toInt()
    private const val C_ROCK_LIGHT = 0xFF90A4AE.toInt()
    private const val C_ROCK_DARK = 0xFF455A64.toInt()
    private const val C_SWAMP_LIGHT = 0xFF4E6B4A.toInt()
    private const val C_SWAMP_DARK = 0xFF2F442C.toInt()
    private const val C_RUINS_LIGHT = 0xFF7E728B.toInt()
    private const val C_RUINS_DARK = 0xFF43394E.toInt()

    fun getTileBitmap(tileType: TileType): ImageBitmap {
        return spriteCache.computeIfAbsent("tile_${tileType.name}") {
            createTileBitmap(tileType).asImageBitmap()
        }
    }

    fun getDecoBitmap(decoType: DecoType): ImageBitmap? {
        if (decoType == DecoType.NONE) return null
        return spriteCache.computeIfAbsent("deco_${decoType.name}") {
            createDecoBitmap(decoType).asImageBitmap()
        }
    }

    fun getPlayerBitmap(direction: Direction, walkFrame: Int, isAttacking: Boolean): ImageBitmap {
        val key = if (isAttacking) "hero_attack_${direction.name}" else "hero_walk_${direction.name}_$walkFrame"
        return spriteCache.computeIfAbsent(key) {
            createPlayerBitmap(direction, walkFrame, isAttacking).asImageBitmap()
        }
    }

    fun getEnemyOverworldBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("ow_$spriteKey") {
            createEnemyBitmap(spriteKey, size = 32).asImageBitmap()
        }
    }

    fun getEnemyBattleBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("bt_$spriteKey") {
            createEnemyBitmap(spriteKey, size = 64).asImageBitmap()
        }
    }

    fun getAnimalBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("animal_$spriteKey") {
            createAnimalBitmap(spriteKey).asImageBitmap()
        }
    }

    fun getNpcBitmap(spriteKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("npc_$spriteKey") {
            createNpcBitmap(spriteKey).asImageBitmap()
        }
    }

    fun getChestBitmap(isOpen: Boolean): ImageBitmap {
        val key = if (isOpen) "chest_open" else "chest_closed"
        return spriteCache.computeIfAbsent(key) {
            createChestBitmap(isOpen).asImageBitmap()
        }
    }

    fun getItemIconBitmap(iconKey: String): ImageBitmap {
        return spriteCache.computeIfAbsent("icon_$iconKey") {
            createItemIconBitmap(iconKey).asImageBitmap()
        }
    }

    // ==========================================
    // PROCEDURAL BITMAP BUILDERS (32x32 & 64x64)
    // ==========================================

    private fun createTileBitmap(tileType: TileType): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32)

        val (cPrimary, cSecondary) = when (tileType) {
            TileType.GRASS -> C_GRASS_LIGHT to C_GRASS_DARK
            TileType.DIRT -> C_DIRT_LIGHT to C_DIRT_DARK
            TileType.SAND -> C_SAND_LIGHT to C_SAND_DARK
            TileType.WATER, TileType.DEEP_WATER -> C_WATER_LIGHT to C_WATER_DARK
            TileType.ROCK_GROUND, TileType.CLIFF -> C_ROCK_LIGHT to C_ROCK_DARK
            TileType.SWAMP_MUD -> C_SWAMP_LIGHT to C_SWAMP_DARK
            TileType.RUINS_STONE -> C_RUINS_LIGHT to C_RUINS_DARK
        }

        for (y in 0 until 32) {
            for (x in 0 until 32) {
                // Procedural dithering and noise for authentic 16-bit texture
                val isDither = ((x + y) % 4 == 0) || ((x * 7 + y * 13) % 9 == 0)
                val color = if (isDither) cSecondary else cPrimary

                // Subtle grid border for stone tiles
                if (tileType == TileType.RUINS_STONE && (x == 0 || y == 0 || x == 31 || y == 31)) {
                    pixels[y * 32 + x] = C_RUINS_DARK
                } else if (tileType == TileType.WATER && (y == 8 || y == 22) && (x in 4..14 || x in 18..28)) {
                    // Water ripple highlight
                    pixels[y * 32 + x] = C_WHITE
                } else {
                    pixels[y * 32 + x] = color
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createDecoBitmap(decoType: DecoType): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        when (decoType) {
            DecoType.TREE_OAK, DecoType.TREE_PINE -> {
                // Trunk
                for (y in 18 until 30) {
                    for (x in 13 until 19) {
                        pixels[y * 32 + x] = if (x == 13 || x == 18) C_DIRT_DARK else C_DIRT_LIGHT
                    }
                }
                // Foliage canopy
                val leafPrimary = if (decoType == DecoType.TREE_OAK) C_GRASS_LIGHT else 0xFF1B5E20.toInt()
                val leafDark = if (decoType == DecoType.TREE_OAK) C_GRASS_DARK else 0xFF0D3813.toInt()
                for (y in 4 until 20) {
                    for (x in 6 until 26) {
                        val distSq = (x - 16) * (x - 16) + (y - 12) * (y - 12)
                        if (distSq < 64) {
                            pixels[y * 32 + x] = if ((x + y) % 3 == 0) leafDark else leafPrimary
                        }
                    }
                }
            }
            DecoType.DEAD_TREE -> {
                // Gnarly dead branches
                for (y in 8 until 30) {
                    for (x in 14 until 18) pixels[y * 32 + x] = C_DIRT_DARK
                }
                for (i in 0..6) {
                    pixels[(14 - i) * 32 + (14 - i)] = C_DIRT_DARK
                    pixels[(16 - i) * 32 + (17 + i)] = C_DIRT_DARK
                }
            }
            DecoType.ROCK_BOULDER -> {
                for (y in 10 until 28) {
                    for (x in 8 until 24) {
                        val distSq = (x - 16) * (x - 16) + (y - 19) * (y - 19)
                        if (distSq < 48) {
                            val isShade = x > 16 || y > 20
                            pixels[y * 32 + x] = if (isShade) C_ROCK_DARK else C_ROCK_LIGHT
                        }
                    }
                }
            }
            DecoType.BUSH -> {
                for (y in 14 until 28) {
                    for (x in 8 until 24) {
                        val distSq = (x - 16) * (x - 16) + (y - 21) * (y - 21)
                        if (distSq < 36) pixels[y * 32 + x] = if (x % 2 == 0) C_GRASS_DARK else C_GRASS_LIGHT
                    }
                }
            }
            DecoType.FLOWER_RED, DecoType.FLOWER_BLUE -> {
                val petal = if (decoType == DecoType.FLOWER_RED) C_RUBY else C_WATER_LIGHT
                // Stem
                for (y in 22 until 28) pixels[y * 32 + 16] = C_GRASS_DARK
                // Petals
                pixels[20 * 32 + 16] = C_GOLD
                pixels[19 * 32 + 16] = petal
                pixels[21 * 32 + 16] = petal
                pixels[20 * 32 + 15] = petal
                pixels[20 * 32 + 17] = petal
            }
            DecoType.RUINS_PILLAR -> {
                for (y in 4 until 30) {
                    for (x in 11 until 21) {
                        val isEdge = x == 11 || x == 20 || y == 4 || y == 29
                        pixels[y * 32 + x] = if (isEdge) C_BLACK else if (x < 15) C_RUINS_LIGHT else C_RUINS_DARK
                    }
                }
            }
            DecoType.RUINS_ALTAR -> {
                for (y in 12 until 28) {
                    for (x in 8 until 24) {
                        pixels[y * 32 + x] = if (y < 16) C_RUINS_LIGHT else C_RUINS_DARK
                    }
                }
                // Glowing golden artifact orb on top of altar
                for (y in 6 until 12) {
                    for (x in 13 until 19) {
                        pixels[y * 32 + x] = if (x in 14..17 && y in 7..10) C_GOLD else C_RUBY
                    }
                }
            }
            DecoType.CACTUS -> {
                for (y in 8 until 28) {
                    for (x in 14 until 18) pixels[y * 32 + x] = 0xFF2E7D32.toInt()
                }
                for (x in 9..14) pixels[16 * 32 + x] = 0xFF2E7D32.toInt()
                for (y in 12..16) pixels[y * 32 + 9] = 0xFF2E7D32.toInt()
                for (x in 18..23) pixels[14 * 32 + x] = 0xFF2E7D32.toInt()
                for (y in 10..14) pixels[y * 32 + 23] = 0xFF2E7D32.toInt()
            }
            else -> {}
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createPlayerBitmap(direction: Direction, walkFrame: Int, isAttacking: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        // Head (y: 6 to 13, x: 12 to 19)
        for (y in 6..13) {
            for (x in 12..19) {
                pixels[y * 32 + x] = C_SKIN
            }
        }
        // Hair (y: 4 to 8)
        for (y in 4..8) {
            for (x in 11..20) {
                if (y <= 6 || x == 11 || x == 20) pixels[y * 32 + x] = C_HAIR_BROWN
            }
        }

        // Eyes based on direction
        when (direction) {
            Direction.DOWN -> {
                pixels[9 * 32 + 14] = C_BLACK
                pixels[9 * 32 + 17] = C_BLACK
            }
            Direction.UP -> {
                // Back of head (hair covers)
                for (y in 6..10) for (x in 12..19) pixels[y * 32 + x] = C_HAIR_BROWN
            }
            Direction.LEFT -> {
                pixels[9 * 32 + 13] = C_BLACK
            }
            Direction.RIGHT -> {
                pixels[9 * 32 + 18] = C_BLACK
            }
        }

        // Tunic / Body (y: 14 to 22, x: 10 to 21)
        for (y in 14..22) {
            for (x in 11..20) {
                pixels[y * 32 + x] = if (x == 11 || x == 20 || y == 22) C_HERO_TUNIC_DARK else C_HERO_TUNIC
            }
        }
        // Golden Belt
        for (x in 11..20) pixels[19 * 32 + x] = C_GOLD

        // Legs / Boots with walk animation offset
        val legOffset = if (walkFrame == 1) 2 else if (walkFrame == 2) -2 else 0
        for (y in 23..28) {
            // Left leg
            for (x in 12..14) pixels[(y + (if (legOffset > 0) -1 else 0)) * 32 + x] = C_HERO_BOOTS
            // Right leg
            for (x in 17..19) pixels[(y + (if (legOffset < 0) -1 else 0)) * 32 + x] = C_HERO_BOOTS
        }

        // Sword / Attack swing
        if (isAttacking) {
            when (direction) {
                Direction.RIGHT, Direction.DOWN -> {
                    // Golden blade extending out
                    for (i in 0..10) {
                        pixels[(16 - i / 2) * 32 + (21 + i)] = C_STEEL
                        pixels[(15 - i / 2) * 32 + (21 + i)] = C_GOLD
                    }
                }
                Direction.LEFT -> {
                    for (i in 0..10) {
                        pixels[(16 - i / 2) * 32 + (10 - i)] = C_STEEL
                        pixels[(15 - i / 2) * 32 + (10 - i)] = C_GOLD
                    }
                }
                Direction.UP -> {
                    for (i in 0..10) {
                        pixels[(12 - i) * 32 + (16 + i / 3)] = C_STEEL
                    }
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createEnemyBitmap(spriteKey: String, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(size * size) { C_TRANSPARENT }
        val s = size / 32 // Scale factor (1 for 32x32 overworld, 2 for 64x64 battle)

        val (cBody, cEye, cDetail) = when {
            spriteKey.contains("wolf") -> Triple(0xFF757575.toInt(), C_RUBY, 0xFF424242.toInt())
            spriteKey.contains("goblin") -> Triple(0xFF689F38.toInt(), C_GOLD, 0xFF33691E.toInt())
            spriteKey.contains("scorpion") -> Triple(0xFFD84315.toInt(), C_BLACK, 0xFFBF360C.toInt())
            spriteKey.contains("golem") -> Triple(0xFF78909C.toInt(), 0xFF00E5FF.toInt(), 0xFF37474F.toInt())
            spriteKey.contains("croc") -> Triple(0xFF2E7D32.toInt(), C_GOLD, 0xFF1B5E20.toInt())
            spriteKey.contains("wraith") -> Triple(0xFF7E57C2.toInt(), 0xFF69F0AE.toInt(), 0xFF311B92.toInt())
            spriteKey.contains("boss") -> Triple(0xFF212121.toInt(), 0xFFFF1744.toInt(), 0xFFD50000.toInt())
            else -> Triple(0xFF9E9E9E.toInt(), C_RUBY, C_BLACK)
        }

        // Draw body silhouette
        val centerX = size / 2
        val centerY = size / 2
        val radius = (10 * s)

        for (y in 0 until size) {
            for (x in 0 until size) {
                val dx = x - centerX
                val dy = y - centerY
                val distSq = dx * dx + dy * dy

                if (distSq < radius * radius) {
                    val isEdge = distSq > (radius - 2 * s) * (radius - 2 * s)
                    pixels[y * size + x] = if (isEdge) cDetail else cBody
                }
            }
        }

        // Distinct features
        if (spriteKey.contains("boss")) {
            // Demonic horns / crown
            for (y in 4 * s until 12 * s) {
                for (x in 6 * s until 10 * s) pixels[y * size + x] = C_GOLD
                for (x in 22 * s until 26 * s) pixels[y * size + x] = C_GOLD
            }
            // Fiery core in center
            for (y in 14 * s until 20 * s) {
                for (x in 13 * s until 19 * s) pixels[y * size + x] = 0xFFFF3D00.toInt()
            }
        }

        // Glowing menacing eyes
        val eyeY = (centerY - 3 * s)
        val eyeX1 = (centerX - 4 * s)
        val eyeX2 = (centerX + 3 * s)
        for (ey in eyeY until eyeY + 2 * s) {
            for (ex in eyeX1 until eyeX1 + 2 * s) if (ex in 0 until size && ey in 0 until size) pixels[ey * size + ex] = cEye
            for (ex in eyeX2 until eyeX2 + 2 * s) if (ex in 0 until size && ey in 0 until size) pixels[ey * size + ex] = cEye
        }

        bmp.setPixels(pixels, 0, size, 0, 0, size, size)
        return bmp
    }

    private fun createAnimalBitmap(spriteKey: String): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        val bodyColor = when (spriteKey) {
            "deer" -> 0xFFA1887F.toInt()
            "sheep" -> 0xFFECEFF1.toInt()
            else -> 0xFF4CAF50.toInt() // frog
        }

        // Oval body
        for (y in 14..24) {
            for (x in 10..22) {
                pixels[y * 32 + x] = bodyColor
            }
        }
        // Head
        for (y in 10..15) {
            for (x in 20..26) pixels[y * 32 + x] = bodyColor
        }
        // Eye
        pixels[11 * 32 + 24] = C_BLACK

        // Antlers for deer
        if (spriteKey == "deer") {
            pixels[7 * 32 + 22] = C_DIRT_DARK
            pixels[8 * 32 + 22] = C_DIRT_DARK
            pixels[6 * 32 + 21] = C_DIRT_DARK
            pixels[6 * 32 + 23] = C_DIRT_DARK
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createNpcBitmap(spriteKey: String): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        val robeColor = if (spriteKey.contains("wizard")) 0xFF4A148C.toInt() else 0xFF2E7D32.toInt()
        val hatColor = if (spriteKey.contains("wizard")) 0xFF6A1B9A.toInt() else 0xFF3E2723.toInt()

        // Robe
        for (y in 14..28) {
            for (x in 10..21) pixels[y * 32 + x] = robeColor
        }
        // Face
        for (y in 8..13) {
            for (x in 12..19) pixels[y * 32 + x] = C_SKIN
        }
        // Eyes
        pixels[10 * 32 + 14] = C_BLACK
        pixels[10 * 32 + 17] = C_BLACK
        // Beard for wizard
        if (spriteKey.contains("wizard")) {
            for (y in 12..18) {
                for (x in 13..18) pixels[y * 32 + x] = C_WHITE
            }
        }
        // Hat
        for (y in 3..7) {
            for (x in 11..20) pixels[y * 32 + x] = hatColor
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createChestBitmap(isOpen: Boolean): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        // Chest Body (wood & gold trim)
        for (y in 14..28) {
            for (x in 6..25) {
                val isTrim = x == 6 || x == 25 || y == 14 || y == 28 || x == 15 || x == 16
                pixels[y * 32 + x] = if (isTrim) C_GOLD else C_DIRT_DARK
            }
        }

        if (isOpen) {
            // Open lid tilted back
            for (y in 6..13) {
                for (x in 6..25) {
                    pixels[y * 32 + x] = if (x == 6 || x == 25 || y == 6) C_GOLD else C_DIRT_LIGHT
                }
            }
            // Sparkles inside
            pixels[16 * 32 + 12] = C_GOLD
            pixels[17 * 32 + 18] = C_WHITE
            pixels[18 * 32 + 14] = 0xFF00E5FF.toInt()
        } else {
            // Lock
            pixels[20 * 32 + 15] = C_GOLD
            pixels[20 * 32 + 16] = C_GOLD
            pixels[21 * 32 + 15] = C_BLACK
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }

    private fun createItemIconBitmap(iconKey: String): Bitmap {
        val bmp = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(32 * 32) { C_TRANSPARENT }

        when {
            iconKey.contains("potion") -> {
                val liquidColor = when {
                    iconKey.contains("red") -> C_RUBY
                    iconKey.contains("blue") -> C_WATER_LIGHT
                    else -> C_GOLD
                }
                // Flask cork
                for (x in 14..17) pixels[8 * 32 + x] = C_DIRT_LIGHT
                // Flask neck
                for (y in 9..12) for (x in 14..17) pixels[y * 32 + x] = C_WHITE
                // Round body
                for (y in 13..26) {
                    for (x in 9..22) {
                        val dist = (x - 15.5f) * (x - 15.5f) + (y - 19.5f) * (y - 19.5f)
                        if (dist < 36) {
                            pixels[y * 32 + x] = if (y < 16) C_WHITE else liquidColor
                        }
                    }
                }
            }
            iconKey.contains("sword") -> {
                val isRunic = iconKey.contains("runic")
                val bladeColor = if (isRunic) 0xFF00E5FF.toInt() else C_STEEL
                for (i in 0..16) {
                    pixels[(24 - i) * 32 + (8 + i)] = bladeColor
                    pixels[(24 - i) * 32 + (7 + i)] = C_STEEL_DARK
                }
                // Hilt
                pixels[25 * 32 + 7] = C_GOLD
                pixels[26 * 32 + 6] = C_HERO_BOOTS
            }
            iconKey.contains("armor") -> {
                val plateColor = if (iconKey.contains("steel")) C_STEEL else C_DIRT_LIGHT
                for (y in 8..24) {
                    for (x in 8..23) pixels[y * 32 + x] = plateColor
                }
            }
            iconKey.contains("artifact") -> {
                // Sun orb
                for (y in 6..25) {
                    for (x in 6..25) {
                        val d = (x - 15.5f) * (x - 15.5f) + (y - 15.5f) * (y - 15.5f)
                        if (d < 64) pixels[y * 32 + x] = if (d < 25) C_WHITE else C_GOLD
                    }
                }
            }
            else -> {
                // Default gem / material
                for (y in 10..22) {
                    for (x in 10..22) pixels[y * 32 + x] = C_GOLD
                }
            }
        }

        bmp.setPixels(pixels, 0, 32, 0, 0, 32, 32)
        return bmp
    }
}
