/***************************************************************************************************
 * Copyright (c) 2005-2008 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 * 
 **************************************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;


public interface IParameterBFE extends IBFElement {

    public final static String XML_TAG = "Parameter";
    public final static String PROPERTY_DATATYPE = "datatype";
    
    public static final String MSGCODE_PREFIX = "PARAMETERBFE-";
    public static final String MSGCODE_NAME_NOT_SPECIFIED = MSGCODE_PREFIX + "nameNotSpecified";
    public static final String MSGCODE_NAME_NOT_VALID = IIpsProjectNamingConventions.INVALID_NAME;
    public static final String MSGCODE_NAME_DUBLICATE = MSGCODE_PREFIX + "nameDuplicate";
    public static final String MSGCODE_DATATYPE_NOT_SPECIFIED = MSGCODE_PREFIX + "datatypeNotSpecified";
    public static final String MSGCODE_DATATYPE_DOES_NOT_EXISIT = MSGCODE_PREFIX + "datatypeDoesNotExists";

    
    public String getDatatype();
    
    public void setDatatype(String datatype);
    
    public Datatype findDatatype() throws CoreException;

}
