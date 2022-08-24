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

import org.faktorips.devtools.abstraction.util.IpsNLS;

public class Messages extends IpsNLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.builder.messages"; //$NON-NLS-1$

    static {
        IpsNLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsBuilder_ipsSrcFileNotParsable;
    public static String IpsBuilder_missingManifestMf;
    public static String IpsBuilder_msgExceptionWhileBuildingDependentProjects;
    public static String IpsBuilder_msgBuildResults;
    public static String IpsBuilder_msgInvalidProperties;
    public static String IpsBuilder_validatingProject;
    public static String IpsBuilder_preparingBuild;
    public static String IpsBuilder_startFullBuild;
    public static String IpsBuilder_startIncrementalBuild;
    public static String IpsBuilder_finishBuild;
    public static String IpsBuilder_deleting;
    public static String IpsBuilder_building;
    public static String IpsLoggingFrameworkConnectorPropertyDef_Deprecated;

}
