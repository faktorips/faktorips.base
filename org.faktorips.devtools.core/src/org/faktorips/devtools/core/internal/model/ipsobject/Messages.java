/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.ipsobject.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String IpsObjectGeneration_msgDuplicateGeneration;
    public static String IpsObjectPartContainer_msgInvalidDescriptionCount;
    public static String IpsObjectPartContainer_msgInvalidLabelCount;
    public static String IpsObjectPartContainer_msgInvalidVersionFormat;

    public static String IpsObject_msg_OtherIpsObjectAlreadyInPathAhead;

    public static String TimedIpsObject_msgIvalidValidToDate;

    public static String IpsObjectGeneration_msgInvalidFromDate;
    public static String IpsObjectGeneration_msgInvalidFormatFromDate;

    public static String Label_msgLocaleMissing;
    public static String Label_msgLocaleNotSupportedByProject;

    public static String Description_msgLocaleMissing;
    public static String Description_msgLocaleNotSupportedByProject;

}
