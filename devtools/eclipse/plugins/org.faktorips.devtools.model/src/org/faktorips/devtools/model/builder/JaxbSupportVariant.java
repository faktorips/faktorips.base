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
    /** Jakarta XML Binding **/
    JakartaXmlBinding("jakarta.xml.bind.", "org.faktorips.runtime.xml.jakarta.");

    /**
     * The Jaxb support ID used by the standard builder.
     */
    public static final String STD_BUILDER_PROPERTY_GENERATE_JAXB_SUPPORT = "generateJaxbSupport"; //$NON-NLS-1$
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
        return switch (configValue.trim().toLowerCase()) {
            case IpsStringUtils.EMPTY, "false", "none" -> None;
            case "jakartaxmlbinding" -> JakartaXmlBinding;
            default -> ClassicJAXB;
        };
    }

    public String getLibraryPackage() {
        return libraryPackage;
    }

    public String getIpsPackage() {
        return ipsPackage;
    }
}
