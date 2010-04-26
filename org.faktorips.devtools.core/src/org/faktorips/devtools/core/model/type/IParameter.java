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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A parameter (or argument) of a method.
 * 
 * @author Jan Ortmann
 */
public interface IParameter extends IIpsObjectPart {

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Sets the name of the parameter.
     */
    public void setName(String newName);

    /**
     * Sets the parameter's value datatype.
     */
    public void setDatatype(String type);

    /**
     * Returns the parameter's datatype.
     */
    public String getDatatype();

    /**
     * Returns the parameter's datatype or <code>null</code> if the datatype can't be found.
     * 
     * @param ipsProject The ips project which ips object path is used to search.
     * 
     * @throws CoreException if an excpetion occurs while searching for the datatype.
     */
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException;
}
