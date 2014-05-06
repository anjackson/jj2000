/* 
 * CVS identifier:
 * 
 * $Id: ImgDataAdapter.java,v 1.6 2000/09/05 09:24:06 grosbois Exp $
 * 
 * Class:                   ImgDataAdapter
 * 
 * Description:             A default implementation of the ImgData
 *                          interface that has an ImgData source and just
 *                          returns the values of the source.
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
 * This class provides a default implementation of the methods in the
 * 'ImgData' interface. The default implementation is just to return the
 * value of the source, where the source is another 'ImgData' object.
 *
 * <P>This abstract class can be used to facilitate the development of other
 * classes that implement 'ImgData'. For example a YCbCr color transform can 
 * inherit from this class and all the trivial methods do not have to be
 * reimplemented.
 *
 * <P>If the default implementation of a method provided in this class does
 * not suit a particular implementation of the 'ImgData' interface, the
 * method can be overriden to implement the proper behaviour.
 *
 * @see ImgData
 * */
public abstract class ImgDataAdapter implements ImgData {

    /** Index of the current tile */
    protected int tIdx = 0;

    /** The ImgData source */
    protected ImgData imgdatasrc;

    /**
     * Instantiates the ImgDataAdapter object specifying the ImgData
     * source.
     *
     * @param src From where to obtain all the ImgData values.
     *
     *
     * */
    protected ImgDataAdapter(ImgData src) {
        imgdatasrc = src;
    }

    /**
     * Returns the overall width of the current tile in pixels. This is the
     * tile's width without accounting for any component subsampling. This is
     * also referred as the reference grid width in the current tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The total current tile's width in pixels.
     *
     *
     * */
    public int getWidth() {
        return imgdatasrc.getWidth();
    }

    /**
     * Returns the overall height of the current tile in pixels. This is the
     * tile's height without accounting for any component subsampling. This is
     * also referred as the reference grid height in the current tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The total current tile's height in pixels.
     *
     *
     * */
    public int getHeight() {
        return imgdatasrc.getHeight();
    }

    /**
     * Returns the overall width of the image in pixels. This is the
     * image's width without accounting for any component subsampling
     * or tiling.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The total image's width in pixels.
     *
     *
     * */
    public int getImgWidth() {
        return imgdatasrc.getImgWidth();
    }

    /**
     * Returns the overall height of the image in pixels. This is the
     * image's height without accounting for any component subsampling
     * or tiling.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The total image's height in pixels.
     *
     *
     * */
    public int getImgHeight() {
        return imgdatasrc.getImgHeight();
    }

    /**
     * Returns the number of components in the image.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The number of components in the image.
     *
     *
     * */
    public int getNumComps() {
        return imgdatasrc.getNumComps();
    }

    /**
     * Returns the component subsampling factor in the horizontal direction,
     * for the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The horizontal subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsX(int n) {
        return imgdatasrc.getCompSubsX(n);
    }

    /**
     * Returns the component subsampling factor in the vertical direction, for
     * the specified component. This is, approximately, the ratio of
     * dimensions between the reference grid and the component itself, see the
     * 'ImgData' interface desription for details.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The vertical subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsY(int n) {
        return imgdatasrc.getCompSubsY(n);
    }

    /**
     * Returns the width in pixels of the specified component in the current
     * tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the
     * current tile.
     *
     *
     * */
    public int getCompWidth(int n) {
        return imgdatasrc.getCompWidth(n);
    }
    
    /**
     * Returns the height in pixels of the specified component
     * in the current tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the current
     * tile.
     *
     *
     * */
    public int getCompHeight(int n) {
        return imgdatasrc.getCompHeight(n);
    }

    /**
     * Returns the width in pixels of the specified component
     * in the overall image.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgWidth(int n) {
        return imgdatasrc.getCompImgWidth(n);
    }

    /**
     * Returns the height in pixels of the specified component
     * in the overall image.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgHeight(int n) {
        return imgdatasrc.getCompImgHeight(n);
    }

    /**
     * Returns the number of bits, referred to as the "range bits",
     * corresponding to the nominal range of the image data in the specified
     * component. If this number is <i>n</b> then for unsigned data the
     * nominal range is between 0 and 2^b-1, and for signed data it is between
     * -2^(b-1) and 2^(b-1)-1. In the case of transformed data which is not in
     * the image domain (e.g., wavelet coefficients), this method returns the
     * "range bits" of the image data that generated the coefficients.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component.
     *
     * @return The number of bits corresponding to the nominal range
     * of the image data (in the image domain).
     *
     *
     * */
    public int getNomRangeBits(int n) {
        return imgdatasrc.getNomRangeBits(n);
    }

    /**
     * Changes the current tile, given the new indexes. An
     * IllegalArgumentException is thrown if the indexes do not
     * correspond to a valid tile.
     *
     * <P>This default implementation just changes the tile in the
     * source.
     *
     * @param x The horizontal index of the tile.
     *
     * @param y The vertical index of the new tile.
     *
     *
     * */
    public void setTile(int x, int y) {
        imgdatasrc.setTile(x,y);
	tIdx = getTileIdx();
    }

    /**
     * Advances to the next tile, in standard scan-line order (by rows
     * then columns). An NoNextElementException is thrown if the
     * current tile is the last one (i.e. there is no next tile).
     *
     * <P>This default implementation just advances to the next tile
     * in the source.
     *
     *
     * */
    public void nextTile() {
        imgdatasrc.nextTile();
	tIdx = getTileIdx();
    }

    /**
     * Returns the indexes of the current tile. These are the
     * horizontal and vertical indexes of the current tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The current tile's indexes (vertical and horizontal
     * indexes).
     *
     *
     * */
    public Coord getTile(Coord co) {
        return imgdatasrc.getTile(co);
    }
    
    /**
     * Returns the index of the current tile, relative to a standard
     * scan-line order.
     *
     * <P>This default implementation returns the value of the source.
     * 
     * @return The current tile's index (starts at 0).
     *
     *
     * */
    public int getTileIdx() {
        return imgdatasrc.getTileIdx();
    }

    /**
     * Returns the horizontal and vertical offset of the upper-left corner of
     * the current tile, in the specified component, relative to the canvas
     * origin, in the component coordinates (not in the reference grid
     * coordinates). These are the coordinates of the current tile's (not
     * active tile) upper-left corner relative to the canvas.
     *
     * <P>This default implementation returns the value of the source.
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
    public Coord getTileOff(Coord co, int n) {
        return imgdatasrc.getTileOff(co,n);
    }

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The horizontal coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULX(int n) {
        return imgdatasrc.getULX(n);
    }

    /**
     * Returns the vertical coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param n The index of the component (between 0 and N-1)
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULY(int n) {
        return imgdatasrc.getULY(n);
    }

    /**
     * Returns the horizontal coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULX() {
        return imgdatasrc.getImgULX();
    }

    /**
     * Returns the vertical coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULY() {
        return imgdatasrc.getImgULY();
    }

    /**
     * Returns the number of tiles in the horizontal and vertical directions.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The number of tiles in the horizontal (Coord.x) and
     * vertical (Coord.y) directions.
     *
     *
     * */
    public Coord getNumTiles(Coord co) {
        return imgdatasrc.getNumTiles(co);
    }

    /**
     * Returns the total number of tiles in the image.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @return The total number of tiles in the image.
     *
     *
     * */
    public int getNumTiles() {
        return imgdatasrc.getNumTiles();
    }

}
