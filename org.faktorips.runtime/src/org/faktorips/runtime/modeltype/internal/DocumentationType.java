/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal;

import java.util.Arrays;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Type for the different kinds of documentation messages.
 */
public enum DocumentationType {

    LABEL,

    PLURAL_LABEL,

    DESCRIPTION;

    public static final String QNAME_SEPARATOR = "-";

    /**
     * Creates a unique key to identify a message of this type, using the objectName, qualified name
     * type and partName separated by {@link #QNAME_SEPARATOR} and appending the name of the current
     * message type.
     */
    public String getKey(String objectName, String objectType, String partName) {
        return IpsStringUtils.join(Arrays.asList(objectName, objectType, partName, name()), QNAME_SEPARATOR);
    }
}