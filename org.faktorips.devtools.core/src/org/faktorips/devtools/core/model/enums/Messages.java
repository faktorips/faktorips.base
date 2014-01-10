/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.enums;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.enums.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String EnumType_SupertypeDoesNotExist;
    public static String EnumType_SupertypeIsNotAbstract;
    public static String EnumType_cycleDetected;
    public static String EnumType_inconsistentHierarchy;
    public static String EnumType_EnumContentNameEmpty;

    public static String EnumContent_EnumTypeMissing;
    public static String EnumContent_EnumTypeDoesNotExist;
    public static String EnumContent_EnumTypeIsAbstract;
    public static String EnumContent_ValuesArePartOfType;
    public static String EnumContent_EnumContentNameNotCorrect;

}
