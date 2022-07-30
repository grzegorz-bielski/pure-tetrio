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
- add tyrian (?)

features:
- smooth tetromino movement (!!!)

- line clear animations
- predict next few tetrominos
- add scores
- stages
- hold mechanic
- block placement preview
- floating blocks falling down (cascade gravity mode)

bugs:
- I tetromino passes through walls...
- stop continoous rotation
- skipped rotation animation on move
- rotate + move -> rotate; kills the rotation


nice to have:
- move input handlers to scene (?)
- resizing
- centered overlays

ideas: https://www.cmaas.de/tutorial-tetris-with-impact-js