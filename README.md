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

bugs:
- debounce resizing

UI:
- on screen controls (?)
- centered overlays

ideas: 
- https://www.cmaas.de/tutorial-tetris-with-impact-js
- https://tetr.io/