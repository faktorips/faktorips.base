/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.pctype;

import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.MenuUtil;
import org.faktorips.devtools.core.ui.IpsMenuId;
import org.faktorips.devtools.core.ui.editors.type.TypeEditorPage;

abstract class PolicyCmptTypeEditorPage extends TypeEditorPage {

    public PolicyCmptTypeEditorPage(PolicyCmptTypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour,
            String title, String pageId) {

        super(editor, twoSectionsWhenTrueOtherwiseFour, title, pageId);
    }

    @Override
    protected void createToolbarActions(IToolBarManager toolbarManager) {
        toolbarManager.add(new Separator(IpsMenuId.GROUP_JUMP_TO_SOURCE_CODE.getId()));

        IMenuService menuService = (IMenuService)getSite().getService(IMenuService.class);
        menuService.populateContributionManager((ContributionManager)toolbarManager,
                MenuUtil.toolbarUri(IpsMenuId.TOOLBAR_POLICY_CMPT_TYPE_EDITOR_PAGE.getId()));
    }

}
