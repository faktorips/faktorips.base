/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import org.faktorips.runtime.model.type.Attribute;
import org.faktorips.runtime.model.type.AttributeKind;
import org.faktorips.runtime.model.type.ValueSetKind;

/**
 * 
 * @author Daniel Hohenberger
 * @deprecated Use {@link Attribute} directly. Will be removed in Faktor-IPS 3.20+
 */
@Deprecated
public interface IModelTypeAttribute extends IModelElement {

    /**
     * Returns the model type this attribute belongs to.
     */
    public IModelType getModelType();

    /**
     * @return this attribute's datatype <code>Class</code>.
     * @throws ClassNotFoundException if the datatype's class can not be loaded.
     */
    public Class<?> getDatatype() throws ClassNotFoundException;

    /**
     * @return the type of this attribute.
     */
    public AttributeType getAttributeType();

    /**
     * Returns the type of value set restricting this attribute
     */
    public ValueSetType getValueSetType();

    /**
     * Returns if this attribute is product relevant.
     */
    public boolean isProductRelevant();

    /**
     * Checks whether this attribute is changing over time. For product attribute that means the
     * attribute resides in the generation. For policy attributes the optional product configuration
     * ({@link #isProductRelevant()}) resides in the generation.
     * 
     * @return whether or not this attribute is changing over time.
     */
    public boolean isChangingOverTime();

    /**
     * Enum defining the possible value set types.
     * 
     * @deprecated Use {@link ValueSetKind}
     */
    @Deprecated
    public static enum ValueSetType {
        Enum,
        Range,
        AllValues;
    }

    /**
     * Enum defining the possible attribute types.
     * 
     * @deprecated Use {@link AttributeKind}
     */
    @Deprecated
    public static enum AttributeType {

        CHANGEABLE("changeable"),

        CONSTANT("constant"),

        DERIVED_ON_THE_FLY("derived"),

        DERIVED_BY_EXPLICIT_METHOD_CALL("computed");

        private final String xmlName;

        private AttributeType(String xmlName) {
            this.xmlName = xmlName;
        }

        @Override
        public String toString() {
            return xmlName;
        }

        public static AttributeType forName(String name) {
            if ("changeable".equals(name)) {
                return CHANGEABLE;
            }
            if ("constant".equals(name)) {
                return CONSTANT;
            }
            if ("derived".equals(name)) {
                return DERIVED_ON_THE_FLY;
            }
            if ("computed".equals(name)) {
                return DERIVED_BY_EXPLICIT_METHOD_CALL;
            }
            return null;
        }
    }

}
