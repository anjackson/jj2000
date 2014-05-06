// $Id: Parameterized.java,v 1.1.1.1 2002/08/27 11:49:30 grosbois Exp $
//
// $Log: Parameterized.java,v $
// Revision 1.1.1.1  2002/08/27 11:49:30  grosbois
// Imported source from cryptix 3.2
//
// Revision 1.3  2000/08/17 11:35:25  edwin
// Package move java.* -> xjava.*, which is needed for JDK 1.3 compatibility.
// I had to break permission support even further to make this work (I don't
// believe it was working in the first place, so it's not really a problem).
//
// Revision 1.2  1997/12/07 07:39:45  hopwood
// + Trivial changes.
//
// Revision 1.1  1997/11/21 04:31:18  hopwood
// + Committed changes below.
//
// Revision 0.2  1997/11/19  David Hopwood
// + Fixed throws clause of setParameter.
//
// Revision 0.1  1997/11/18  David Hopwood
// + Original version.
//
// $Endlog$
/*
 * Copyright (c) 1997 Systemics Ltd
 * on behalf of the Cryptix Development Team.  All rights reserved.
 */

package xjava.security;

import java.security.InvalidParameterException;


/**
 * This interface is implemented by algorithm objects that may be
 * parameterized (i.e. support the <code>setParameter</code> and
 * <code>getParameter</code> methods). It can be used as a workaround
 * for the absence of these methods in Javasoft's version of JCA/JCE.
 * <p>
 * <strong><a href="../guide/ijce/JCEDifferences.html">This interface
 * is not supported in JavaSoft's version of JCE.</a></strong>
 * <p>
 * <b>Copyright</b> &copy; 1997
 * <a href="http://www.systemics.com/">Systemics Ltd</a> on behalf of the
 * <a href="http://www.systemics.com/docs/cryptix/">Cryptix Development Team</a>.
 * <br>All rights reserved.
 * <p>
 * <b>$Revision: 1.1.1.1 $</b>
 * @author  David Hopwood
 * @since   IJCE 1.0.1
 */
public interface Parameterized {
    /**
     * Sets the specified algorithm parameter to the specified value.
     * <p>
     * This method supplies a general-purpose mechanism through which it is
     * possible to set the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     *
     * @param  param    the string identifier of the parameter.
     * @param  value    the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this cipher implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be set (for example because the object is in the
     *                  wrong state).
     * @exception InvalidParameterTypeException if value is the wrong type
     *                  for this parameter.
     */
    void setParameter(String param, Object value)
    throws NoSuchParameterException, InvalidParameterException,
           InvalidParameterTypeException;

    /**
     * Gets the value of the specified algorithm parameter.
     * <p>
     * This method supplies a general-purpose mechanism through which it
     * is possible to get the various parameters of this object. A uniform
     * algorithm-specific naming scheme for each parameter is desirable but
     * left unspecified at this time.
     *
     * @param  param    the string name of the parameter.
     * @return the object that represents the parameter value.
     * @exception NullPointerException if param == null
     * @exception NoSuchParameterException if there is no parameter with name
     *                  param for this implementation.
     * @exception InvalidParameterException if the parameter exists but cannot
     *                  be read.
     */
    Object getParameter(String param)
    throws NoSuchParameterException, InvalidParameterException;
}
