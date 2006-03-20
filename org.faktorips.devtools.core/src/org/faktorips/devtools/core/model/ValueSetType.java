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

package org.faktorips.devtools.core.model;

import org.faktorips.devtools.core.internal.model.AllValuesValueSet;
import org.faktorips.devtools.core.internal.model.EnumValueSet;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.values.DefaultEnumType;
import org.faktorips.values.DefaultEnumValue;
import org.faktorips.values.EnumType;
import org.faktorips.values.EnumValue;
import org.w3c.dom.Element;

/**
 * The kind of value set.
 * 
 * @author Jan Ortmann
 */
public class ValueSetType extends DefaultEnumValue {

    /**
     * Defines the value set type specifiyinf all value. 
     */
    public final static ValueSetType ALL_VALUES;
    
    /**
     * Defines the value set type range. 
     */
    public final static ValueSetType RANGE;
    
    /**
     * Defines the value set type enumeration. 
     */
    public final static ValueSetType ENUM;

    private final static DefaultEnumType enumType; 
    
    static {
        enumType = new DefaultEnumType("ValueSetType", ValueSetType.class); //$NON-NLS-1$
        ALL_VALUES = new ValueSetType(enumType, "allValues", Messages.ValueSetType__allValues); //$NON-NLS-1$
        RANGE = new ValueSetType(enumType, "range", Messages.ValueSetType_range); //$NON-NLS-1$ 
        ENUM = new ValueSetType(enumType, "enum", Messages.ValueSetType_enumeration); //$NON-NLS-1$ 
    }
    
    public final static EnumType getEnumType() {
        return enumType;
    }
    
    /**
     * Returns the value set type identified by the id. 
     * Returns <code>null</code> if the id does not identify a value set type.
     */
    public final static ValueSetType getValueSetType(String id) {
        return (ValueSetType)enumType.getEnumValue(id);
    }
    
    /**
     * Returns the value set type identified by the name. 
     * Returns <code>null</code> if the name does not identify a value set type.
     */
    public final static ValueSetType getValueSetTypeByName(String name) {
        ValueSetType[] types = getValueSetTypes();
        for (int i=0; i<types.length; i++) {
            if (types[i].getName().equals(name)) {
                return types[i];
            }
        }
        return null;
    }
    
    /**
     * Returns all value set types.
     */
    public final static ValueSetType[] getValueSetTypes() {
        EnumValue[] values = getEnumType().getValues();
        ValueSetType[] types = new ValueSetType[values.length];
        System.arraycopy(values, 0, types, 0, values.length);
        return types;
    }
    
    private ValueSetType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }


	/**
	 * Creates a new value set of the type this method is invoked on.
	 */
	public IValueSet newValueSet(IIpsObjectPart parent, int id) {
		if (this == ALL_VALUES) {
			return new AllValuesValueSet(parent, id);
		}
		else if (this == ENUM) {
			return new EnumValueSet(parent, id);
		}
		else if (this == RANGE) {
			return new RangeValueSet(parent, id);
		}
		return null;
	}

	/**
	 * Creates a new ValueSet - the type of the ValueSet is examined from the given XML-Element.
	 *  
	 * @param valueSetNode The node describing the ValueSet.
	 * @param parent The parent for the new value set.
	 * @param id The IpsObjectPart-ID for the new value set.
	 */
	public static IValueSet newValueSet(Element valueSetNode, IIpsObjectPart parent, int id) {
		String tagName = valueSetNode.getNodeName();
		if (tagName.equals(EnumValueSet.XML_TAG)) {
			return new EnumValueSet(parent, id);
		}
		else if (tagName.equals(RangeValueSet.XML_TAG)) {
			return new RangeValueSet(parent, id);
		}
		else if (tagName.equals(AllValuesValueSet.XML_TAG)) {
			return new AllValuesValueSet(parent, id);
		}
		return null;
	}
}
