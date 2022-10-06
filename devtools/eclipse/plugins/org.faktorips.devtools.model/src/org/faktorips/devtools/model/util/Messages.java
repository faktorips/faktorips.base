/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.util.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsProjectCreation_defaultRuntimeIdPrefix;
    public static String IpsProjectCreation_defaultBasePackageName;
    public static String IpsProjectCreation_defaultSourceFolderName;
    public static String IpsProjectCreationProperties_basePackageName;
    public static String IpsProjectCreationProperties_locales;
    public static String IpsProjectCreationProperties_MsgText_MissingProperty;
    public static String IpsProjectCreationProperties_persistenceSupport;
    public static String IpsProjectCreationProperties_runtimeIdPrefix;
    public static String IpsProjectCreationProperties_sourceFolderName;

    public static String ProjectUtil_msgSourceInProjectImpossible;
}
