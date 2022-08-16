package pureframes.tetrio.game.scenes.gameplay.model

import pureframes.tetrio.game.core.*

final case class Progress(
    score: Int, // https://harddrop.com/wiki/Back-to-Back ??
    level: Int,
    lines: Int
):

  def addFullLines(lines: Int): Progress =
    val nextLines = this.lines + lines
    // based on formula from:
    // https://tetrio.fandom.com/wiki/Tetris_(NES,_Nintendo)
    val nextLevel = math.max(nextLines / 10 - 1, 0)

    copy(
      lines = nextLines,
      level = nextLevel
    )
object Progress:
  def initial = Progress(
    score = 0,
    level = 0,
    lines = 0
  )
