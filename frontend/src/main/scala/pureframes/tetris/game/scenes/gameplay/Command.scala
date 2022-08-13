package pureframes.tetris
package game.scenes.gameplay

import indigo.*
import pureframes.tetris.game.scenes.gameplay.model.*

sealed trait Command
object Command:
  enum GameCommand extends Command:
    case Move(point: Vector2)
    case Rotate(direction: RotationDirection)
    case HardDrop
    case Pause
  export GameCommand.*

  enum DebugCommand extends Command:
    case Reset
    case SpawnTetromino(t: Tetromino)
  export DebugCommand.*

  case class Composite(cmds: Batch[Command]) extends Command
  object Composite:
    def apply(cmds: Command*): Composite = Composite(Batch.fromSeq(cmds))