package indigoextras.gestures

import indigo.*

enum GestureEvent extends GlobalEvent:
    case Tapped(amount: Int)
    case Swiped(direction: Direction)
    case Panned(direction: Direction)

enum Direction:
    case Up, Down, Left, Right