/* 
 * CVS identifier:
 * 
 * $Id: InvWTAdapter.java,v 1.9 2000/09/21 16:13:15 dsanta Exp $
 * 
 * Class:                   InvWTAdapter
 * 
 * Description:             <short description of class>
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

import jj2000.j2k.decoder.*;
import jj2000.j2k.image.*;

/**
 * This class provides default implementation of the methods in the
 * 'InvWT' interface. The source is always a 'MultiResImgData', which
 * is a multi-resolution image. The default implementation is just to
 * return the value of the source at the current image resolution
 * level, which is set by the 'setImgResLevel()' method.
 *
 * <P>This abstract class can be used to facilitate the development of
 * other classes that implement the 'InvWT' interface, because most of
 * the trivial methods are already implemented.
 *
 * <P>If the default implementation of a method provided in this class
 * does not suit a particular implementation of the 'InvWT' interface,
 * the method can be overriden to implement the proper behaviour.
 *
 * <P>If the 'setImgResLevel()' method is overriden then it is very
 * important that the one of this class is called from the overriding
 * method, so that the other methods in this class return the correct
 * values.
 *
 * @see InvWT
 * */
public abstract class InvWTAdapter implements InvWT {

    /** The decoder specifications */
    protected DecoderSpecs decSpec;

    /** The 'MultiResImgData' source */
    protected MultiResImgData mressrc;

    /** The wanted image resolution level */
    int reslvl;

    /**
     * Instantiates the 'InvWTAdapter' object using the specified
     * 'MultiResImgData' source. The reconstruction resolution level is set to
     * full resolution (i.e. the maximum resolution level).
     *
     * @param src From where to obtain the values to return
     *
     * @param decSpec The decoder specifications
     *
     *
     * */
    protected InvWTAdapter(MultiResImgData src,DecoderSpecs decSpec) {
        mressrc = src;
	this.decSpec = decSpec;
    }

    /**
     * Sets the image reconstruction resolution level. A value of 0
     * means reconstruction of an image with the lowest resolution
     * (dimension) available.
     *
     * <P>Note: Image resolution level indexes may differ from
     * tile-component resolution index. They are indeed indexed
     * starting from the lowest number of decomposition levels of each
     * component of each tile.
     *
     * <P>Example: For an image (1 tile) with 2 components (component
     * 0 having 2 decomposition levels and component 1 having 3
     * decomposition levels), the first (tile-) component has 3
     * resolution levels and the second one has 4 resolution levels,
     * whereas the image has only 3 resolution levels available.
     *
     * @param rl The image resolution level.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     * */
    public void setImgResLevel(int rl){
        if(rl<0){
            throw new IllegalArgumentException();
        }
        reslvl = rl;
    }

    /**
     * Returns the overall width of the current tile in pixels. This
     * is the tile's width without accounting for any component
     * subsampling. This is also referred as the reference grid width
     * in the current tile.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The total current tile's width in pixels.
     *
     *
     * */
    public int getWidth() {
	// Find tile resolution level
	int tIdx = getTileIdx();
	int rl = decSpec.dls.getMinInTile(tIdx)-decSpec.dls.getMin()+reslvl;
        return mressrc.getWidth(rl);
    }

    /**
     * Returns the overall height of the current tile in pixels. This
     * is the tile's height without accounting for any component
     * subsampling. This is also referred as the reference grid height
     * in the current tile.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The total current tile's height in pixels.
     *
     *
     * */
    public int getHeight(){
	// Find tile resolution level
	int tIdx = getTileIdx();
	int rl = decSpec.dls.getMinInTile(tIdx)-decSpec.dls.getMin()+reslvl;
        return mressrc.getHeight(rl);
    }

    /**
     * Returns the overall width of the image in pixels. This is the
     * image's width without accounting for any component subsampling
     * or tiling.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The total image's width in pixels.
     *
     *
     * */
    public int getImgWidth() {
        return mressrc.getImgWidth(reslvl);
    }

    /**
     * Returns the overall height of the image in pixels. This is the
     * image's height without accounting for any component subsampling
     * or tiling.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The total image's height in pixels.
     *
     *
     * */
    public int getImgHeight() {
        return mressrc.getImgHeight(reslvl);
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
        return mressrc.getNumComps();
    }

    /**
     * Returns the component subsampling factor in the horizontal
     * direction, for the specified component. This is, approximately,
     * the ratio of dimensions between the reference grid and the
     * component itself, see the 'ImgData' interface desription for
     * details.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The horizontal subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsX(int c) {
        return mressrc.getCompSubsX(c);
    }

    /**
     * Returns the component subsampling factor in the vertical
     * direction, for the specified component. This is, approximately,
     * the ratio of dimensions between the reference grid and the
     * component itself, see the 'ImgData' interface desription for
     * details.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The vertical subsampling factor of component 'n'
     *
     * @see ImgData
     *
     *
     * */
    public int getCompSubsY(int c) {
        return mressrc.getCompSubsY(c);
    }

    /**
     * Returns the width in pixels of the specified component in the
     * current tile.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>n</tt> in the current
     * tile.
     *
     *
     * */
    public int getCompWidth(int c) {
	// Find tile-component resolution level
	int tIdx = getTileIdx();
	int rl = ((Integer)decSpec.dls.getTileCompVal(tIdx,c)).intValue()
	    -decSpec.dls.getMin()+reslvl;
        return mressrc.getCompWidth(c,rl);
    }

    /**
     * Returns the height in pixels of the specified component in the
     * current tile.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the current
     * tile.
     *
     *
     * */
    public int getCompHeight(int c) {
	// Find tile-component resolution level
	int tIdx = getTileIdx();
	int rl = ((Integer)decSpec.dls.getTileCompVal(tIdx,c)).intValue()
	    -decSpec.dls.getMin()+reslvl;
        return mressrc.getCompHeight(c,rl);
    }

    /**
     * Returns the width in pixels of the specified component in the
     * overall image.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>c</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgWidth(int c) {
	// Find component resolution level
	int rl = decSpec.dls.getMinInComp(c)-decSpec.dls.getMin()+reslvl;
        return mressrc.getCompImgWidth(c,rl);
    }

    /**
     * Returns the height in pixels of the specified component in the
     * overall image.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>n</tt> in the overall
     * image.
     *
     *
     * */
    public int getCompImgHeight(int c) {
	// Find component resolution level
	int rl = decSpec.dls.getMinInComp(c)-decSpec.dls.getMin()+reslvl;
        return mressrc.getCompImgHeight(c,rl);
    }

    /**
     * Changes the current tile, given the new indices. An
     * IllegalArgumentException is thrown if the coordinates do not
     * correspond to a valid tile.
     *
     * <P>This default implementation calls the same method on the source.
     *
     * @param x The horizontal index of the tile.
     *
     * @param y The vertical index of the new tile.
     *
     *
     * */
    public void setTile(int x, int y) {
        mressrc.setTile(x,y);
    }

    /**
     * Advances to the next tile, in standard scan-line order (by rows
     * then columns). An NoNextElementException is thrown if the
     * current tile is the last one (i.e. there is no next tile).
     *
     * <P>This default implementation calls the same method on the source.
     *
     *
     * */
    public void nextTile() {
        mressrc.nextTile();
    }

    /**
     * Returns the indixes of the current tile. These are the
     * horizontal and vertical indexes of the current tile.
     *
     * <P>This default implementation returns the value of the source.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The current tile's indices (vertical and horizontal
     * indexes).
     *
     *
     * */
    public Coord getTile(Coord co) {
        return mressrc.getTile(co);
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
        return mressrc.getTileIdx();
    }

    /**
     * Returns the horizontal and vertical offset of the upper-left corner of
     * the current tile, in the specified component, relative to the canvas
     * origin, in the component coordinates (not in the reference grid
     * coordinates). These are the coordinates of the current tile's (not
     * active tile) upper-left corner relative to the canvas.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param co If not null the object is used to return the values,
     * if null a new one is created and returned.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The horizontal and vertical offsets of the upper-left
     * corner of the current tile, for the specified component,
     * relative to the canvas origin, in the component coordinates.
     *
     *
     * */
    public Coord getTileOff(Coord co, int c) {
        return mressrc.getTileOff(co,c,reslvl);
    }

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The horizontal coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULX(int c) {
	// Find component resolution level
	int tIdx = getTileIdx();
	int rl = ((Integer)decSpec.dls.getTileCompVal(tIdx,c)).intValue()
	    -decSpec.dls.getMin()+reslvl;
        return mressrc.getULX(c,rl);
    }

    /**
     * Returns the vertical coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'n', in the
     * component coordinates.
     *
     *
     * */
    public int getULY(int c) {
	// Find component resolution level
	int tIdx = getTileIdx();
	int rl = ((Integer)decSpec.dls.getTileCompVal(tIdx,c)).intValue()
	    -decSpec.dls.getMin()+reslvl;
        return mressrc.getULY(c,rl);
    }

    /**
     * Returns the horizontal coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULX() {
        return mressrc.getImgULX(reslvl);
    }

    /**
     * Returns the vertical coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * <P>This default implementation returns the value of the source at the
     * current reconstruction resolution level.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     *
     *
     * */
    public int getImgULY() {
        return mressrc.getImgULY(reslvl);
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
        return mressrc.getNumTiles(co);
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
        return mressrc.getNumTiles();
    }
}
