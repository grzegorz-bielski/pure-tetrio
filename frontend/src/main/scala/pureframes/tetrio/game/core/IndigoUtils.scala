package pureframes.tetrio.game.core

import cats.kernel.Monoid
import indigo.Point
import indigo.Vector2
import indigo.shared.collections.Batch
import indigoextras.geometry.Vertex

import scala.annotation.tailrec
import scala.annotation.targetName

import scalajs.js

extension (underlying: Point)
  def toVertex: Vertex   = Vertex.fromPoint(underlying)
  def toVector2: Vector2 = Vector2.fromPoint(underlying)

extension [T](underlying: IndexedSeq[T])
  def toBatch: Batch[T] = Batch.fromIndexedSeq(underlying)

extension [T](underlying: Option[T])
  def toBatch: Batch[T] = Batch.fromOption(underlying)

extension [T](underlying: Array[T])
  def toBatch: Batch[T] = Batch.fromArray(underlying)

extension [T](underlying: js.Array[T])
  def toBatch: Batch[T] = Batch.fromJSArray(underlying)

given [A]: Monoid[Batch[A]] =
  Monoid.instance(Batch.empty, _ ++ _)

extension (underlying: Vector2)
  def tuple: (Double, Double)                 = Tuples.to(underlying)
  def fromTuple(t: (Double, Double)): Vector2 = Tuples.from[Vector2](t)
  def toVertex: Vertex                        = Vertex.fromVector2(underlying)
  def mapCoords(fn: Double => Double): Vector2 =
    Vector2(fn(underlying.x), fn(underlying.y))
  def sameDirectionAs(another: Vector2): Boolean =
    // TODO: can this be smarter ?
    (underlying.x.sign == another.x.sign && another.x != 0) ||
      (underlying.y.sign == another.y.sign && another.y != 0)

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
