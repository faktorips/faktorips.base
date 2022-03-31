/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.model.ipsobject.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectType_nameEnumContent;
    public static String IpsObjectType_nameEnumType;
    public static String IpsObjectType_namePolicyClass;
    public static String IpsObjectType_nameProductClass;
    public static String IpsObjectType_nameTableStructure;
    public static String IpsObjectType_nameProductComponent;
    public static String IpsObjectType_nameProductTemplate;
    public static String IpsObjectType_nameTableContents;
    public static String IpsObjectType_nameTestCaseType;
    public static String IpsObjectType_nameTestCase;
    public static String IpsObjectType_nameEnumContentPlural;
    public static String IpsObjectType_nameEnumTypePlural;
    public static String IpsObjectType_namePolicyClassPlural;
    public static String IpsObjectType_nameProductClassPlural;
    public static String IpsObjectType_nameTableStructurePlural;
    public static String IpsObjectType_nameProductComponentPlural;
    public static String IpsObjectType_nameProductTemplatePlural;
    public static String IpsObjectType_nameTableContentsPlural;
    public static String IpsObjectType_nameTestCaseTypePlural;
    public static String IpsObjectType_nameTestCasePlural;

}
