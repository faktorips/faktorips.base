/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.faktorips.devtools.core.model.pctype.Modifier;


/**
 * A type's method.
 */
public interface IMethod extends IParameterContainer {

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    public final static String PROPERTY_PARAMETERS = "parameters"; //$NON-NLS-1$
    
    /**
     * Returns the type this method belongs to.
     */
    public IType getType();

    /**
     * Sets the method's name.
     */
    public void setName(String newName);
    
    /**
     * Returns the name of the value datatype this method returns. 
     */
    public String getDatatype();
    
    /**
     * Sets name of the value datatype this method returns. 
     */
    public void setDatatype(String newDatatype);
    
    /**
     * Returns the modifier.
     */
    public Modifier getModifier();

    /**
     * Sets the modifier.
     */
    public void setModifier(Modifier newModifier);
    
    /**
     * Returns <code>true</code> if this is an abstract method, <code>false</code> otherwise.
     */
    public boolean isAbstract();

    /**
     * Sets if this is an abstract method or not.
     */
    public void setAbstract(boolean newValue);
    
    /**
     * Returns the Java modifier. Determined from the ips modifier and the abstract flag.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier();

    /**
     * Returns <code>true</code> if the other method has the same name, the same numer of parameters
     * and each parameter has the same datatype as the parameter in this method. Returns <code>false</code> otherwise.
     * Note that the return type is not checked. 
     */
    public boolean isSame(IMethod method);
    
    
}
