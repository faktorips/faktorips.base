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

package org.faktorips.runtime.modeltype;


/**
 * 
 * @author Daniel Hohenberger
 */
public interface IModelTypeAttribute extends IModelElement {

    /**
     * @return this attribute's datatype <code>Class</code>.
     */
    public Class<?> getDatatype();

    /**
     * @return the type of this attribute.
     */
    public AttributeType getAttributeType();

    /**
     * Enum defining the possible attribute types.
     */
    public enum AttributeType{
        changeable, constant, derived, computed;
    }

    /**
     * @return the type of value set restricting this attribute
     */
    public ValueSetType getValueSetType();

    /**
     * Enum defining the possible value set types.
     */
    public enum ValueSetType {
        Enum, Range, AllValues;
    }
    
    /**
     * @return if this attribute is product relevant.
     */
    public boolean isProductRelevant();

}
