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
- block placement preview
- line clear animations
- game over animations
- live leader board
- cascade gravity mode (?)

bugs:
- debounce resizing (!)
    - refactor subs to fs2
    - use new `Sub.make(id: String, fs2.Stream[F, A])` constructor

UI:
- add gesture controls
- remove screen controls
- centered overlays

setup:
- split vendor chunk from app chunks (borked)
- add sourcemaps (borked)
- move vite-pure-css plugin to pure-css repo and publish
- use std and node types from scalablytyped
- hot-reload (?)
    - https://github.com/scala-js/scala-js/issues/4643
- sbt task for stylesheets gen?

ideas: 
- https://tetr.io/