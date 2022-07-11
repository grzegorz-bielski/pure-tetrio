package com.core

import scala.annotation.targetName

extension [A, C](f: Function1[A, C])
  @targetName("fnProduct")
  def |*|[B, D](g: B => D): (A, B) => (C, D) =
    (a: A, b: B) => (f(a), g(b))

def const[A](a: A): Any => A = _ => a
