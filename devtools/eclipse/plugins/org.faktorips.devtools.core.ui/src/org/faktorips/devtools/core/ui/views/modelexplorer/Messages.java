/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modelexplorer.messages"; //$NON-NLS-1$
    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ModelExplorer_menuItemMove;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Title;
    public static String ModelExplorer_menuShowIpsProjectsOnly_Tooltip;
    public static String ModelExplorer_menuGroupCategories_Title;
    public static String ModelExplorer_menuGroupCategories_Tooltip;
    public static String ModelExplorer_submenuNew;
    public static String ModelExplorer_submenuLayout;
    public static String ModelExplorer_actionFlatLayout;
    public static String ModelExplorer_actionHierarchicalLayout;
    public static String ModelExplorer_submenuRefactor;
    public static String ModelExplorer_submenuCleanUp;
    public static String ModelExplorer_errorTitle;
    public static String ModelExplorer_defaultPackageLabel;
    public static String ModelExplorer_nonIpsProjectLabel;
    public static String ModelLabelProvider_noProductDefinitionProjectLabel;
    public static String OpenActionGroup_openWithMenuLabel;
    public static String ModelExlporer_selectedElements;

    public static String ModelExplorerDropListener_titleMove;

}
