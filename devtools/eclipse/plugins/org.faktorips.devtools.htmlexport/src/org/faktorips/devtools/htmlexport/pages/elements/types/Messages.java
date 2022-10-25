/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.pages.elements.types;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.htmlexport.pages.elements.types.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductGenerationAttributeTable_nullIncluded;

    public static String ProductGenerationAttributeTable_nullNotIncluded;

    public static String ProductGenerationAttributeTable_emptyValueSet;

    public static String ProductGenerationAttributeTable_emptyDefaultValue;

    public static String ProductGenerationAttributeTable_Mandatory;

    public static String ProductGenerationAttributeTable_Optional;

    public static String ProductGenerationAttributeTable_Irrelevant;

    public static String ProductGenerationAttributeTable_AttributeRelevance;

    public static String ProductGenerationAttributeTable_EmptyRange;

    public static String ProductGenerationAttributeTable_DefaultValue;

    public static String ProductGenerationAttributeTable_ValueSet;

    public static String ProductGenerationAttributeTable_valueSetUnrestricted;

}
