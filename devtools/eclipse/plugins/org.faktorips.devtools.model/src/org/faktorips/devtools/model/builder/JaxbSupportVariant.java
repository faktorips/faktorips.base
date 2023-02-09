/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Variants of JAXB-Support.
 * 
 * @since 23.6
 */
public enum JaxbSupportVariant {
    /** No JAXB **/
    None("", ""),
    /** Classic JAXB, **/
    ClassicJAXB("javax.xml.bind.", "org.faktorips.runtime.xml.javax."),
    /** Jakarta XML Binding 3 **/
    JakartaXmlBinding3("jakarta.xml.bind.", "org.faktorips.runtime.xml.jakarta3.");

    private final String libraryPackage;
    private final String ipsPackage;

    JaxbSupportVariant(String libraryPackage, String ipsPackage) {
        this.libraryPackage = libraryPackage;
        this.ipsPackage = ipsPackage;
    }

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

    public String getLibraryPackage() {
        return libraryPackage;
    }

    public String getIpsPackage() {
        return ipsPackage;
    }
}
