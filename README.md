# Indigo Tetris

## Dev

Run in separate terminal windows
```zsh
npm run serve
```
```zsh
sbt "~ fastOptJS;indigoBuild"
```

TODO:

setup:
- controller setup -> derive all cmds from input state
- add tyrian (?)

- higher walls
- line clear animations
- predict next few tetrominos
- add scores
- stages
- hold mechanic
- block placement preview
- floating blocks falling down (cascade gravity mode)

bugs:
- stop continoous rotation
- skipped rotation animation on move
- rotate + move -> rotate; kills the rotation


nice to have:
- resizing
- centered overlays

ideas: https://www.cmaas.de/tutorial-tetris-with-impact-js