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

    AttributeKind(String xmlName) {
        this.xmlName = xmlName;
    }

    @Override
    public String toString() {
        return xmlName;
    }

    public static AttributeKind forName(String name) {
        return switch (name) {
            case "changeable" -> CHANGEABLE;
            case "constant" -> CONSTANT;
            case "derived" -> DERIVED_ON_THE_FLY;
            case "computed" -> DERIVED_BY_EXPLICIT_METHOD_CALL;
            default -> null;
        };
    }
}
