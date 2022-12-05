package pureframes.tetrio
package game.scenes.gameplay

import indigo.*
import pureframes.tetrio.game.scenes.gameplay.model.*

sealed trait GameplayCommand
object GameplayCommand:
  enum GameCommand extends GameplayCommand:
    case Move(point: Vector2)
    case Rotate(direction: RotationDirection)
    case HardDrop
    case Pause
  export GameCommand.*
  
  val MoveLeft = Move(Vector2(-1, 0))
  val MoveRight = Move(Vector2(1, 0))
  val MoveDown = Move(Vector2(0, 1))

  enum DebugCommand extends GameplayCommand:
    case Reset
    case SpawnTetromino(t: Tetromino)
  export DebugCommand.*

  case class Composite(cmds: Batch[GameplayCommand]) extends GameplayCommand
  object Composite:
    def apply(cmds: GameplayCommand*): Composite = Composite(Batch.fromSeq(cmds))
