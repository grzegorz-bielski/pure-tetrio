package com.model

import munit.FunSuite

class RotationSpec extends FunSuite:
    test("rotates the state correctly") {
        val states = Vector(
            // Rotation.State.Spawn,
            Rotation.State.Clockwise,
            Rotation.State.InvertedSpawn,
            Rotation.State.CounterClockwise,
            Rotation.State.Spawn
        )

        states.foldLeft(Rotation.State.Spawn) { (state, expected) => 
           val next =  state.rotateClockwise
           assertEquals(next, expected)
           next
        }
    }