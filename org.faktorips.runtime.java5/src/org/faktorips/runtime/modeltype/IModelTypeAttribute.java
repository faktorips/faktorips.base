/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.modeltype;

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
     * Enum defining the possible attribute types.
     */
    public enum AttributeType {
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
     * Returns the type of value set restricting this attribute
     */
    public ValueSetType getValueSetType();

    /**
     * Enum defining the possible value set types.
     */
    public enum ValueSetType {
        Enum,
        Range,
        AllValues;
    }

    /**
     * Returns if this attribute is product relevant.
     */
    public boolean isProductRelevant();

}
