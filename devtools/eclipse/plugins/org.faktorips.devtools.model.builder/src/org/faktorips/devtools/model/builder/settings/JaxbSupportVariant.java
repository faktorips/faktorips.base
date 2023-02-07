/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder.settings;

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
    ClassicJAXB,
    /** Jakarta XML Binding 3, jakarta.xml.bind.* */
    JakartaXmlBinding3;

    public static JaxbSupportVariant of(String configValue) {
        if (null == configValue) {
            return None;
        }
        switch (configValue.trim().toLowerCase()) {
            case IpsStringUtils.EMPTY:
            case "false":
            case "none":
                return None;

            case "jakartaxmlbinding3":
                return JakartaXmlBinding3;

            default:
                return ClassicJAXB;
        }
    }
}
