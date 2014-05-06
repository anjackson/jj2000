/*
 * CVS identifier:
 *
 * $Id: Tiler.java,v 1.33 2001/02/27 19:13:15 grosbois Exp $
 *
 * Class:                   Tiler
 *
 * Description:             An object to create TiledImgData from
 *                          ImgData
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
 * */
package jj2000.j2k.image;

import jj2000.j2k.util.*;
import jj2000.j2k.*;

/**
 * This class places an image in the canvas coordinate system, tiles it, if so
 * specified, and performs the coordinate conversions transparently. The
 * source must be a 'BlkImgDataSrc' which is not tiled and has a the image
 * origin at the canvas origin (i.e. it is not "canvased"), or an exception is
 * thrown by the constructor. A tiled and "canvased" output is given through
 * the 'BlkImgDataSrc' interface. See the 'ImgData' interface for a
 * description of the canvas and tiling.
 *
 * <P>All tiles produced are rectangular, non-overlapping and their union
 * covers all the image. However, the tiling may not be uniform, depending on
 * the nominal tile size, tiling origin, component subsampling and other
 * factors. Therefore it might not be assumed that all tiles are of the same
 * width and height.
 *
 * <P>The nominal dimension of the tiles is the maximal one, in the reference
 * grid. All the components of the image have the same number of tiles.
 *
 * @see ImgData
 *
 * @see BlkImgDataSrc
 * */
public class Tiler extends ImgDataAdapter implements BlkImgDataSrc {

    /** The source of image data */
    private final BlkImgDataSrc src;

    /** The horizontal coordinate of the image origin in the canvas system, on 
     * the reference grid. */
    private final int ax;

    /** The vertical coordinate of the image origin in the canvas system, on
     * the reference grid. */
    private final int ay;

    /** The horizontal coordinate of the tiling origin in the canvas system, on
     * the reference grid. */
    private final int px;

    /** The vertical coordinate of the tiling origin in the canvas system, on
     * the reference grid. */
    private final int py;

    /** The nominal width of the tiles, on the reference grid. If 0 then there 
     * is no tiling in that direction. */
    private final int nw;

    /** The nominal height of the tiles, on the reference grid. If 0 then
     * there is no tiling in that direction. */
    private final int nh;

    /** The number of tiles in the horizontal direction. */
    private final int nht;

    /** The number of tiles in the vertical direction. */
    private final int nvt;

    /** The component width in the current active tile, for each component */
    private final int bw[];

    /** The component height in the current active tile, for each component */
    private final int bh[];

    /** The horizontal offsets of the upper-left corner of the current tile
     * (not active tile) with respect to the canvas origin, in the component
     * grid, for each component. */
    private final int offX[];

    /** The vertical offsets of the upper-left corner of the current tile (not
     * active tile) with respect to the canvas origin, in the component grid,
     * for each component. */
    private final int offY[];

    /** The horizontal coordinates of the upper-left corner of the active
     * tile, with respect to the image origin, in the component grid, for
     * each component. */
    private final int iulx[];

    /** The vertical coordinates of the upper-left corner of the active
     * tile, with respect to the image origin, in the component grid, for
     * each component. */
    private final int iuly[];

    /** The horizontal index of the current tile */
    private int tx;

    /** The vertical index of the current tile */
    private int ty;

    /** The width of the current tile, on the reference grid. */
    private int cw;

    /** The height of the current tile, on the reference grid. */
    private int ch;

    /**
     * Constructs a new tiler with the specified 'BlkImgDataSrc' source,
     * image origin, tiling origin and nominal tile size.
     *
     * <P>It must be noted that 'ax' and 'bx' can not be chosen arbitrarily
     * for multi component images with different component subsampling. This
     * is because not all '(ax,bx)' yield the correct component dimensions
     * (i.e. the component dimensions derived from '(ax,bx)' and the
     * subsampling factors might not be the same as the true component
     * dimensions). See the 'ImgData' interface for component width
     * calculation from '(ax,bx)' and subsampling factors. This is checked by
     * the constructor and an 'IllegalArgumentException' is thrown if an
     * inconsistency occurs.
     *
     * @param src The 'BlkImgDataSrc' source from where to get the image
     * data. It must not be tiled and the image origin must be at '(0,0)' on
     * its canvas.
     *
     * @param ax The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid (i.e. the image's top-left corner in the
     * reference grid).
     *
     * @param ay The vertical coordinate of the image origin in the canvas
     * system, on the reference grid (i.e. the image's top-left corner in the
     * reference grid).
     *
     * @param px The horizontal tiling origin, in the canvas system, on the
     * reference grid. It must satisfy 'px<=ax'.
     *
     * @param py The vertical tiling origin, in the canvas system, on the
     * reference grid. It must satisfy 'py<=ay'.
     *
     * @param nw The nominal tile width, on the reference grid. If 0 then
     * there is no tiling in that direction.
     *
     * @param nh The nominal tile height, on the reference grid. If 0 then
     * there is no tiling in that direction.
     *
     * @exception IllegalArgumentException If src is tiled or "canvased", or
     * if the arguments do not satisfy the specified constraints.
     * */
    public Tiler(BlkImgDataSrc src,int ax,int ay,int px,int py,int nw,int nh) {
        super(src);

        int i,w,h;

        // Verify that input is not tiled
        if (src.getNumTiles() != 1) {
            throw new IllegalArgumentException("Source is tiled");
        }
        // Verify that source is not "canvased"
        if (src.getImgULX() != 0 || src.getImgULY() != 0) {
            throw new IllegalArgumentException("Source is \"canvased\"");
        }
        // Verify that arguments satisfy trivial requirements
        if (ax < 0 || ay < 0 || px < 0 || py < 0 || nw < 0 || nh < 0 ||
            px > ax || py > ay) {
            throw new IllegalArgumentException("Invalid image origin, "+
                                               "tiling origin or nominal "+
                                               "tile size");
        }
        // Check that all component dimensions are compatible with the
        // specified '(ax,bx)'
        for (i=0; i<src.getNumComps(); i++) {
            w = (ax+src.getImgWidth()+src.getCompSubsX(i)-1) /
                src.getCompSubsX(i) -
                (ax+src.getCompSubsX(i)-1)/src.getCompSubsX(i);
            h = (ay+src.getImgHeight()+src.getCompSubsY(i)-1) /
                src.getCompSubsY(i) -
                (ay+src.getCompSubsY(i)-1)/src.getCompSubsY(i);
            if (w != src.getCompImgWidth(i) || h != src.getCompImgHeight(i)) {
                throw
                    new IllegalArgumentException("Image origin (ax,ay) = ("+ax+
                                                 ","+ay+") yields invalid "+
                                                 "component dimensions");
            }
        }
        // Now we are OK for tiling

        // If we are using no tiling in either direction
        // then set maximum tile size
        if (nw == 0) nw = ax+src.getImgWidth()-px;
        if (nh == 0) nh = ay+src.getImgHeight()-py;

        // Automatically adjust px,py so that tile (0,0) always overlaps with
        // the image.
        if (ax-px >= nw || ay-py >= nh) {
            px += ((ax-px)/nw)*nw;
            py += ((ay-py)/nh)*nh;
            FacilityManager.getMsgLogger().
                printmsg(MsgLogger.INFO,"Automatically adjusted tiling "+
                         "origin to equivalent one ("+px+","+py+") so that "+
                         "first tile overlaps the image");
        }

        // Initialize
        this.src = src;
        this.ax = ax;
        this.ay = ay;
        this.px = px;
        this.py = py;
        this.nw = nw;
        this.nh = nh;

        // Calculate the number of tiles
        nht = (ax+src.getImgWidth()-px+nw-1)/nw;
        nvt = (ay+src.getImgHeight()-py+nh-1)/nh;

        // Allocate arrays
	bw = new int[src.getNumComps()];
	bh = new int[src.getNumComps()];
	offX = new int[src.getNumComps()];
	offY = new int[src.getNumComps()];
        iulx = new int[src.getNumComps()];
        iuly = new int[src.getNumComps()];

        // Set everything for first tile
        setTile(0,0);
    }

    /**
     * Returns the overall width of the current tile in pixels. This is the
     * tile's width without accounting for any component subsampling.
     *
     * @return The total current tile width in pixels.
     * */
    public final int getWidth() {
        return cw;
    }

    /**
     * Returns the overall height of the current tile in pixels. This is the
     * tile's width without accounting for any component subsampling.
     *
     * @return The total current tile height in pixels.
     * */
    public final int getHeight() {
        return ch;
    }

    /**
     * Returns the width in pixels of the specified component in the current
     * tile.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The width in pixels of component <tt>c</tt> in the current
     * tile.
     * */
    public final int getCompWidth(int c) {
        return bw[c];
    }

    /**
     * Returns the height in pixels of the specified component in the current
     * tile.
     *
     * @param c The index of the component, from 0 to N-1.
     *
     * @return The height in pixels of component <tt>c</tt> in the current
     * tile.
     * */
    public final int getCompHeight(int c) {
        return bh[c];
    }

    /**
     * Returns the position of the fixed point in the specified
     * component. This is the position of the least significant integral
     * (i.e. non-fractional) bit, which is equivalent to the number of
     * fractional bits. For instance, for fixed-point values with 2 fractional
     * bits, 2 is returned. For floating-point data this value does not apply
     * and 0 should be returned. Position 0 is the position of the least
     * significant bit in the data.
     *
     * @param c The index of the component.
     *
     * @return The position of the fixed-point, which is the same as the
     * number of fractional bits. For floating-point data 0 is returned.
     * */
    public int getFixedPoint(int c) {
        return src.getFixedPoint(c);
    }

    /**
     * Returns, in the blk argument, a block of image data containing the
     * specifed rectangular area, in the specified component. The data is
     * returned, as a reference to the internal data, if any, instead of as a
     * copy, therefore the returned data should not be modified.
     *
     * <P>The rectangular area to return is specified by the 'ulx', 'uly', 'w'
     * and 'h' members of the 'blk' argument, relative to the current
     * tile. These members are not modified by this method. The 'offset' and
     * 'scanw' of the returned data can be arbitrary. See the 'DataBlk' class.
     *
     * <P>This method, in general, is more efficient than the 'getCompData()'
     * method since it may not copy the data. However if the array of returned
     * data is to be modified by the caller then the other method is probably
     * preferable.
     *
     * <P>If the data array in <tt>blk</tt> is <tt>null</tt>, then a new one
     * is created if necessary. The implementation of this interface may
     * choose to return the same array or a new one, depending on what is more
     * efficient. Therefore, the data array in <tt>blk</tt> prior to the
     * method call should not be considered to contain the returned data, a
     * new array may have been created. Instead, get the array from
     * <tt>blk</tt> after the method has returned.
     *
     * <P>The returned data may have its 'progressive' attribute set. In this
     * case the returned data is only an approximation of the "final" data.
     *
     * @param blk Its coordinates and dimensions specify the area to return,
     * relative to the current tile. Some fields in this object are modified
     * to return the data.
     *
     * @param c The index of the component from which to get the data.
     *
     * @return The requested DataBlk
     *
     * @see #getCompData
     * */
    public final DataBlk getInternCompData(DataBlk blk, int c) {
        // Check that block is inside tile
        if (blk.ulx < 0 || blk.uly < 0 || 
            blk.ulx+blk.w > bw[c] || blk.uly+blk.h > bh[c]) {
            throw new IllegalArgumentException("Block is outside the tile");
        }
        // Translate to the sources coordinates
        blk.ulx += iulx[c];
        blk.uly += iuly[c];
        blk = src.getInternCompData(blk,c);
        // Translate back to the tiled coordinates
        blk.ulx -= iulx[c];
        blk.uly -= iuly[c];
	return blk;
    }

    /**
     * Returns, in the blk argument, a block of image data containing the
     * specifed rectangular area, in the specified component. The data is
     * returned, as a copy of the internal data, therefore the returned data
     * can be modified "in place".
     *
     * <P>The rectangular area to return is specified by the 'ulx', 'uly', 'w'
     * and 'h' members of the 'blk' argument, relative to the current
     * tile. These members are not modified by this method. The 'offset' of
     * the returned data is 0, and the 'scanw' is the same as the block's
     * width. See the 'DataBlk' class.
     *
     * <P>This method, in general, is less efficient than the
     * 'getInternCompData()' method since, in general, it copies the
     * data. However if the array of returned data is to be modified by the
     * caller then this method is preferable.
     *
     * <P>If the data array in 'blk' is 'null', then a new one is created. If
     * the data array is not 'null' then it is reused, and it must be large
     * enough to contain the block's data. Otherwise an 'ArrayStoreException'
     * or an 'IndexOutOfBoundsException' is thrown by the Java system.
     *
     * <P>The returned data may have its 'progressive' attribute set. In this
     * case the returned data is only an approximation of the "final" data.
     *
     * @param blk Its coordinates and dimensions specify the area to return,
     * relative to the current tile. If it contains a non-null data array,
     * then it must be large enough. If it contains a null data array a new
     * one is created. Some fields in this object are modified to return the
     * data.
     *
     * @param n The index of the component from which to get the data.
     *
     * @return The requested DataBlk
     *
     * @see #getInternCompData
     * */
    public final DataBlk getCompData(DataBlk blk, int n) {
        // Check that block is inside tile
        if (blk.ulx < 0 || blk.uly < 0 || 
            blk.ulx+blk.w > bw[n] || blk.uly+blk.h > bh[n]) {
            throw new IllegalArgumentException("Block is outside the tile");
        }
        // Translate to the source's coordinates
        blk.ulx += iulx[n];
        blk.uly += iuly[n];
        blk = src.getCompData(blk,n);
        // Translate back to the tiled coordinates
        blk.ulx -= iulx[n];
        blk.uly -= iuly[n];
	return blk;
    }

    /**
     * Changes the current tile, given the new tile indexes. An
     * IllegalArgumentException is thrown if the coordinates do not correspond
     * to a valid tile.
     *
     * @param x The horizontal index of the tile.
     *
     * @param y The vertical index of the new tile.
     * */
    public final void setTile(int x, int y) {
        int i;          // counter
        int ctox,ctoy;  // new current tile origin in the reference grid
        int tonx;       // x of the origin of the next tile in the X direction
        int tony;       // y of the origin of the next tile in the Y direction

        // Check tile indexes
        if (x < 0 || y < 0 || x >= nht || y >= nvt) {
            throw new IllegalArgumentException("Tile's indexes out of bounds");
        }

        // Set new current tile
        tx = x;
        ty = y;
        // Calculate tile origins
        ctox = (x != 0) ? px+x*nw : ax;
        ctoy = (y != 0) ? py+y*nh : ay;
        tonx = (x != nht-1) ? px+(x+1)*nw : ax+src.getImgWidth();
        tony = (y != nvt-1) ? py+(y+1)*nh : ay+src.getImgHeight();
        // Set general variables
        cw = tonx - ctox;
        ch = tony - ctoy;
        // Set component specific variables
        for (i = src.getNumComps()-1; i >= 0 ; i--) {
	    bw[i] = (tonx+src.getCompSubsX(i)-1)/src.getCompSubsX(i) -
                (ctox+src.getCompSubsX(i)-1)/src.getCompSubsX(i);
	    bh[i] = (tony+src.getCompSubsY(i)-1)/src.getCompSubsY(i) -
                (ctoy+src.getCompSubsY(i)-1)/src.getCompSubsY(i);
            offX[i] = (px+x*nw+src.getCompSubsX(i)-1)/src.getCompSubsX(i);
            offY[i] = (py+y*nh+src.getCompSubsY(i)-1)/src.getCompSubsY(i);
            iulx[i] = (ctox+src.getCompSubsX(i)-1)/src.getCompSubsX(i)-
                (ax+src.getCompSubsX(i)-1)/src.getCompSubsX(i);
            iuly[i] = (ctoy+src.getCompSubsY(i)-1)/src.getCompSubsY(i)-
                (ay+src.getCompSubsY(i)-1)/src.getCompSubsY(i);
        }
    }

    /**
     * Advances to the next tile, in standard scan-line order (by rows then
     * columns). An NoNextElementException is thrown if the current tile is
     * the last one (i.e. there is no next tile).
     * */
    public final void nextTile() {
        if (tx == nht-1 && ty == nvt-1) { // Already at last tile
            throw new NoNextElementException();
        }
        else if (tx < nht-1) { // If not at end of current tile line
            setTile(tx+1,ty);
        }
        else { // First tile at next line
            setTile(0,ty+1);
        }
    }

    /**
     * Returns the horizontal and vertical indexes of the current tile.
     *
     * @param co If not null this object is used to return the
     * information. If null a new one is created and returned.
     *
     * @return The current tile's horizontal and vertical indexes..
     * */
    public final Coord getTile(Coord co) {
        if (co != null) {
            co.x = tx;
            co.y = ty;
            return co;
        }
        else {
            return new Coord(tx,ty);
        }
    }

    /**
     * Returns the index of the current tile, relative to a standard scan-line
     * order.
     *
     * @return The current tile's index (starts at 0).
     * */
    public final int getTileIdx() {
        return ty*nht+tx;
    }

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
     * @param c The index of the component (between 0 and N-1).
     *
     * @return The horizontal and vertical offsets of the upper-left corner of
     * the current tile, for the specified component, relative to the canvas
     * origin, in the component coordinates.
     * */
    public final Coord getTileOff(Coord co, int c) {
        if (co != null) {
            co.x = offX[c];
            co.y = offY[c];
            return co;
        }
        else {
            return new Coord(offX[c],offY[c]);
        }
    }

    /**
     * Returns the horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, in the component
     * coordinates, for the specified component.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The horizontal coordinate of the upper-left corner of the
     * active tile, with respect to the canvas origin, for component 'c', in
     * the component coordinates.
     * */
    public final int getULX(int c) {
        return iulx[c]+(ax+src.getCompSubsX(c)-1)/src.getCompSubsX(c);
    }

    /**
     * Returns the vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, in the component coordinates,
     * for the specified component.
     *
     * @param c The index of the component (between 0 and N-1)
     *
     * @return The vertical coordinate of the upper-left corner of the active
     * tile, with respect to the canvas origin, for component 'c', in the
     * component coordinates.
     * */
    public final int getULY(int c) {
        return iuly[c]+(ay+src.getCompSubsY(c)-1)/src.getCompSubsY(c);
    }

    /**
     * Returns the horizontal coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The horizontal coordinate of the image origin in the canvas
     * system, on the reference grid.
     * */
    public final int getImgULX() {
        return ax;
    }

    /**
     * Returns the vertical coordinate of the image origin, the top-left
     * corner, in the canvas system, on the reference grid.
     *
     * @return The vertical coordinate of the image origin in the canvas
     * system, on the reference grid.
     * */
    public final int getImgULY() {
        return ay;
    }

    /**
     * Returns the number of tiles in the horizontal and vertical directions.
     *
     * @param co If not null this object is used to return the information. If
     * null a new one is created and returned.
     *
     * @return The number of tiles in the horizontal (Coord.x) and vertical
     * (Coord.y) directions.
     * */
    public final Coord getNumTiles(Coord co) {
        if (co != null) {
            co.x = nht;
            co.y = nvt;
            return co;
        }
        else {
            return new Coord(nht,nvt);
        }
    }

    /**
     * Returns the total number of tiles in the image.
     *
     * @return The total number of tiles in the image.
     * */
    public final int getNumTiles() {
        return nht*nvt;
    }

    /**
     * Returns the nominal width of the tiles in the reference grid.
     *
     * @return The nominal tile width, in the reference grid.
     * */
    public final int getNomTileWidth(){
        return nw;
    }

    /**
     * Returns the nominal width of the tiles in the reference grid.
     *
     * @return The nominal tile width, in the reference grid.
     * */
    public final int getNomTileHeight(){
        return nh;
    }

    /**
     * Returns the tiling origin, refferred to as '(Px,Py)' in the 'ImgData'
     * interface.
     *
     * @param co If not null this object is used to return the information. If
     * null a new one is created and returned.
     *
     * @return The coordinate of the tiling origin, in the canvas system, on
     * the reference grid.
     *
     * @see ImgData
     * */
    public final Coord getTilingOrigin(Coord co) {
        if (co != null) {
            co.x = px;
            co.y = py;
            return co;
        }
        else {
            return new Coord(px,py);
        }
    }

    /**
     * Returns a String object representing Tiler's informations
     *
     * @return Tiler's infos in a string
     * */
    public String toString(){
	return 
	    "Tiler: source= "+src+
	    "\n"+getNumTiles()+" tile(s), nominal width= "+nw+
	    ", nominal height= "+nh;
    }
}
