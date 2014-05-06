/* 
 * CVS identifier:
 * 
 * $Id: MultiResImgData.java,v 1.8 2000/09/05 09:26:25 grosbois Exp $
 * 
 * Class:                   MultiResImgData
 * 
 * Description:             The interface for classes that provide
 *                          multi-resolution image data.
 * 
 * 
 * 
 * COPYRIGHT:
 * 
 * This software module was originally developed by Raphaël Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askelöf (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, Félix Henry, Gerard Mozelle and Patrice Onno (Canon Research
 * Centre France S.A) in the course of development of the JPEG2000
 * standard as specified by ISO/IEC 15444 (JPEG 2000 Standard). This
 * software module is an implementation of a part of the JPEG 2000
 * Standard. Swiss Federal Institute of Technology-EPFL, Ericsson Radio
 * Systems AB and Canon Research Centre France S.A (collectively JJ2000
 * Partners) agree not to assert against ISO/IEC and users of the JPEG
 * 2000 Standard (Users) any of their rights under the copyright, not
 * including other intellectual property rights, for this software module
 * with respect to the usage by ISO/IEC and Users of this software module
 * or modifications thereof for use in hardware or software products
 * claiming conformance to the JPEG 2000 Standard. Those intending to use
 * this software module in hardware or software products are advised that
 * their use may infringe existing patents. The original developers of
 * this software module, JJ2000 Partners and ISO/IEC assume no liability
 * for use of this software module or modifications thereof. No license
 * or right to this software module is granted for non JPEG 2000 Standard
 * conforming products. JJ2000 Partners have full right to use this
 * software module for his/her own purpose, assign or donate this
 * software module to any third party and to inhibit third parties from
 * using this software module for non JPEG 2000 Standard conforming
 * products. This copyright notice must be included in all copies or
 * derivative works of this software module.
 * 
 * Copyright (c) 1999/2000 JJ2000 Partners.
 * 
 * 
 * 
 */


package jj2000.j2k.wavelet.synthesis;

import jj2000.j2k.image.*;

/**
 * This interface defines methods to access image attributes (width, height,
 * number of components, etc.) of multiresolution images, such as those
 * resulting from an inverse wavelet transform. The image can be tiled or not
 * (i.e. if the image is not tiled then there is only 1 tile). It should be
 * implemented by all classes that provide multi-resolution image data, such
 * as entropy decoders, dequantizers, etc. This interface, however, does not
 * define methods to transfer image data (i.e. pixel data), that is defined by
 * other interfaces, such as 'CBlkQuantDataSrcDec'.
 *
 * <P>This interface is very similar to the 'ImgData' one. It differs only
 * by the fact that it handles multiple resolutions.
 *
 * <P>Resolution levels are counted from 0 to L. Resolution level 0 is the
 * lower resolution, while L is the maximum resolution level, or full
 * resolution, which is returned by 'getMaxResLvl()'. Note that there are L+1
 * resolution levels available.
 *
 * <P>As in the 'ImgData' interface a multi-resolution image lies on top of
 * a canvas. The canvas coordinates are mapped from the full resolution
 * reference grid (i.e. resolution level 'L' reference grid) to a resolution
 * level 'l' reference grid by '(x_l,y_l) =
 * (ceil(x_l/2^(L-l)),ceil(y_l/2^(L-l)))', where '(x,y)' are the full
 * resolution reference grid coordinates and '(x_l,y_l)' are the level 'l'
 * reference grid coordinates.
 *
 * <P>For details on the canvas system and its implications consult the
 * 'ImgData' interface.
 *
 * <P>Note that tile sizes may not be obtained by simply dividing the tile
 * size in the reference grid by the subsampling factor.
 *
 * @see ImgData
 *
 * @see jj2000.j2k.quantization.dequantizer.CBlkQuantDataSrcDec
 * */
public interface MultiResImgData {

//      /**
//       * Returns the maximum resolution level available from the
//       * bit stream. The number of resolution levels available is the
//       * returned value plus 1. When tile-components have different
//       * maximum number of resolution levels, it returns the lowest one.
//       *
//       * @return The maximum resolution level available for the current
//       * tile
//       *
//       *
//       * */
//      public int getMaxResLvl();

    /**
     * Returns the overall width of the current tile in pixels for the
     * given resolution level. This is the tile's width without
     * accounting for any component subsampling. The resolution level
     * is indexed from the lowest number of resolution levels of all
     * components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The total current tile's width in pixels.
     *
     *
     * */
    public int getWidth(int rl);

    /**
     * Returns the overall height of the current tile in pixels, for
     * the given resolution level. This is the tile's height without
     * accounting for any component subsampling. The resolution level
     * is indexed from the lowest number of resolution levels of all
     * components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The total current tile's height in pixels.
     *
     *
     * */
    public int getHeight(int rl);

    /**
     * Returns the overall width of the image in pixels, for the given
     * resolution level. This is the image's width without accounting
     * for any component subsampling or tiling. The resolution level
     * is indexed from the lowest number of resolution levels of all
     * components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The total image's width in pixels.
     *
     *
     * */
    public int getImgWidth(int rl);

    /**
     * Returns the overall height of the image in pixels, for the
     * given resolution level. This is the image's height without
     * accounting for any component subsampling or tiling. The
     * resolution level is indexed from the lowest number of
     * resolution levels of all components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The total image's height in pixels.
     *
     *
     * */
    public int getImgHeight(int rl);

    /**
     * Returns the number of components in the image.
     *
     * @return The number of components in the image.
     *
     *
     * */
    public int getNumComps();

    /**
     * Returns the component subsampling factor in the horizontal direction,
     * for the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The horizontal subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsX(int n);

    /**
     * Returns the component subsampling factor in the vertical direction, for
     * the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The vertical subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsY(int n);

    /**
     * Returns the width in pixels of the specified component
     * in the current tile, for the given resolution level.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The width in pixels of component <tt>n</tt> in the
     * current tile.
     *
     *
     * */
    public int getCompWidth(int n, int rl);

    /**
     * Returns the height in pixels of the specified component
     * in the current tile, for the given resolution level.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The height in pixels of component <tt>n</tt> in the
     * current tile.
     *
     *
     * */
    public int getCompHeight(int n, int rl);

    /**
     * Returns the width in pixels of the specified component
     * in the overall image, for the given resolution level.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The width in pixels of component <tt>n</tt> in the
     * overall image.
     *
     *
     * */
    public int getCompImgWidth(int n, int rl);

    /**
     * Returns the height in pixels of the specified component
     * in the overall image, for the given resolution level.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The height in pixels of component <tt>n</tt> in the
     * overall image.
     *
     *
     * */
    public int getCompImgHeight(int n, int rl);

    /**
     * Changes the current tile, given the new indexes. An
     * IllegalArgumentException is thrown if the indexes do not
     * correspond to a valid tile.
     *
     * @param x The horizontal indexes the tile.
     *
     * @param y The vertical indexes of the new tile.
     *
     *
     * */
    public void setTile(int x, int y);

    /**
     * Advances to the next tile, in standard scan-line order (by rows
     * then columns). An NoNextElementException is thrown if the
     * current tile is the last one (i.e. there is no next tile).
     *
     *
     * */
    public void nextTile();

    /**
     * Returns the indexes of the current tile. These are the
     * horizontal and vertical indexes of the current tile.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The current tile's indexes (vertical and horizontal
     * indexes).
     *
     *
     * */
    public Coord getTile(Coord co);

    /**
     * Returns the index of the current tile, relative to a standard
     * scan-line order.
     *
     * @return The current tile's index (starts at 0).
     *
     *
     * */
    public int getTileIdx();

    /**
     * Returns the horizontal and vertical offset of the upper-left corner of
     * the current tile, in the specified component, relative to the canvas
     * origin, for the specified resolution level. This is returned in the
     * component coordinates (not in the reference grid coordinates) reduced
     * to the specified resolution. These are the coordinates of the current
     * tile's (not active tile) upper-left corner relative to the canvas, in
     * the specified component, at the specified resolution.
     *
     * @param co If not null the object is used to return the values,
     * if null a new one is created and returned.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The horizontal and vertical offsets of the upper-left
     * corner of the current tile, for the specified component and
     * resolution level, relative to the canvas origin, in the component
     * coordinates.
     *
     *
     * */
    public Coord getTileOff(Coord co, int n, int rl);

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component and resolution level. This is
     * actually the horizontal coordinate of the top-left corner of the image
     * data within the current tile.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The horizontal coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates, at resolution level 'rl'.
     *
     *
     * */
    public int getULX(int n, int rl);

    /**
     * Returns the vertical coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component and resolution level. This is
     * actually the vertical coordinate of the top-left corner of the image
     * data within the current tile.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates, at resolution level 'rl'.
     *
     *
     * */
    public int getULY(int n, int rl);

    /**
     * Returns the horizontal coordinate of the image origin, the
     * top-left corner, in the canvas system, on the reference grid at
     * the specified resolution level.  The resolution level is
     * indexed from the lowest number of resolution levels of all
     * components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULX(int rl);

    /**
     * Returns the vertical coordinate of the image origin, the
     * top-left corner, in the canvas system, on the reference grid at
     * the specified resolution level.  The resolution level is
     * indexed from the lowest number of resolution levels of all
     * components of the current tile.
     *
     * @param rl The resolution level, from 0 to L.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULY(int rl);

    /**
     * Returns the number of tiles in the horizontal and vertical directions.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The number of tiles in the horizontal (Coord.x) and
     * vertical (Coord.y) directions.
     *
     *
     * */
    public Coord getNumTiles(Coord co);

    /**
     * Returns the total number of tiles in the image.
     *
     * @return The total number of tiles in the image.
     *
     *
     * */
    public int getNumTiles();

}
