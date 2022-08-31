/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productdefinitionexplorer;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.productdefinitionexplorer.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ProductExplorer_MenuShowProdDefProjectsOnly_Title;
    public static String ProductExplorer_MenuShowProdDefProjectsOnly_Tooltip;
    public static String ProductExplorer_subMenuTeam;
    public static String ProductExplorer_subMenuCompareWith;
    public static String ProductExplorer_subMenuReplaceWith;

    public static String ProductExplorer_actionSync_label;
    public static String ProductExplorer_actionSync_tooltip;
    public static String ProductExplorer_actionCommit_label;
    public static String ProductExplorer_actionCommit_tooltip;
    public static String ProductExplorer_actionUpdate_label;
    public static String ProductExplorer_actionUpdate_tooltip;
    public static String ProductExplorer_actionTag_label;
    public static String ProductExplorer_actionTag_tooltip;
    public static String ProductExplorer_actionBranch_label;
    public static String ProductExplorer_actionBranch_tooltip;
    public static String ProductExplorer_actionSwitchBranch_label;
    public static String ProductExplorer_actionSwitchBranch_tooltip;
    public static String ProductExplorer_actionShowResourceHistory_label;
    public static String ProductExplorer_actionShowResourceHistory_tooltip;
    public static String ProductExplorer_actionRestoreFromRepositoryAction_label;
    public static String ProductExplorer_actionRestoreFromRepositoryAction_tooltip;

    public static String ProductExplorer_actionCompareWithLatest_label;
    public static String ProductExplorer_actionCompareWithLatest_tooltip;
    public static String ProductExplorer_actionCompareWithBranch_label;
    public static String ProductExplorer_actionCompareWithBranch_tooltip;
    public static String ProductExplorer_actionCompareWithEachOther_label;
    public static String ProductExplorer_actionCompareWithEachOther_tooltip;
    public static String ProductExplorer_actionCompareWithRevision_label;
    public static String ProductExplorer_actionCompareWithRevision_tooltip;
    public static String ProductExplorer_actionCompareWithLocalHistory_label;
    public static String ProductExplorer_actionCompareWithLocalHistory_tooltip;

    public static String ProductExplorer_actionReplaceWithLatest_label;
    public static String ProductExplorer_actionReplaceWithLatest_tooltip;
    public static String ProductExplorer_actionReplaceWithBranch_label;
    public static String ProductExplorer_actionReplaceWithBranch_tooltip;
    public static String ProductExplorer_actionReplaceWithRevision_label;
    public static String ProductExplorer_actionReplaceWithRevision_tooltip;
    public static String ProductExplorer_actionReplaceWithPreviousFromLocalHistory_label;
    public static String ProductExplorer_actionReplaceWithPreviousFromLocalHistory_tooltip;
    public static String ProductExplorer_actionReplaceWithLocalHistory_label;
    public static String ProductExplorer_actionReplaceWithLocalHistory_tooltip;

    public static String ProductExplorer_labelRenameRefactoring;
    public static String ProductExplorer_labelMoveRefactoring;

}
