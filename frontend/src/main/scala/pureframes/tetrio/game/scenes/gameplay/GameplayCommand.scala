package pureframes.tetrio.game.scenes.gameplay

import indigo.*
import pureframes.tetrio.game.scenes.gameplay.model.*

sealed trait GameplayCommand
object GameplayCommand:
  enum GameCommand extends GameplayCommand:
    case Move(point: Vector2)
    case Rotate(direction: RotationDirection)
    case SwapHeld
    case HardDrop
    case Pause
    case Reset
  export GameCommand.*
  
  val MoveLeft = Move(Vector2(-1, 0))
  val MoveRight = Move(Vector2(1, 0))
  val MoveDown = Move(Vector2(0, 1))

  enum DebugCommand extends GameplayCommand:
    case SpawnTetromino(t: Tetromino)
  export DebugCommand.*

  case class Composite(cmds: Batch[GameplayCommand]) extends GameplayCommand
  object Composite:
    def apply(cmds: GameplayCommand*): Composite = Composite(Batch.fromSeq(cmds))
