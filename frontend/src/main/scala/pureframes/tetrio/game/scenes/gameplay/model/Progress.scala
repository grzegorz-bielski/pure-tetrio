package pureframes.tetrio.game.scenes.gameplay.model

import indigo.*
import pureframes.tetrio.game.core.*

final case class Progress(
    score: Long, // https://harddrop.com/wiki/Back-to-Back ??
    level: Int,
    lines: Int
):

  lazy val fallDelay: Seconds = Progress.fallDelay(level)

  def addFullLines(lines: Int): Progress =
    val nextLines = this.lines + lines
    val nextLevel = math.max(nextLines / 10 - 1, 0)
    val score     = calculateScore(lines)

    Progress(
      lines = nextLines,
      level = nextLevel,
      score = score
    )

  private def calculateScore(lines: Int): Long =
    if lines == 0 then score
    else
      val multiplier = if level == 0 then 1 else level
      // TODO: use iron / refined ?
      val lineScore = lines match
        case 1 => 100
        case 2 => 300
        case 3 => 500
        case 4 => 800

      score + (lineScore * multiplier)

object Progress:
  def initial = Progress(
    score = 0,
    level = 0,
    lines = 0
  )

  def fallDelay(level: Int): Seconds =
    level match
      case 0 => Seconds(1)
      case n =>
        val prev = fallDelay(level - 1)
        prev - prev / 6
