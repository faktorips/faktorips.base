/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.valueset;

import java.util.Arrays;
import java.util.List;

import org.faktorips.devtools.model.internal.ipsobject.DescriptionHelper;
import org.faktorips.devtools.model.internal.valueset.DerivedValueSet;
import org.faktorips.devtools.model.internal.valueset.EnumValueSet;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.internal.valueset.StringLengthValueSet;
import org.faktorips.devtools.model.internal.valueset.UnrestrictedValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;
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
    UNRESTRICTED("allValues", Messages.ValueSetType__allValues, ValueToXmlHelper.XML_TAG_ALL_VALUES) { //$NON-NLS-1$
        @Override
        public IUnrestrictedValueSet newValueSet(IValueSetOwner parent, String id) {
            return new UnrestrictedValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type enumeration.
     */
    ENUM("enum", Messages.ValueSetType_enumeration, ValueToXmlHelper.XML_TAG_ENUM) { //$NON-NLS-1$
        @Override
        public IEnumValueSet newValueSet(IValueSetOwner parent, String id) {
            return new EnumValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type range.
     */
    RANGE("range", Messages.ValueSetType_range, ValueToXmlHelper.XML_TAG_RANGE) { //$NON-NLS-1$
        @Override
        public IRangeValueSet newValueSet(IValueSetOwner parent, String id) {
            return new RangeValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type derived. Values are not configured but determined at runtime by
     * implementing the getValueSet method.
     * 
     * @since 20.6
     */
    DERIVED("derived", Messages.ValueSetType_derived, ValueToXmlHelper.XML_TAG_DERIVED) { //$NON-NLS-1$
        @Override
        public IDerivedValueSet newValueSet(IValueSetOwner parent, String id) {
            return new DerivedValueSet(parent, id);
        }
    },

    /**
     * Defines the value set type stringLength. String values are restricted by maximum length.
     * 
     * @since 20.6
     */
    STRINGLENGTH("stringLength", Messages.ValueSetType_stringLength, ValueToXmlHelper.XML_TAG_STRINGLENGTH) { //$NON-NLS-1$
        @Override
        public IStringLengthValueSet newValueSet(IValueSetOwner parent, String id) {
            return new StringLengthValueSet(parent, id);
        }
    };

    private final String id;

    private final String name;

    private final String xmlTag;

    ValueSetType(String id, String name, String xmlTag) {
        this.id = id;
        this.name = name;
        this.xmlTag = xmlTag;
    }

    /**
     * Creates a new value set of the type this method is invoked on.
     */
    public abstract IValueSet newValueSet(IValueSetOwner parent, String id);

    /**
     * Creates a new <code>IValueSet</code> - the type of the value set is derived from the given
     * XML element.
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
        return Arrays.stream(values())
                .filter(v -> v.xmlTag.equals(tagName))
                .findFirst()
                .map(v -> v.newValueSet(parent, id))
                .orElse(null);
    }

    public static List<ValueSetType> getValueSetTypesAsList() {
        return Arrays.asList(values());
    }

    public static List<ValueSetType> getNumericValueSetTypesAsList() {
        return Arrays.asList(ValueSetType.UNRESTRICTED, ValueSetType.RANGE, ValueSetType.ENUM, ValueSetType.DERIVED);
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

    /**
     * @since 20.6
     */
    public boolean isDerived() {
        return this == ValueSetType.DERIVED;
    }

    /**
     * @since 20.6
     */
    public boolean isStringLength() {
        return this == ValueSetType.STRINGLENGTH;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
