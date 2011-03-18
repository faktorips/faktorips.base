/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchPage;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.commands.IpsAbstractHandler;
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
