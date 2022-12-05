package pureframes.tetrio.app.components

import pureframes.css.*
import pureframes.tetrio.app.AppMsg
import pureframes.tetrio.game.scenes.gameplay.GameplayCommand
import pureframes.tetrio.game.scenes.gameplay.model.RotationDirection
import tyrian.Html.*
import tyrian.*

object ScreenControls extends Styled:
  def view: Html[AppMsg] =
    div(
      clsx(containerStyles)
    )(
      // format: off
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.Rotate(RotationDirection.CounterClockwise))))(rotateCounterClockwise),
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.Rotate(RotationDirection.Clockwise))))(rotateClockwise),
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.MoveLeft)))(arrowLeft),
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.MoveDown)))(arrowDown),
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.HardDrop)))(arrowUp),
      ControlBtn(onClick(AppMsg.Input(GameplayCommand.MoveRight)))(arrowRight)
      // format: on
    )

  val containerStyles = css"""
    position: absolute;
    bottom: 0;
    right: 0;
  """

  def ControlBtn[M](attributes: Attr[M]*)(children: Elem[M]*): Html[M] =
    button(
      (clsx(buttonStyles)) +: attributes.toList
    )(children.toList)

  val buttonStyles = css"""
    touch-action: manipulation;
    user-select: none;
    -webkit-user-select: none;
    cursor: pointer;

    background: none;
    border: none;

    width: 3rem;
    height: 3rem;

    padding: 0;
    margin: 0.5rem;
  """

  val sharedArrowStyles = css"""
    fill: var(--text-white);
  """

  val rotateClockwise = rotatingArrow(None)
  val rotateCounterClockwise = rotatingArrow(
    Some(css"""transform: scaleX(-1);""")
  )

  def rotatingArrow(css: Option[Css]) =
    import tyrian.SVG.*

    svg(
      xmlns   := "http://www.w3.org/2000/svg",
      viewBox := "0 0 512 512",
      clsx(sharedArrowStyles, css)
    )(
      path(
        d := "M496 48V192c0 17.69-14.31 32-32 32H320c-17.69 0-32-14.31-32-32s14.31-32 32-32h63.39c-29.97-39.7-77.25-63.78-127.6-63.78C167.7 96.22 96 167.9 96 256s71.69 159.8 159.8 159.8c34.88 0 68.03-11.03 95.88-31.94c14.22-10.53 34.22-7.75 44.81 6.375c10.59 14.16 7.75 34.22-6.375 44.81c-39.03 29.28-85.36 44.86-134.2 44.86C132.5 479.9 32 379.4 32 256s100.5-223.9 223.9-223.9c69.15 0 134 32.47 176.1 86.12V48c0-17.69 14.31-32 32-32S496 30.31 496 48z"
      )
    )

  val arrowLeft  = arrow(direction("left"))
  val arrowRight = arrow(direction("right"))
  val arrowDown  = arrow(direction("down"))
  val arrowUp    = arrow(direction("up"))

  inline def direction(inline dir: "up" | "left" | "right" | "down") =
    val rotation =
      inline dir match
        case d: "up"    => "-90"
        case d: "right" => "0"
        case d: "down"  => "90"
        case d: "left"  => "180"

    css"""
      transform: rotate(${rotation}deg);
    """

  def arrow(css: Css) =
    import tyrian.SVG.*

    svg(
      xmlns   := "http://www.w3.org/2000/svg",
      x       := "0",
      y       := "0",
      viewBox := "0 0 31.143 31.143",
      clsx(sharedArrowStyles, css)
    )(
      path(
        d := "m0 15.571c1e-3 1.702 1.383 3.081 3.085 3.083l17.528-2e-3 -4.738 4.739c-1.283 1.284-1.349 3.301-0.145 4.507 1.205 1.201 3.222 1.138 4.507-0.146l9.896-9.898c1.287-1.283 1.352-3.301 0.146-4.506-0.033-0.029-0.068-0.051-0.1-0.08-0.041-0.043-0.07-0.094-0.113-0.139l-9.764-9.762c-1.268-1.266-3.27-1.316-4.474-0.111-1.205 1.205-1.153 3.208 0.111 4.476l4.755 4.754h-17.609c-1.704-1e-3 -3.085 1.379-3.085 3.085z"
      )
    )
