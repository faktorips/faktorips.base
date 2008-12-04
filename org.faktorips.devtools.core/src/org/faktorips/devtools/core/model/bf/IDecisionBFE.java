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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;


public interface IDecisionBFE extends IBFElement {

    public final static String XML_TAG = "Decision";
    public final static String PROPERTY_DATATYPE = "datatype";
    
    public static final String MSGCODE_PREFIX = "DECISIONBFE-";
    public static final String MSGCODE_DATATYPE_NOT_SPECIFIED = MSGCODE_PREFIX + "datatypeNotSpecified";
    public static final String MSGCODE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX + "datatypeDoesNotExist";
    public static final String MSGCODE_DATATYPE_ONLY_NONE_PRIM_VALUEDATATYPE = MSGCODE_PREFIX + "datatypeOnlyNonePrimValuedatatype";

    public String getDatatype();

    public void setDatatype(String datatype);
    
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException;
}
