# Pure Tetrio

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
```zsh
npm run build
```

TODO:

features:
- add levels 
    - (https://tetris.fandom.com/wiki/Tetris_(NES,_Nintendo))
    - https://harddrop.com/wiki/Back-to-Back
- add scores (https://tetris.wiki/Scoring - BPS ?)
- line clear animations
- predict next few tetrominos (?)
- stages (?)
- block placement preview
- hold mechanic
- floating blocks falling down (cascade gravity mode)

infra:
- CI/CD & hosting - fe -> static site

bugs:
- 0 blocks from the floor & after rotation - the descent time is discarded and tetromino is placed instantly (!)
- stop continuous rotation
- debounce resizing
<!-- - skipped rotation animation on move -->
<!-- - rotate + move -> rotate; kills the rotation -->

UI:
- on screen controls (?)
- centered overlays

ideas: 
- https://www.cmaas.de/tutorial-tetris-with-impact-js
- https://tetr.io/