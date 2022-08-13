package pureframes.tetris
package game.core

import indigo.shared.assets.*
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic

case class Assets(
    tetrominos: Assets.Tetrominos
)
object Assets:
  val assets: Set[AssetType] = Set(
    AssetType.Image(
      Tetrominos.name,
      AssetPath("assets/tetrominos.png")
    )
  )

  case class Tetrominos(size: Int):
    import Tetrominos.*

    val l = graphic(size)
    val j = graphic(size).withCrop(size, 0, size, size)
    val o = graphic(size).withCrop(size * 2, 0, size, size)
    val i = graphic(size).withCrop(size * 3, 0, size, size)
    val s = graphic(size).withCrop(size * 4, 0, size, size)
    val t = graphic(size).withCrop(size * 5, 0, size, size)
    val z = graphic(size).withCrop(size * 6, 0, size, size)
    
    // not actual tetromino piece, but it shares the sprite :shrug
    val wall = graphic(size).withCrop(size * 7, 0, size, size) 

    private def graphic(size: Int) =
      Graphic(0, 0, size, size, Material.Bitmap(name))

  object Tetrominos:
    val name = AssetName("tetrominos")
