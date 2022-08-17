/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelstructure;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modelstructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ModelStructure_tooltipToggleButton;
    public static String ModelStructure_menuShowCardinalities_name;
    public static String ModelStructure_menuShowCardinalities_tooltip;
    public static String ModelStructure_menuShowRoleName_name;
    public static String ModelStructure_menuShowRoleName_tooltip;
    public static String ModelStructure_contextMenuOpenAssociationTargetingTypeEditor;
    public static String ModelStructure_waitingLabel;
    public static String ModelStructure_emptyMessage;
    public static String ModelStructure_menuShowProjects_name;
    public static String ModelStructure_menuShowProjects_tooltip;
    public static String ModelStructure_NothingToShow_message;

    public static String ModelStructure_tooltipInheritedAssociations;
    public static String ModelStructure_tooltipHasInheritedAssociation;

    public static String ModelStructure_tooltipRefreshContents;
}
