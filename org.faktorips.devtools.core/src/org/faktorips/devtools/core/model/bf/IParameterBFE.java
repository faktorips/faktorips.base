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


public interface IParameterBFE extends IBFElement {

    public final static String XML_TAG = "Parameter";
    public final static String PROPERTY_DATATYPE = "datatype";
    
    public String getDatatype();
    
    public void setDatatype(String datatype);
    
    public Datatype findDatatype() throws CoreException;

}
