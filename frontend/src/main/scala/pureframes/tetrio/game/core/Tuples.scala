package pureframes.tetrio.game.core

import scala.deriving.*

// blindly copied from:
// https://taig.medium.com/converting-between-tuples-and-case-classes-in-scala-3-7079ccedf4c0
// see also: https://www.scala-lang.org/2021/02/26/tuples-bring-generic-programming-to-scala-3.html
object Tuples:
  def to[A <: Product](value: A)(using
      mirror: Mirror.ProductOf[A]
  ): mirror.MirroredElemTypes =
    Tuple.fromProductTyped(value)
  def from[A](value: Product)(using
      mirror: Mirror.ProductOf[A],
      ev: value.type <:< mirror.MirroredElemTypes
  ): A =
    mirror.fromProduct(value)
