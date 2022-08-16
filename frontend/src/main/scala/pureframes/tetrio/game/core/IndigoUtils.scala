package pureframes.tetrio
package game.core

import indigo.Point
import indigo.Vector2
import indigo.shared.collections.Batch
import indigoextras.geometry.Vertex

import scala.annotation.tailrec
import scala.annotation.targetName

extension (underlying: Point)
  def toVertex: Vertex   = Vertex.fromPoint(underlying)
  def toVector2: Vector2 = Vector2.fromPoint(underlying)

extension [T](underlying: IndexedSeq[T])
  def toBatch: Batch[T] = Batch.fromIndexedSeq(underlying)

extension (underlying: Vector2)
  def tuple: (Double, Double)                 = Tuples.to(underlying)
  def fromTuple(t: (Double, Double)): Vector2 = Tuples.from[Vector2](t)
  def mapCoords(fn: Double => Double): Vector2 =
    Vector2(fn(underlying.x), fn(underlying.y))
  def sameDirectionAs(another: Vector2): Boolean =
    underlying.x.sign == another.x.sign || underlying.y.sign == another.y.sign

  @targetName("vectorRange")
  def -->(end: Vector2): Batch[Vector2] =
    val start = underlying

    @tailrec
    def go(prev: Vector2, acc: Batch[Vector2]): Batch[Vector2] =
      if prev == end then acc
      else
        val next = Vector2(step(end.x, prev.x), step(end.y, prev.y))
        go(next, acc :+ next)
    go(start, Batch(start))

  private def step(end: Double, prev: Double) =
    if end == 0 then 0
    else if prev - 1 >= end then prev - 1
    else if prev + 1 <= end then prev + 1
    else prev

given Conversion[Vertex, Vector2] = _.toVector2
given Conversion[Vector2, Vertex] = Vertex.fromVector2(_)
