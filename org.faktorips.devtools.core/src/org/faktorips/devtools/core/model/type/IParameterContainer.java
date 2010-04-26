/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * A container containing parameters.
 * 
 * @author Jan Ortmann
 */
public interface IParameterContainer extends IIpsObjectPart {

    /**
     * Returns the method's parameters. Returns an empty array if the method doesn't have any
     * parameter.
     */
    public IParameter[] getParameters();

    /**
     * Returns the method's parameter names. Returns an empty array if the method doesn't have any
     * parameter.
     */
    public String[] getParameterNames();

    /**
     * Returns the number of parameters.
     */
    public int getNumOfParameters();

    /**
     * Creates a new parameter.
     */
    public IParameter newParameter();

    /**
     * Creates a new parameter.
     */
    public IParameter newParameter(String datatype, String name);

    /**
     * Moves the parameters identified by the indexes up or down by one position. If one of the
     * indexes is 0 (the first parameter), no parameter is moved up. If one of the indexes is the
     * number of parameters - 1 (the last parameter) no parameter is moved down.
     * 
     * @param indexes The indexes identifying the parameters.
     * @param up <code>true</code>, to move the parameters up, <false> to move them down.
     * 
     * @return The new indexes of the moved parameters.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify a parameter.
     */
    public int[] moveParameters(int[] indexes, boolean up);

}
