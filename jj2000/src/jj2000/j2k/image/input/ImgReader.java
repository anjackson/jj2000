/*
 * CVS identifier:
 *
 * $Id: ImgReader.java,v 1.8 2000/11/27 14:57:29 grosbois Exp $
 *
 * Class:                   ImgReader
 *
 * Description:             Generic interface for image readers (from
 *                          file or other resource)
 *
 *
 *
 * COPYRIGHT:
 * 
 * This software module was originally developed by Rapha�l Grosbois and
 * Diego Santa Cruz (Swiss Federal Institute of Technology-EPFL); Joel
 * Askel�f (Ericsson Radio Systems AB); and Bertrand Berthelot, David
 * Bouchard, F�lix Henry, Gerard Mozelle and Patrice Onno (Canon Research
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
 */
package jj2000.j2k.image.input;

import jj2000.j2k.image.*;
import jj2000.j2k.*;
import java.io.*;

/**
 * This is the generic interface to be implemented by all image file (or other
 * resource) readers for different image file formats.
 *
 * <P>An ImgReader behaves as an ImgData object. Whenever image data is
 * requested through the getInternCompData() or getCompData() methods, the
 * image data will be read (if it is not buffered) and returned. Implementing
 * classes should not buffer large amounts of data, so as to reduce memory
 * usage.
 *
 * <P>This class sets the image origin to (0,0). All default implementations
 * of the methods assume this.
 *
 * <P>This class provides default implementations of many methods. These
 * default implementations assume that there is no tiling (i.e., the only tile
 * is the entire image), that the image origin is (0,0) in the canvas system
 * and that there is no component subsampling (all components are the same
 * size), but they can be overloaded by the implementating class if need be.
 * */
public abstract class ImgReader implements BlkImgDataSrc {

    /** The width of the image */
    protected int w;

    /** The height of the image */
    protected int h;

    /** The number of components in the image */
    protected int nc;

    /**
     * Closes the underlying file or network connection from where the
     * image data is being read.
     *
     * @exception IOException If an I/O error occurs.
     */
    public abstract void close() throws IOException;

    /**
     * Returns the width of the current tile in pixels, assuming there is
     * no-tiling. Since no-tiling is assumed this is the same as the width of
     * the image. The value of <tt>w</tt> is returned.
     *
     * @return The total image width in pixels.
     * */
    public int getWidth() {
        return w;
    }

    /**
     * Returns the overall height of the current tile in pixels, assuming
     * there is no-tiling. Since no-tiling is assumed this is the same as the
     * width of the image. The value of <tt>h</tt> is returned.
     *
     * @return The total image height in pixels.  */
    public int getHeight() {
        return h;
    }

    /**
     * Returns the overall width of the image in pixels. This is the image's
     * width without accounting for any component subsampling or tiling. The
     * value of <tt>w</tt> is returned.
     *
     * @return The total image's width in pixels.
     * */
    public int getImgWidth() {
        return w;
    }

    /**
     * Returns the overall height of the image in pixels. This is the image's
     * height without accounting for any component subsampling or tiling. The
     * value of <tt>h</tt> is returned.
     *
     * @return The total image's height in pixels.
     * */
    public int getImgHeight() {
        return h;
    }

    /**
     * Returns the number of components in the image. The value of <tt>nc</tt>
     * is returned.
     *
     * @return The number of components in the image.
     * */
    public int getNumComps() {
        return nc;
    }

    /**
     * Returns the component subsampling factor in the horizontal direction,
     * for the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * @param c The index of the component (between 0 and C-1)
     *
     * @return The horizontal subsampling factor of component 'c'
     *
     * @see ImgData
     * */
    public int getCompSubsX(int c) {
        return 1;
    }

    /**
     * Returns the component subsampling factor in the vertical direction, for
     * the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * @param c The index of the component (between 0 and C-1)
     *
     * @return The vertical subsampling factor of component 'c'
     *
     * @see ImgData
     * */
    public int getCompSubsY(int c) {
        return 1;
    }

    /**
     * Returns the width in pixels of the specified component in the current
     * tile. This default implementation assumes no tiling and no component
     * subsampling (i.e., all components, or components, have the same
     * dimensions in pixels).
     *
     * @param c The index of the component, from 0 to C-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the current
     * tile.
     * */
    public int getCompWidth(int n) {
        return w;
    }

    /**
     * Returns the height in pixels of the specified component in the current
     * tile. This default implementation assumes no tiling and no component
     * subsampling (i.e., all components, or components, have the same
     * dimensions in pixels).
     *
     * @param c The index of the component, from 0 to C-1.
     *
     * @return The height in pixels of component <tt>c</tt> in the current
     * tile.
     * */
    public int getCompHeight(int c) {
        return h;
    }

    /**
     * Returns the width in pixels of the specified component in the overall
     * image. This default implementation assumes no component, or component,
     * subsampling (i.e. all components have the same dimensions in pixels).
     *
     * @param c The index of the component, from 0 to C-1.
     *
     * @return The width in pixels of component <tt>c</tt> in the overall
     * image.
     * */
    public int getCompImgWidth(int c) {
        return w;
    }

    /**
     * Returns the height in pixels of the specified component in the overall
     * image. This default implementation assumes no component, or component,
     * subsampling (i.e. all components have the same dimensions in pixels).
     *
     * @param c The index of the component, from 0 to C-1.
     *
     * @return The height in pixels of component <tt>c</tt> in the overall
     * image.
     * */
    public int getCompImgHeight(int c) {
        return h;
    }

    /**
     * Changes the current tile, given the new coordinates. An
     * IllegalArgumentException is thrown if the coordinates do not correspond
     * to a valid tile. This default implementation assumes no tiling so the
     * only valid arguments are x=0, y=0.
     *
     * @param x The horizontal coordinate of the tile.
     *
     * @param y The vertical coordinate of the new tile.
     * */
    public void setTile(int x, int y) {
        if (x!=0 || y != 0) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Advances to the next tile, in standard scan-line order (by rows then
     * columns). A NoNextElementException is thrown if the current tile is the
     * last one (i.e. there is no next tile). This default implementation
     * assumes no tiling, so NoNextElementException() is always thrown.
     * */
    public void nextTile() {
        throw new NoNextElementException();
    }

    /**
     * Returns the coordinates of the current tile. This default
     * implementation assumes no-tiling, so (0,0) is returned.
     *
     * @param co If not null this object is used to return the information. If
     * null a new one is created and returned.
     *
     * @return The current tile's coordinates.
     * */
    public Coord getTile(Coord co) {
        if (co != null) {
            co.x = 0;
            co.y = 0;
            return co;
        }
        else {
            return new Coord(0,0);
        }
    }

    /**
     * Returns the index of the current tile, relative to a standard scan-line
     * order. This default implementations assumes no tiling, so 0 is always
     * returned.
     *
     * @return The current tile's index (starts at 0).
     * */
    public int getTileIdx() {
        return 0;
    }

    /**
     * Returns the horizontal and vertical offset of the upper-left corner of
     * the current tile, in the specified component, relative to the canvas
     * origin, in the component coordinates (not in the reference grid
     * coordinates). These are the coordinates of the current tile's (not
     * active tile) upper-left corner relative to the canvas.
     *
     * <P>This default implementation assumes no tiling and that the
     * partitioning origin is the canvas origin, so (0,0) is always returned.
     *
     * @param co If not null the object is used to return the values, if null
     * a new one is created and returned.
     *
     * @param c The index of the component (between 0 and C-1)
     *
     * @return The horizontal and vertical offsets of the upper-left corner of
     * the current tile, for the specified component, relative to the canvas
     * origin, in the component coordinates.
     * */
    public Coord getTileOff(Coord co, int c) {
        if (co != null) {
            co.x = 0;
            co.y = 0;
            return co;
        }
        else {
            return new Coord(0,0);
        }
    }

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * @param c The index of the component (between 0 and C-1)
     *
     * @return The horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, for component 'c', in
     * the component coordinates.
     * */
    public int getULX(int c) {
        return 0;
    }

    /**
     * Returns the vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, in the component coordinates,
     * for the specified component.
     *
     * @param c The index of the component (between 0 and C-1)
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'c', in the
     * component coordinates.
     * */
    public int getULY(int c) {
        return 0;
    }

    /**
     * Returns the horizontal coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     * */
    public int getImgULX() {
        return 0;
    }

    /**
     * Returns the vertical coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     * */
    public int getImgULY() {
        return 0;
    }

    /**
     * Returns the number of tiles in the horizontal and vertical
     * directions. This default implementation assumes no tiling, so (1,1) is
     * always returned.
     *
     * @param co If not null this object is used to return the information. If
     * null a new one is created and returned.
     *
     * @return The number of tiles in the horizontal (Coord.x) and vertical
     * (Coord.y) directions.
     * */
    public Coord getNumTiles(Coord co) {
        if (co != null) {
            co.x = 1;
            co.y = 1;
            return co;
        }
        else {
            return new Coord(1,1);
        }
    }

    /**
     * Returns the total number of tiles in the image. This default
     * implementation assumes no tiling, so 1 is always returned.
     *
     * @return The total number of tiles in the image.
     * */
    public int getNumTiles() {
        return 1;
    }

    /**
     * Returns true if the data read was originally signed in the specified
     * component, false if not.
     *
     * @param c The index of the component, from 0 to C-1.
     *
     * @return true if the data was originally signed, false if not.
     * */
    public abstract boolean isOrigSigned(int c);

}
