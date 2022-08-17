/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

public abstract class AbstractTemplateToolBarMenuBuilder {

    private final ToolBar toolBar;

    public AbstractTemplateToolBarMenuBuilder(ToolBar toolBar) {
        super();
        this.toolBar = toolBar;
    }

    public Menu createTemplateMenue() {
        MenuManager menuManager = new MenuManager();
        initDynamicMenue(menuManager);
        return menuManager.createContextMenu(toolBar);
    }

    private void initDynamicMenue(MenuManager menuManager) {
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener2() {

            @Override
            public void menuAboutToShow(IMenuManager manager) {
                addOpenTemplateAction(manager);
                addShowTemplatePropertyUsageAction(manager);
            }

            @Override
            public void menuAboutToHide(IMenuManager manager) {
                // nothing to do
            }

        });
    }

    protected abstract void addOpenTemplateAction(IMenuManager manager);

    protected abstract void addShowTemplatePropertyUsageAction(IMenuManager manager);

}
