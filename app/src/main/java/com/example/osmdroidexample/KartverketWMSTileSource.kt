package com.example.osmdroidexample

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.MapTileIndex
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sinh

open class KartverketWMSTileSource(
    aName: String?,
    aZoomMinLevel: Int,
    aZoomMaxLevel: Int,
    aTileSizePixels: Int,
    aImageFilenameEnding: String?,
    aBaseUrl: Array<out String>?,
    val layers: String,
    val version: String,
    val srs: String
) : OnlineTileSourceBase(
    aName,
    aZoomMinLevel,
    aZoomMaxLevel,
    aTileSizePixels,
    aImageFilenameEnding,
    aBaseUrl
) {

    companion object {
        val WMS_FORMAT_STRING = "%s" +
                "service=wms" +
                "&version=%s" +
                "&request=GetMap" +
                "&layers=%s" +
                "&srs=%s" +
                "&bbox=%f,%f,%f,%f" +
                "&format=image/png" +
                "&width=%s" +
                "&height=%s"

        const val MAP_SIZE = 20037508.34789244 * 2
        val TILE_ORIGIN = doubleArrayOf(-20037508.34789244, 20037508.34789244)
        var MINX = 0
        var MAXX = 1
        var MINY = 2
        var MAXY = 3
        var ORIG_X = 0
        var ORIG_Y = 1

        fun tile2boundingBox(x: Int, y: Int, zoom: Int): BoundingBox {
            return BoundingBox(tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom), tile2lon(x, zoom))
        }

        private fun tile2lon(x: Int, z: Int): Double {
            return x.toDouble() / 2.0.pow(z.toDouble()) * 360.0 - 180
        }

        private fun tile2lat(y: Int, z: Int): Double {
            val n = Math.PI - (2.0 * Math.PI * y.toDouble()) / 2.0.pow(z.toDouble())
            return Math.toDegrees(atan(sinh(n)))
        }
    }



    override fun getTileURLString(pMapTileIndex: Long): String {

        val bbox = tile2boundingBox(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex))
        //val bbox = getBoundingBox(MapTileIndex.getX(pMapTileIndex), MapTileIndex.getY(pMapTileIndex), MapTileIndex.getZoom(pMapTileIndex))

        val tileUrlStr = WMS_FORMAT_STRING.format(baseUrl, version, layers, srs,
            bbox.lonWest, bbox.latSouth, bbox.lonEast, bbox.latNorth,
            tileSizePixels, tileSizePixels)

        return tileUrlStr
    }

    fun getBoundingBox(x: Int, y: Int, zoom: Int): DoubleArray {
        val tileSize = MAP_SIZE / 2.0.pow(zoom.toDouble())
        val minx = TILE_ORIGIN[ORIG_X] + x * tileSize
        val maxx = TILE_ORIGIN[ORIG_X] + (x + 1) * tileSize
        val miny = TILE_ORIGIN[ORIG_Y] - (y + 1) * tileSize
        val maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize

        val bbox = DoubleArray(4)
        bbox[MINX] = minx
        bbox[MINY] = miny
        bbox[MAXX] = maxx
        bbox[MAXY] = maxy

        return bbox
    }


}