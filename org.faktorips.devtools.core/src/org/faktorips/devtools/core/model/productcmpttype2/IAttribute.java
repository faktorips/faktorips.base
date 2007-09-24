/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype2;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IValueSetOwner;
import org.faktorips.devtools.core.model.pctype.Modifier;

/**
 * An attribute of a product component type.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IAttribute extends IIpsObjectPart, IValueSetOwner, IProdDefProperty {

    // modifiable properties
    public final static String PROPERTY_DATATYPE = "datatype";
    public final static String PROPERTY_MODIFIER = "modifier";
    public final static String PROPERTY_DEFAULT_VALUE = "defaultValue";
    
    /**
     * Sets the attribute's name.
     */
    public void setName(String newName);
    
    /**
     * Returns the product component type the attribute belongs to.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Returns the attribute's modifier. This method never returns <code>null</code>.
     */
    public Modifier getModifier();
    
    /**
     * Sets the attribute's modifier.
     * 
     * @throws NullPointerException if newModifier is <code>null</code>.
     */
    public void setModifier(Modifier newModifer);

    /**
     * Returns the attribute's *own* value datatype as id. If this is a linked attribute, it's datatype is derived from
     * the policy component type attribute it linked to. For this case the return value of this method is undefined
     * and it does not make any sense to use it.
     * <p> 
     * The attribute value datatype can always be obtained via findValueDatatype().
     * 
     * @see #findDatatype() 
     * @see ValueDatatype
     */
    public String getDatatype();
    
    /**
     * Sets the attribute's value datatype id. 
     * 
     * @see ValueDatatype
     */
    public void setDatatype(String newDatatype);
    
    /**
     * Returns the attribute's value datatype. If this attribute is linked to a policy component type attribute,
     * the policy component type's value datatype is returned. If the attribute is not linked, the attribute's *own*
     * value datatype is returned.
     * 
     * @param project The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     *
     * @see #getDatatype()
     */
    public ValueDatatype findDatatype(IIpsProject project) throws CoreException;
    
    /**
     * Returns the attribute's default value.
     */
    public String getDefaultValue();
    
    /**
     * Sets the attribute's default value.
     */
    public void setDefaultValue(String newValue);
        
}
