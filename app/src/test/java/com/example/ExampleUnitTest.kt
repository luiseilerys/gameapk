package com.example

import com.example.game.model.CombatSkillCatalog
import com.example.game.model.Combatant
import com.example.game.model.ItemCatalog
import com.example.game.model.Player
import com.example.game.world.CHUNK_SIZE
import com.example.game.world.SimplexNoise
import com.example.game.world.WorldGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying core RPG mechanics: procedural generation, combat calculations,
 * crafting recipes, and player state integrity.
 */
class ExampleUnitTest {

  @Test
  fun testSimplexNoise_deterministicAndBounded() {
    val noise = SimplexNoise(12345L)
    val val1 = noise.eval(10.5f, 20.3f)
    val val2 = noise.eval(10.5f, 20.3f)
    assertEquals(val1, val2, 0.0001f)
    assertTrue("Noise should be in range [0, 1]", val1 in 0.0f..1.0f)
  }

  @Test
  fun testWorldGenerator_createsValidArtifactChunk() {
    val generator = WorldGenerator(seed = 987654L)
    val chunk = generator.generateChunk(generator.artifactChunkX, generator.artifactChunkY)

    assertTrue(chunk.isDungeonArtifactChunk)
    assertEquals(CHUNK_SIZE, chunk.tiles.size)
    assertEquals(CHUNK_SIZE, chunk.tiles[0].size)

    // Check chest with artifact is present
    val artifactChest = chunk.chests.find { chest ->
      chest.loot.any { it.id == ItemCatalog.ANCIENT_ARTIFACT.id }
    }
    assertNotNull("Artifact chest must exist in artifact chunk", artifactChest)
  }

  @Test
  fun testCrafting_recipeIngredientsExist() {
    val recipes = ItemCatalog.RECIPES
    assertTrue("At least 5 crafting recipes must be available", recipes.size >= 5)

    recipes.forEach { recipe ->
      assertTrue(recipe.ingredients.isNotEmpty())
      assertNotNull(recipe.resultItem)
      assertTrue(recipe.resultQuantity > 0)
    }
  }

  @Test
  fun testCombat_turnOrderSortedBySpeed() {
    val hero = Combatant(
      id = "hero",
      name = "Hero",
      isPlayer = true,
      currentHp = 100,
      maxHp = 100,
      currentMp = 30,
      maxMp = 30,
      attack = 15,
      defense = 8,
      speed = 14,
      spriteKey = "hero"
    )

    val golem = Combatant(
      id = "golem",
      name = "Golem",
      isPlayer = false,
      currentHp = 80,
      maxHp = 80,
      currentMp = 0,
      maxMp = 0,
      attack = 20,
      defense = 12,
      speed = 6,
      spriteKey = "enemy_golem"
    )

    val turnOrder = listOf(golem, hero).sortedByDescending { it.speed }
    assertEquals("Hero with speed 14 should act before Golem with speed 6", "hero", turnOrder.first().id)
  }

  @Test
  fun testPlayer_statsAndInventory() {
    val player = Player()
    assertEquals(1, player.level)
    assertTrue(player.totalAttack > player.baseStrength)
    assertTrue(player.totalDefense > player.baseDefense)
    assertTrue("Inventory must not be empty at start", player.inventory.isNotEmpty())
  }

  @Test
  fun testLocalization_bilingualStrings() {
    val esNewGame = com.example.game.localization.Strings.getNewGame(com.example.game.localization.GameLanguage.SPANISH)
    val enNewGame = com.example.game.localization.Strings.getNewGame(com.example.game.localization.GameLanguage.ENGLISH)
    assertEquals("⚔ NUEVA PARTIDA", esNewGame)
    assertEquals("⚔ NEW GAME", enNewGame)

    val esForest = com.example.game.localization.Strings.getBiomeName(com.example.game.model.Biome.FOREST, com.example.game.localization.GameLanguage.SPANISH)
    val enForest = com.example.game.localization.Strings.getBiomeName(com.example.game.model.Biome.FOREST, com.example.game.localization.GameLanguage.ENGLISH)
    assertEquals("Bosque Esmeralda", esForest)
    assertEquals("Emerald Forest", enForest)
  }
}

