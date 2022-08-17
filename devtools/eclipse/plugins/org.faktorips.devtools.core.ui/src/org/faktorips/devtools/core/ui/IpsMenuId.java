/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.search.ui.IContextMenuConstants;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * Enumerates all menu IDs, toolbar IDs and group IDs provided by Faktor-IPS.
 * <p>
 * The enum additionally provides some methods to directly create separators or group markers. Using
 * this methods some conditions may be checked for example if we only want to show simple menus.
 * <p>
 * Note that most group markers have the prefix <em>fips.</em> to distinguish from eclipse group
 * markers. For example we use the group marker <em>fips.edit</em> instead of <em>group.edit</em>.
 * Using this own id we could contribute to any context menu (popup:org.eclipse.ui.popup.any)
 * without worrying about providing to some standard menus.
 * 
 * @author Alexander Weickmann
 */
public enum IpsMenuId {

    TOOLBAR_ENUM_TYPE_EDITOR_PAGE("org.faktorips.devtools.core.ui.editors.enumtype.enumTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_POLICY_CMPT_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.pctype.policyCmptTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_PRODUCT_CMPT_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.productcmpttype.productCmptTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_TEST_CASE_TYPE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.testcasetype.testCaseTypeEditorPage.toolbar"), //$NON-NLS-1$

    TOOLBAR_TABLE_STRUCTURE_EDITOR_PAGE(
            "org.faktorips.devtools.core.ui.editors.tablestructure.tableStructureEditorPage.toolbar"), //$NON-NLS-1$

    GROUP_NEW_PRODUCTC("fips.new"), //$NON-NLS-1$

    GROUP_COPY_PRODUCTC("fips.copy.product"), //$NON-NLS-1$

    GROUP_JUMP_TO_SOURCE_CODE("jumpToSourceCode") { //$NON-NLS-1$

        @Override
        protected boolean isEnabled() {
            return !IpsPlugin.getDefault().isProductDefinitionPerspective();
        }

    },
    GROUP_REFACTORING("refactoring"), //$NON-NLS-1$

    GROUP_EDIT("fips.edit") { //$NON-NLS-1$
        @Override
        protected boolean isEnabled() {
            return !IpsPlugin.getDefault().getIpsPreferences().isSimpleContextMenuEnabled();
        }
    },

    GROUP_PROPERTIES("fips.properties") { //$NON-NLS-1$
        @Override
        protected boolean isEnabled() {
            return !IpsPlugin.getDefault().getIpsPreferences().isSimpleContextMenuEnabled();
        }
    },

    GROUP_NAVIGATE("navigate"), //$NON-NLS-1$

    GROUP_GOTO("fips.goto") { //$NON-NLS-1$

        @Override
        protected boolean isEnabled() {
            return !IpsPlugin.getDefault().isProductDefinitionPerspective();
        }

    },

    GROUP_ADDITIONS(IContextMenuConstants.GROUP_ADDITIONS);

    private final String id;

    IpsMenuId(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of this Faktor-IPS menu, toolbar or group.
     */
    public String getId() {
        return id;
    }

    public static void addDefaultGroups(IMenuManager manager) {
        GROUP_EDIT.addSeparator(manager);
        GROUP_REFACTORING.addSeparator(manager);
        GROUP_NAVIGATE.addSeparator(manager);
        GROUP_GOTO.addGroupMarker(manager);
        GROUP_ADDITIONS.addSeparator(manager);
        GROUP_PROPERTIES.addSeparator(manager);
    }

    protected boolean isEnabled() {
        return true;
    }

    public void addGroupMarker(IMenuManager manager) {
        if (isEnabled()) {
            manager.add(new GroupMarker(getId()));
        }
    }

    public void addSeparator(IMenuManager manager) {
        if (isEnabled()) {
            manager.add(new Separator(getId()));
        }
    }
}
