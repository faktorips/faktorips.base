/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xmodel;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Variants of JAXB-Support.
 * 
 * @since 23.6
 */
public enum JaxbSupportVariant {
    /** No JAXB **/
    None,
    /** Classic JAXB, javax.xml.bind.* */
    Javax,
    /** Jakarta XML Binding 3, jakarta.xml.bind.* */
    Jakarta3;

    public static JaxbSupportVariant of(String configValue) {
        if (IpsStringUtils.isBlank(configValue)) {
            return None;
        }
        String trimmedValue = configValue.trim();
        if ("true (Jakarta XML Binding 3.0)".equalsIgnoreCase(trimmedValue)) {
            return Jakarta3;
        }
        return Boolean.parseBoolean(trimmedValue) ? Javax : None;
    }
}
