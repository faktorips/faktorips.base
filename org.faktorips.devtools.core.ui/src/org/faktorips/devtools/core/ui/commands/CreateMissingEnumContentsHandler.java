/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.actions.CreateMissingEnumContentsAction;

/**
 * This handler simply used the old IpsAction {@link CreateMissingEnumContentsAction} and wrap it to
 * a command handler to use in the eclipse command framework
 * 
 * @author dirmeier
 */
public class CreateMissingEnumContentsHandler extends IpsAbstractHandler {

    @Override
    public void execute(ExecutionEvent event, IWorkbenchPage activePage, IIpsSrcFile ipsSrcFile)
            throws ExecutionException {
        IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
        ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
        if (currentSelection instanceof IStructuredSelection) {
            new CreateMissingEnumContentsAction(null, workbenchWindow).run((IStructuredSelection)currentSelection);
        }
    }

}
