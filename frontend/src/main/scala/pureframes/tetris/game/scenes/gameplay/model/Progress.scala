package pureframes.tetris.game.scenes.gameplay.model

case class Progress(
    score: Int,
    level: Int,
    lines: Int
)
object Progress:
    def initial = Progress(
        score = 0,
        level = 0,
        lines = 0,
    )