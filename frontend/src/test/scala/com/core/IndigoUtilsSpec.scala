package com.core

import indigo.*

class IndigoUtilsSpec extends munit.FunSuite:
    test("-->") {
        val testcases = Batch(
            // format: off
            Vector2(0, 0) --> Vector2(0, 2) -> Batch(Vector2(0, 0), Vector2(0, 1), Vector2(0, 2)),
            Vector2(0, 0) --> Vector2(1, 3) -> Batch(Vector2(0, 0), Vector2(1, 1), Vector2(1, 2), Vector2(1, 3)),
            Vector2(0, 0) --> Vector2(2, 3) -> Batch(Vector2(0, 0), Vector2(1, 1), Vector2(2, 2), Vector2(2, 3)),
            Vector2(0, 0) --> Vector2(0, -2) -> Batch(Vector2(0, 0), Vector2(0, -1), Vector2(0, -2)),
            Vector2(0, 0) --> Vector2(-1, -2) -> Batch(Vector2(0, 0), Vector2(-1, -1), Vector2(-1, -2)),
            Vector2(0, 0) --> Vector2(1, -2) -> Batch(Vector2(0, 0), Vector2(1, -1), Vector2(1, -2)),
            // format: on
        )

        testcases.foreach { (range, expected) => 
            assertEquals(range, expected)
        }
    }