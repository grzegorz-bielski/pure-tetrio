package com.core

import indigo.Point
import indigo.Vector2
import indigo.shared.collections.Batch
import indigoextras.geometry.Vertex

import scala.annotation.tailrec

extension (underlying: Point)
  def toVertex: Vertex = Vertex.fromPoint(underlying)
  def toVector2: Vector2 = Vector2.fromPoint(underlying)

extension (underlying: Vector2)
  // adapted from snake demo
  def -->(end: Vector2): Batch[Vector2] =
    val start = underlying

    @tailrec
    def go(
        last: Vector2,
        dest: Vector2,
        p: Vector2 => Boolean,
        acc: Batch[Vector2]
    ): Batch[Vector2] =
      if p(last) then acc
      else
        val next =
          Vector2(
            x = if (last.x + 1 <= end.x) last.x + 1 else last.x,
            y = if (last.y + 1 <= end.y) last.y + 1 else last.y
          )
        go(next, dest, p, acc :+ next)

    if lessThanOrEqual(start, end) then go(start, end, _ == end, Batch(start))
    else go(end, start, _ == start, Batch(end))

  private def lessThanOrEqual(a: Vector2, b: Vector2): Boolean =
    a.x <= b.x && a.y <= b.y


given Conversion[Vertex, Vector2] = _.toVector2
given Conversion[Vector2, Vertex] = Vertex.fromVector2(_)