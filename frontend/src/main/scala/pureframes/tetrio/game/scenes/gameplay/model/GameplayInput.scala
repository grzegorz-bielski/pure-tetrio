package pureframes.tetrio
package game.scenes.gameplay.model

import indigo.IndigoLogger.*
import indigo.*
import indigo.shared.datatypes.Vector2
import indigo.shared.events.KeyboardEvent.KeyDown
import pureframes.tetrio.game.core.*
import pureframes.tetrio.game.scenes.gameplay.*
import pureframes.tetrio.game.scenes.gameplay.model.*

import scala.collection.immutable.Queue

import GameplayCommand.*
import RotationDirection.*

final case class GameplayInput(
    spawnPoint: Vector2,
    cmds: Queue[GameplayCommand]
):

  def onFrameEnd: GameplayInput =
    copy(cmds = Queue.empty[GameplayCommand])

  def onInput(e: InputEvent, ctx: GameContext): GameplayInput =
    e match
      case KeyDown(key) => copy(cmds = produceCommands(key))
      case _            => this

  def isMoving(ctx: GameContext): Boolean =
     ctx.inputState.keyboard.keysDown.collect(gameMappings).exists {
      case m: Move => true
      case _ => false
     }

  def isMovingDown(ctx: GameContext): Boolean =
    ctx.inputState.keyboard.keysDown.collect(gameMappings).exists {
      case m: Move => m.point.y > 0
      case _ => false
    }

  private lazy val produceCommands =
    inputMappings.andThen(cmds.enqueue).orElse(_ => cmds)

  private lazy val inputMappings =
    debugMappings orElse gameMappings

  private lazy val gameMappings: PartialFunction[Key, GameplayCommand] =
    case Key.SPACE       => HardDrop
    case Key.LEFT_ARROW  => Move(Vector2(-1, 0))
    case Key.RIGHT_ARROW => Move(Vector2(1, 0))
    case Key.DOWN_ARROW  => Move(Vector2(0, 1))
    case Key.KEY_Q       => Rotate(CounterClockwise)
    case Key.KEY_W       => Rotate(Clockwise)
    case Key.KEY_P       => Pause

  private lazy val debugMappings: PartialFunction[Key, GameplayCommand] =
    case Key.KEY_I => SpawnTetromino(Tetromino.i(spawnPoint))
    case Key.KEY_J => SpawnTetromino(Tetromino.j(spawnPoint))
    case Key.KEY_L => SpawnTetromino(Tetromino.l(spawnPoint))
    case Key.KEY_O => SpawnTetromino(Tetromino.o(spawnPoint))
    case Key.KEY_S => SpawnTetromino(Tetromino.s(spawnPoint))
    case Key.KEY_T => SpawnTetromino(Tetromino.t(spawnPoint))
    case Key.KEY_Z => SpawnTetromino(Tetromino.z(spawnPoint))
    case Key.KEY_R => Reset

object GameplayInput:
  def initial(spawnPoint: Vector2): GameplayInput =
    GameplayInput(spawnPoint = spawnPoint, cmds = Queue.empty[GameplayCommand])
