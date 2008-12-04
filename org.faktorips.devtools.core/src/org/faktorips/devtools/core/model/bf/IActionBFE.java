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
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectNamingConventions;


public interface IActionBFE extends IBFElement {

    public final static String XML_TAG = "Action";
    public final static String PROPERTY_TARGET = "target";
    public final static String PROPERTY_EXECUTABLE_METHOD_NAME = "executableMethodName";
    
    public static final String MSGCODE_PREFIX = "ACTIONBFE-";
    public static final String MSGCODE_TARGET_NOT_SPECIFIED = MSGCODE_PREFIX + "targetNotSpecified";
    public static final String MSGCODE_TARGET_DOES_NOT_EXIST = MSGCODE_PREFIX + "targetDoesNotExist";
    public static final String MSGCODE_TARGET_NOT_VALID_TYPE = MSGCODE_PREFIX + "targetNotValidType";
    public static final String MSGCODE_METHOD_NOT_SPECIFIED = MSGCODE_PREFIX + "methodNotSpecified";
    public static final String MSGCODE_METHOD_DOES_NOT_EXIST = MSGCODE_PREFIX + "methodDoesNotExist";
    public static final String MSGCODE_NAME_NOT_SPECIFIED = MSGCODE_PREFIX + "nameNotSpecified";
    public static final String MSGCODE_NAME_NOT_VALID = IIpsProjectNamingConventions.INVALID_NAME;

    
    public void setTarget(String target);
    
    public String getTarget();
    
    public IParameterBFE getParameter();
    
    public IBusinessFunction findReferencedBusinessFunction() throws CoreException;
   
    public void setExecutableMethodName(String name);
    
    public String getExecutableMethodName();
    
    public String getReferencedBfQualifiedName();
    
    public String getReferencedBfUnqualifedName();
    
}
