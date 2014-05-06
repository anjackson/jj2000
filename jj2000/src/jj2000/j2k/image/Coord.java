/*
 * CVS identifier:
 *
 * $Id: Coord.java,v 1.13 2000/09/21 16:13:05 dsanta Exp $
 *
 * Class:                   Coord
 *
 * Description:             Class for storage of 2-D coordinates
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
 * 
 * 
 * 
 */


package jj2000.j2k.image;

/**
 * This class represents 2-D coordinates.
 *
 */
public class Coord {
    /** The horizontal coordinate */
    public int x;

    /** The vertical coordinate */
    public int y;

    /**
     * Creates a new coordinate object given with the (0,0) coordinates
     *
     *
     * */
    public Coord() {
    }

    /**
     * Creates a new coordinate object given the two coordinates.
     *
     * @param x The horizontal coordinate.
     *
     * @param y The vertical coordinate.
     *
     *
     * */
    public Coord(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Creates a new coordinate object given another Coord object i.e. copy 
     * constructor
     *
     * @param c The Coord object to be copied.
     *
     * */
    public Coord(Coord c) {
        this.x = c.x;
        this.y = c.y;
    }

    /**
     * Returns a string representation of the object coordinates
     *
     * @return The vertical and the horizontal coordinates
     *
     *
     */
    public String toString(){
	return "("+x+","+y+")";
    }
}
