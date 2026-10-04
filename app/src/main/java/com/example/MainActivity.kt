package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.game.model.GameScreen
import com.example.game.ui.BattleScreen
import com.example.game.ui.GameOverScreen
import com.example.game.ui.InventoryScreen
import com.example.game.ui.MainMenuScreen
import com.example.game.ui.OverworldCanvas
import com.example.game.ui.OverworldHUD
import com.example.game.ui.PauseMenuDialog
import com.example.game.ui.VictoryScreen
import com.example.game.ui.VirtualControls
import com.example.game.ui.WorldMapScreen
import com.example.game.viewmodel.GameViewModel
import com.example.ui.theme.MyApplicationTheme

/**
 * Single Activity hosting the complete 2D SNES Open World Exploration RPG with Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A)
                ) {
                    GameRoot(viewModel = viewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.audio.stopMusic()
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.currentScreen.value == GameScreen.OVERWORLD) {
            viewModel.resumeToOverworld()
        }
    }
}

@Composable
fun GameRoot(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val battleState by viewModel.battleState.collectAsState()
    val activeDialogue by viewModel.activeDialogue.collectAsState()

    when (currentScreen) {
        GameScreen.MAIN_MENU -> {
            MainMenuScreen(viewModel = viewModel)
        }

        GameScreen.OVERWORLD -> {
            BackHandler {
                if (activeDialogue != null) {
                    viewModel.onActionButtonAPressed()
                } else {
                    viewModel.togglePause()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // 1. 2D World Canvas (60 FPS rendering)
                OverworldCanvas(viewModel = viewModel)

                // 2. HUD & Minimap & Dialogues
                OverworldHUD(viewModel = viewModel)

                // 3. Virtual Joystick & SNES Action Buttons
                VirtualControls(
                    onJoystickMove = { x, y ->
                        viewModel.joystickX = x
                        viewModel.joystickY = y
                    },
                    onButtonAPressed = { viewModel.onActionButtonAPressed() },
                    onButtonBPressed = { viewModel.onActionButtonBPressed() }
                )

                // 4. Pause Menu Dialog Modal
                if (isPaused) {
                    PauseMenuDialog(
                        viewModel = viewModel,
                        onResume = { viewModel.resumeToOverworld() },
                        onOpenInventory = { viewModel.openInventory() },
                        onOpenMap = { viewModel.openWorldMap() },
                        onExitToMenu = { viewModel.returnToMainMenu() }
                    )
                }
            }
        }

        GameScreen.BATTLE -> {
            BackHandler {
                // Back button inside combat attempts flee
                viewModel.executePlayerAction(com.example.game.model.BattleActionType.FLEE)
            }

            if (battleState != null) {
                BattleScreen(
                    state = battleState!!,
                    viewModel = viewModel
                )
            }
        }

        GameScreen.INVENTORY -> {
            BackHandler {
                viewModel.resumeToOverworld()
            }

            InventoryScreen(
                viewModel = viewModel,
                onClose = { viewModel.resumeToOverworld() }
            )
        }

        GameScreen.WORLD_MAP -> {
            BackHandler {
                viewModel.resumeToOverworld()
            }

            WorldMapScreen(
                viewModel = viewModel,
                onClose = { viewModel.resumeToOverworld() }
            )
        }

        GameScreen.GAME_OVER -> {
            BackHandler {
                viewModel.returnToMainMenu()
            }

            GameOverScreen(viewModel = viewModel)
        }

        GameScreen.VICTORY -> {
            BackHandler {
                viewModel.returnToMainMenu()
            }

            VictoryScreen(viewModel = viewModel)
        }
    }
}
