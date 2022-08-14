# Pure Tetris

## Dev

Run in separate terminal windows
```zsh
npm run dev
```
```zsh
sbt "dev"
```

## Prod
```zsh
sbt "build"
```
````zsh
npm run build
```

TODO:

features:
- add levels (https://tetris.fandom.com/wiki/Tetris_(NES,_Nintendo))
- add scores (https://tetris.wiki/Scoring - BPS ?)
- line clear animations
- predict next few tetrominos (?)
- stages (?)
- block placement preview
- hold mechanic
- floating blocks falling down (cascade gravity mode)

bugs:
- stop continoous rotation
<!-- - skipped rotation animation on move -->
<!-- - rotate + move -> rotate; kills the rotation -->


UI:
- resizing
- on screen controls (?)
- centered overlays

ideas: https://www.cmaas.de/tutorial-tetris-with-impact-js