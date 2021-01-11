/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.model.bf.messages"; //$NON-NLS-1$

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String BFElementType_bfCallAction;
    public static String BFElementType_decision;
    public static String BFElementType_end;
    public static String BFElementType_inlineAction;
    public static String BFElementType_merge;
    public static String BFElementType_methodCallAction;
    public static String BFElementType_methodCallDecision;
    public static String BFElementType_parameter;
    public static String BFElementType_start;
    public static String BusinessFunctionIpsObjectType_displayName;
    public static String BusinessFunctionIpsObjectType_displayNamePlural;

}
