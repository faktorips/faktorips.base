/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.model.type;

/**
 * The possible kinds of attributes.
 */
public enum AttributeKind {

    /**
     * Can be modified by calling it's setter.
     */
    CHANGEABLE("changeable"),

    /**
     * Can not be modified.
     */
    CONSTANT("constant"),

    /**
     * The value is calculated on every call to the getter.
     */
    DERIVED_ON_THE_FLY("derived"),

    /**
     * The value is calculated by the call to another method.
     */
    DERIVED_BY_EXPLICIT_METHOD_CALL("computed");

    private final String xmlName;

    private AttributeKind(String xmlName) {
        this.xmlName = xmlName;
    }

    @Override
    public String toString() {
        return xmlName;
    }

    public static AttributeKind forName(String name) {
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