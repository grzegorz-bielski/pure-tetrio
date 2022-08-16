package pureframes.tetrio
package game.core

import indigo.Vector2

final case class Matrix2(private val mat: Array[Double]) derives CanEqual:
  def *(v: Vector2): Vector2 =
    val (a, b, c, d) = (mat(0), mat(1), mat(2), mat(3))

    Vector2(
      a * v.x + b * v.y,
      c * v.x + d * v.y
    )

object Matrix2:
  def apply(
      row0: (Double, Double),
      row1: (Double, Double)
  ): Matrix2 = Matrix2(
    Array(row0._1, row0._2, row1._1, row1._2)
  )
