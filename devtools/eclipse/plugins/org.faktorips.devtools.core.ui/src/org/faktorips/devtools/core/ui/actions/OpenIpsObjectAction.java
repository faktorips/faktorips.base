/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Daniel Hohenberger
 * @author Cornelius Dirmeier
 */
public class OpenIpsObjectAction extends Action implements IWorkbenchWindowActionDelegate {

    public OpenIpsObjectAction() {
        super();
        setText(Messages.OpenIpsObjectAction_titleText);
        setDescription(Messages.OpenIpsObjectAction_description);
        setToolTipText(Messages.OpenIpsObjectAction_tooltip);
        setAccelerator(SWT.CTRL | SWT.SHIFT | 'I');
        setImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor("OpenIpsObject.gif")); //$NON-NLS-1$
    }

    @Override
    public void run() {
        IWorkbenchWindow activeWorkbenchWindow = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        Shell parent = activeWorkbenchWindow.getShell();
        boolean onlyProdDefs = IpsPlugin.getDefault().isProductDefinitionPerspective();
        OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(parent,
                Messages.OpenIpsObjectAction_dialogTitle, new OpenIpsObjectContext(onlyProdDefs), true);
        String selectedText = getSelectedText(activeWorkbenchWindow);
        dialog.setFilter(StringUtil.unqualifiedName(selectedText));
        if (dialog.open() == Window.OK) {
            ArrayList<IIpsElement> objects = dialog.getSelectedObjects();
            for (IIpsElement ipsElement : objects) {
                if (ipsElement instanceof IIpsSrcFile) {
                    IIpsObject ipsObject = ((IIpsSrcFile)ipsElement).getIpsObject();
                    IpsUIPlugin.getDefault().openEditor(ipsObject);
                }
            }
        }

    }

    protected String getSelectedText(IWorkbenchWindow activeWorkbenchWindow) {
        String selectedText = IpsStringUtils.EMPTY;
        ISelection selection = activeWorkbenchWindow.getSelectionService().getSelection();
        if (selection instanceof ITextSelection) {
            ITextSelection textSelection = (ITextSelection)selection;
            selectedText = textSelection.getText();
        }
        return selectedText;
    }

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void dispose() {
        // do nothing.
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // do nothing.
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }

}
