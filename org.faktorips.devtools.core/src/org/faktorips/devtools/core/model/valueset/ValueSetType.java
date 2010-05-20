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

package org.faktorips.devtools.core.model.valueset;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.enums.DefaultEnumType;
import org.faktorips.devtools.core.enums.DefaultEnumValue;
import org.faktorips.devtools.core.enums.EnumType;
import org.faktorips.devtools.core.enums.EnumValue;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.w3c.dom.Element;

/**
 * The kind of value set.
 * 
 * @author Jan Ortmann
 */
public class ValueSetType extends DefaultEnumValue {

    /**
     * Defines the value set type that does not restrict the values in the set. All values allowed
     * by the data type are allowed.
     */
    public final static ValueSetType UNRESTRICTED;

    /**
     * Defines the value set type range.
     */
    public final static ValueSetType RANGE;

    /**
     * Defines the value set type enumeration.
     */
    public final static ValueSetType ENUM;

    /**
     * Defines a value set type that has yet to be specified (on the product side) as either
     * enumeration or range.
     */
    private final static DefaultEnumType enumType;

    static {
        enumType = new DefaultEnumType("ValueSetType", ValueSetType.class); //$NON-NLS-1$
        UNRESTRICTED = new ValueSetType(enumType, "allValues", Messages.ValueSetType__allValues); //$NON-NLS-1$
        ENUM = new ValueSetType(enumType, "enum", Messages.ValueSetType_enumeration); //$NON-NLS-1$ 
        RANGE = new ValueSetType(enumType, "range", Messages.ValueSetType_range); //$NON-NLS-1$ 
    }

    public final static EnumType getEnumType() {
        return enumType;
    }

    /**
     * Returns the value set type identified by the id. Returns <code>null</code> if the id does not
     * identify a value set type.
     */
    public final static ValueSetType getValueSetType(String id) {
        return (ValueSetType)enumType.getEnumValue(id);
    }

    /**
     * Returns the value set type identified by the name. Returns <code>null</code> if the name does
     * not identify a value set type.
     */
    public final static ValueSetType getValueSetTypeByName(String name) {
        ValueSetType[] types = getValueSetTypes();
        for (ValueSetType type2 : types) {
            if (type2.getName().equals(name)) {
                return type2;
            }
        }
        return null;
    }

    public final static ValueSetType[] getValueSetTypes() {
        EnumValue[] values = getEnumType().getValues();
        ValueSetType[] types = new ValueSetType[values.length];
        System.arraycopy(values, 0, types, 0, values.length);
        return types;
    }

    public final static List<ValueSetType> getValueSetTypesAsList() {
        EnumValue[] values = getEnumType().getValues();
        List<ValueSetType> types = new ArrayList<ValueSetType>();
        for (EnumValue value : values) {
            types.add((ValueSetType)value);
        }
        return types;
    }

    private ValueSetType(DefaultEnumType type, String id, String name) {
        super(type, id, name);
    }

    /**
     * Creates a new value set of the type this method is invoked on.
     */
    public IValueSet newValueSet(IIpsObjectPart parent, String id) {
        if (this == UNRESTRICTED) {
            return new UnrestrictedValueSet(parent, id);
        } else if (this == ENUM) {
            return new EnumValueSet(parent, id);
        } else if (this == RANGE) {
            return new RangeValueSet(parent, id);
        }
        return null;
    }

    /**
     * Creates a new <tt>IValueSet</tt> - the type of the value set is derived from the given XML
     * element.
     * 
     * @param valueSetNode The node describing the value set.
     * @param parent The parent for the new value set.
     * @param id The IPS object part id for the new value set.
     */
    public static IValueSet newValueSet(Element valueSetNode, IIpsObjectPart parent, String id) {
        Element element = DescriptionHelper.getFirstNoneDescriptionElement(valueSetNode);
        if (element == null) {
            return null;
        }
        String tagName = element.getNodeName();
        if (tagName.equals(EnumValueSet.XML_TAG)) {
            return new EnumValueSet(parent, id);
        } else if (tagName.equals(RangeValueSet.XML_TAG)) {
            return new RangeValueSet(parent, id);
        } else if (tagName.equals(UnrestrictedValueSet.XML_TAG)) {
            return new UnrestrictedValueSet(parent, id);
        }
        return null;
    }

    public boolean isUnrestricted() {
        return this == ValueSetType.UNRESTRICTED;
    }

    public boolean isRange() {
        return this == ValueSetType.RANGE;
    }

    public boolean isEnum() {
        return this == ValueSetType.ENUM;
    }

}
