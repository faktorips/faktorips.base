/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.eclipse.emf.codegen;

/**
 * Defines on which methods additional annotations are generated.
 *
 * @since 24.1
 */
public enum AdditionalAnnotationsLocation {

    GeneratedAndRestrainedModifiable,
    OnlyGenerated;

    /**
     * Parses the setting from the Faktor-IPS generator page.
     */
    public static AdditionalAnnotationsLocation fromString(String name) {
        if (OnlyGenerated.name().equalsIgnoreCase(name)) {
            return OnlyGenerated;
        }
        return GeneratedAndRestrainedModifiable;
    }
}
