/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.faktorips.devtools.core.ui.commands.IpsAbstractHandler;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.stdbuilder.ui.StdBuilderUICommandId;
import org.faktorips.devtools.stdbuilder.ui.dynamicmenus.JumpToSourceCodeDynamicMenuContribution;

/**
 * @see StdBuilderUICommandId#COMMAND_OPEN_JUMP_TO_SOURCE_CODE_CONTEXT_MENU
 * 
 * @author Alexander Weickmann
 */
public class OpenJumpToSourceCodeContextMenuHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {

        JumpToSourceCodeDynamicMenuContribution jumpToSourceCodeDynamic = new JumpToSourceCodeDynamicMenuContribution();
        jumpToSourceCodeDynamic.initialize(activePage.getWorkbenchWindow());

        MenuManager menuManager = new MenuManager();
        for (IContributionItem contributionItem : jumpToSourceCodeDynamic.getContributionItems()) {
            menuManager.add(contributionItem);
        }

        Control cursorControl = activePage.getActiveEditor().getSite().getShell().getDisplay().getCursorControl();
        Menu menu = menuManager.createContextMenu(cursorControl);
        menu.setVisible(true);
    }

}
