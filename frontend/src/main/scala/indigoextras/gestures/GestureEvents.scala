package indigoextras.gestures

import indigo.*

enum GestureEvent extends GlobalEvent:
    case AreaTapped(amount: Int)
    case AreaSwiped(direction: Direction)

enum Direction:
    case Up, Down, Left, Right