package com.model

import indigo.Point
import indigoextras.geometry.Vertex

extension (underlying: Point)
  def toVertex: Vertex = Vertex.fromPoint(underlying)
