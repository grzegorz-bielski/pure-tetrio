package com

import com.init.*
import com.model.*
import indigo.*
import indigo.scenes.Scene

type GameViewModel = Unit

type GameContext = FrameContext[SetupData]

trait GameScene extends Scene[SetupData, GameModel, GameViewModel]
trait Game extends IndigoGame[BootData, SetupData, GameModel, GameViewModel]

export com.model.GameModel