/*
 * CVS Identifier:
 *
 * $Id: ImgData.java,v 1.8 2000/09/05 09:24:04 grosbois Exp $
 *
 * Interface:           ImgData
 *
 * Description:         The interface for classes that provide image
 *                      data.
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

package jj2000.j2k.image;

/**
 * This interface defines methods to access image attributes (width, height,
 * number of components, etc.). The image can be tiled or not (i.e. if the
 * image is not tiled then there is only 1 tile). It should be implemented by
 * all classes that provide image data, such as image file readers, color
 * transforms, wavelet transforms, etc. This interface, however, does not
 * define methods to transfer image data (i.e. pixel data), that is defined by 
 * other interfaces, such as 'BlkImgDataSrc'.
 *
 * <P>The image itself is considered to lie on top of a canvas, and thus its
 * top-left corner can be positioned at a coordinate other than (0,0). This
 * canvas is partitioned into tiles (1 tile if there are no tiles). The origin
 * for the partitioning into tiles might be different from the image
 * origin. Since the tiles are a partition of the canvas, and not of the image
 * itself, it is worth noting that some tiles (at the top, bottom, left or
 * right) can partially lie outside of the image data. We call "active tile"
 * the part of a tile that contains actual image data.
 *
 * <P>An image might be composed of several components (numbered from 0 to
 * N-1), where each component can have a different resolution. We therefore
 * define a reference grid, on which all components are layed down. Each
 * component has an horizontal and a vertical subsampling factor, which
 * determines the ratio of the grid's horizontal and vertical dimensions to
 * the component's horizontal and vertical dimensions, respectively. Therefore
 * if 'W' and 'H' are the width and height of the reference grid, 'Rx' and
 * 'Ry' the horizontal and vertical subsampling factors, respectively, and
 * 'ax' and 'ay' are the horizontal and vertical coordinates of the top-left
 * corner of the reference grid with respect to the canvas origin, the width
 * of the component is 'ceil((W+ax)/Rx)-ceil(ax/Rx)' and its height is
 * 'ceil((H+ay)/Ry)-ceil(ay/Ry)'. Note that this formulae is not directly
 * applicable to the tile sizes. In general, a coordinate in the reference
 * grid is mapped to a component coordinate as 'x_c = ceil(x/Rx)' and 'y_c =
 * ceil(y/Ry)', where '(x_c,y_c)' are the component coordinates, '(x,y)' the
 * reference grid coordinates, both in the canvas system, and 'Rx' and 'Ry'
 * the horizontal and vertical subsampling factors, respectively.
 *
 * <P>The reference grid dimensions are commonly referred to as "image" width
 * and height, in contrast to component width and height. The 'getCompSubsX()'
 * and 'getCompSubsY()' methods return the componet's subsampling factors.
 *
 * <P>The image origin '(ax,ay)' is returned by the 'getImgULX()' and
 * 'getImgULY()' methods.
 *
 * <P>There is always one current tile. All data, coordinates and dimensions,
 * always refer to the current active tile, unless otherwise specified. The
 * current tile may be changed with the 'nextTile()' or 'setTile()' methods.
 *
 * <P>The coordinates (i.e. indexes) of the top-left tile are (0,0), for the
 * other tiles the coordinates increase by one for each new tile (e.g., tile
 * (1,2) is the second tile in the horizontal direction and the third tile in
 * the vertical direction).
 *
 * <P>The coordinates of the top-left corner of each tile (and not of the
 * active tile), with respect to the canvas component coordinates, is returned
 * by the 'getTileOff()' method (these are component coordinates, not reference
 * grid coordinates). For the top-left tile (i.e. tile (0,0)) this is the
 * coordinates of the partitioning reference, in component coordinates. The
 * coordinates of the top-left corner of the active tile, with respect to the
 * tile, is returned by the 'getULX()' and 'getULY()' methods. All other
 * coordinates, unless otherwise specified, are defined with respect to the
 * active tile top-left corner (e.g., (0,0) is always the top-left corner of
 * the current active tile) and are component coordinates (not reference grid
 * coordinates).
 *
 * <P>As mentoned before the origin of tile partitioning might not be the
 * image origin, nor the canvas one. The origin of the tile partition is a
 * point with coordinates '(Px,Py)' in the canvas system, in the reference grid
 * coordinates, where 'Px<=ax' and 'Py<=ay', where '(ax,ay)' is the image
 * origin in tha canvas system, in the reference grid coordinates. The
 * '(Px,Py)' point is the upper-left corner of the tile '(0,0)' in the canvas
 * coordinate system, in the reference grid, which is not what the
 * 'getTileOff()' method returns (this method returns the coordinates of the
 * same point but in component coordinates).
 *
 * <P>Note that all other partitions (e.g., code-blocks) are done
 * with respect a special origin, different from the above ones, which is not
 * specified in the 'ImgData' interface.
 *
 * @see BlkImgDataSrc
 * */
public interface ImgData {

    /**
     * Returns the overall width of the current tile in pixels. This is the
     * tile's width without accounting for any component subsampling. This is
     * also referred as the reference grid width in the current tile.
     *
     * @return The total current tile's width in pixels.
     *
     *
     * */
    public int getWidth();

    /**
     * Returns the overall height of the current tile in pixels. This is the
     * tile's height without accounting for any component subsampling. This is
     * also referred as the reference grid height in the current tile.
     *
     * @return The total current tile's height in pixels.
     *
     *
     * */
    public int getHeight();

    /**
     * Returns the overall width of the image in pixels. This is the
     * image's width without accounting for any component subsampling or
     * tiling.
     *
     * @return The total image's width in pixels.
     *
     *
     * */
    public int getImgWidth();

    /**
     * Returns the overall height of the image in pixels. This is the
     * image's height without accounting for any component subsampling or
     * tiling.
     *
     * @return The total image's height in pixels.
     *
     *
     * */
    public int getImgHeight();

    /**
     * Returns the number of components in the image.
     *
     * @return The number of components in the image.
     *
     *
     *
     */
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
     * Returns the width in pixels of the specified component in the
     * current tile.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the current
     * tile.
     *
     *
     * */
    public int getCompWidth(int n);

    /**
     * Returns the height in pixels of the specified component in the
     * current tile.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the current
     * tile.
     *
     *
     * */
    public int getCompHeight(int n);

    /**
     * Returns the width in pixels of the specified component in the
     * overall image.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgWidth(int n);

    /**
     * Returns the height in pixels of the specified component in the
     * overall image.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgHeight(int n);

    /**
     * Returns the number of bits, referred to as the "range bits",
     * corresponding to the nominal range of the image data in the specified
     * component. If this number is <i>n</b> then for unsigned data the
     * nominal range is between 0 and 2^b-1, and for signed data it is between
     * -2^(b-1) and 2^(b-1)-1. In the case of transformed data which is not in
     * the image domain (e.g., wavelet coefficients), this method returns the
     * "range bits" of the image data that generated the coefficients.
     *
     * @param n The index of the component.
     *
     * @return The number of bits corresponding to the nominal range
     * of the image data (in the image domain).
     *
     *
     * */
    public int getNomRangeBits(int n);

    /**
     * Changes the current tile, given the new indices. An
     * IllegalArgumentException is thrown if the coordinates do not
     * correspond to a valid tile.
     *
     * @param x The horizontal index of the tile.
     *
     * @param y The vertical index of the new tile.
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
     * Returns the indixes of the current tile. These are the
     * horizontal and vertical indexes of the current tile.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The current tile's indices (vertical and horizontal
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
     * origin, in the component coordinates (not in the reference grid
     * coordinates). These are the coordinates of the current tile's (not
     * active tile) upper-left corner relative to the canvas.
     *
     * @param co If not null the object is used to return the values,
     * if null a new one is created and returned.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The horizontal and vertical offsets of the upper-left
     * corner of the current tile, for the specified component,
     * relative to the canvas origin, in the component coordinates.
     *
     *
     * */
    public Coord getTileOff(Coord co, int n);

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The horizontal coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULX(int n);

    /**
     * Returns the vertical coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULY(int n);

    /**
     * Returns the horizontal coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULX();

    /**
     * Returns the vertical coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULY();

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
