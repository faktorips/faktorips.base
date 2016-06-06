/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

import org.faktorips.runtime.IModelObject;

/**
 * 
 * @author Daniel Hohenberger
 */
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
     * Enum defining the possible value set types.
     */
    public static enum ValueSetType {
        Enum,
        Range,
        AllValues;
    }

    /**
     * Enum defining the possible attribute types.
     */
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

    /**
     * Returns the value of the given model object's attribute identified by this model type
     * attribute.
     * 
     * @param modelObject a model object corresponding to the {@link IModelType} this attribute
     *            belongs to
     * @return the value of the given model object's attribute identified by this model type
     *         attribute
     * @throws IllegalArgumentException if the model object does not have an attribute fitting this
     *             model type attribute or that attribute is not accessible for any reason
     */
    public Object getValue(IModelObject modelObject);

    /**
     * Sets the given model object's attribute identified by this model type attribute to the given
     * value. This only works for attributes of type {@link AttributeType#CHANGEABLE}.
     * 
     * @param modelObject a model object corresponding to the {@link IModelType} this attribute
     *            belongs to
     * @param value an object of this model type attribute's datatype
     * @throws IllegalArgumentException if the model object does not have a changeable attribute
     *             fitting this model type attribute or that attribute is not accessible for any
     *             reason or the value does not fit the attribute's datatype.
     */
    public void setValue(IModelObject modelObject, Object value);

}
