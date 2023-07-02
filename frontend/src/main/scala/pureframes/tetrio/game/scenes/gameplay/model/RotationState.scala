package pureframes.tetrio.game.scenes.gameplay.model

// depends on ordinal index
enum RotationState:
  case Spawn, Clockwise, InvertedSpawn, CounterClockwise
object RotationState:
  val size = RotationState.values.size

  def rotate(
      state: RotationState,
      direction: RotationDirection
  ): RotationState =
    RotationState.fromOrdinal(
      math.floorMod((state.ordinal + direction.index), size)
    )

enum RotationDirection(val index: Int):
  case Clockwise        extends RotationDirection(1)
  case CounterClockwise extends RotationDirection(-1)
