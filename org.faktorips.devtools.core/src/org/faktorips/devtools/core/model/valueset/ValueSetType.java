/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.valueset;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.internal.model.valueset.UnrestrictedValueSet;
import org.w3c.dom.Element;

/**
 * The kind of value set.
 * 
 * @author Jan Ortmann
 */
public enum ValueSetType {

    /**
     * Defines the value set type that does not restrict the values in the set. All values allowed
     * by the data type are allowed.
     */
    UNRESTRICTED("allValues", Messages.ValueSetType__allValues) { //$NON-NLS-1$
        @Override
        public IValueSet newValueSet(IValueSetOwner parent, String id) {
            return new UnrestrictedValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type enumeration.
     */
    ENUM("enum", Messages.ValueSetType_enumeration) { //$NON-NLS-1$
        @Override
        public IValueSet newValueSet(IValueSetOwner parent, String id) {
            return new EnumValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type range.
     */
    RANGE("range", Messages.ValueSetType_range) { //$NON-NLS-1$
        @Override
        public IValueSet newValueSet(IValueSetOwner parent, String id) {
            return new RangeValueSet(parent, id);
        }
    };

    private final String id;

    private final String name;

    private ValueSetType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Creates a new value set of the type this method is invoked on.
     */
    public abstract IValueSet newValueSet(IValueSetOwner parent, String id);

    /**
     * Creates a new <tt>IValueSet</tt> - the type of the value set is derived from the given XML
     * element.
     * 
     * @param valueSetNode The node describing the value set.
     * @param parent The parent for the new value set.
     * @param id The IPS object part id for the new value set.
     */
    public static IValueSet newValueSet(Element valueSetNode, IValueSetOwner parent, String id) {
        Element element = DescriptionHelper.getFirstNoneDescriptionElement(valueSetNode);
        if (element == null) {
            return null;
        }
        String tagName = element.getNodeName();
        if (tagName.equals(EnumValueSet.XML_TAG_ENUM)) {
            return new EnumValueSet(parent, id);
        } else if (tagName.equals(RangeValueSet.XML_TAG_RANGE)) {
            return new RangeValueSet(parent, id);
        } else if (tagName.equals(UnrestrictedValueSet.XML_TAG_UNRESTRICTED)) {
            return new UnrestrictedValueSet(parent, id);
        }
        return null;
    }

    public static List<ValueSetType> getValueSetTypesAsList() {
        return Arrays.asList(values());
    }

    public static ValueSetType getValueSetTypeByName(String name) {
        for (ValueSetType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
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

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
