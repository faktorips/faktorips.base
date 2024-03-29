/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modeldescription.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String DefaultModelDescriptionPage_NoDescriptionAvailable;
    public static String DefaultModelDescriptionPage_SortDescription;
    public static String DefaultModelDescriptionPage_SortText;
    public static String DefaultModelDescriptionPage_SortTooltipText;
    public static String ModelDescriptionView_notSupported;
    public static String DefaultModelDescriptionPage_FilterEmptyDescription;
    public static String DefaultModelDescriptionPage_FilterEmptyText;
    public static String DefaultModelDescriptionPage_FilterEmptyTooltipText;
    public static String DefaultModelDescriptionPage_FilterDescriptionsByType;
    public static String DefaultModelDescriptionPage_FilterDescriptionsByTypeText;
    public static String DefaultModelDescriptionPage_FilterDescriptionsByTypeTooltipText;
    public static String DefaultModelDescriptionPage_ErrorIpsModelNotFound;
    public static String DefaultModelDescriptionPage_GeneralInformation;
    public static String DefaultModelDescriptionPage_FilterDescriptionsByTypeDialogTitle;
    public static String FilterDescriptionsByTypeDialog_getTextForAssociation;
    public static String FilterDescriptionsByTypeDialog_getTextForRule;
    public static String FilterDescriptionsByTypeDialog_getTextForAttribute;
    public static String FilterDescriptionsByTypeDialog_getTextForTable;
    public static String FilterDescriptionsByTypeDialog_getTextForMethod;
}
