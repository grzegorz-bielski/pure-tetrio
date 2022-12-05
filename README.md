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
- debounce resizing (!)
    - refactor subs to fs2
    - use new `Sub.make(id: String, fs2.Stream[F, A])` constructor

UI:
- on screen controls (?)
- centered overlays

setup:
- split vendor chunk from app chunks (borked)
- add sourcemaps (borked)
- move vite-pure-css plugin to pure-css repo and publish

ideas: 
- https://www.cmaas.de/tutorial-tetris-with-impact-js
- https://tetr.io/