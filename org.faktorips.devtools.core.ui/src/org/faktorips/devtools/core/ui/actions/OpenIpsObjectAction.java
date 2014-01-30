/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
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
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectContext;
import org.faktorips.devtools.core.ui.dialogs.OpenIpsObjectSelectionDialog;
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
        try {
            boolean onlyProdDefs = IpsPlugin.getDefault().isProductDefinitionPerspective();
            OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(parent,
                    Messages.OpenIpsObjectAction_dialogTitle, new OpenIpsObjectContext(onlyProdDefs));
            String selectedText = getSelectedText(activeWorkbenchWindow);
            dialog.setFilter(StringUtil.unqualifiedName(selectedText));
            if (dialog.open() == Window.OK) {
                IIpsElement object = dialog.getSelectedObject();
                if (object != null) {
                    if (object instanceof IIpsSrcFile) {
                        IIpsObject ipsObject = ((IIpsSrcFile)object).getIpsObject();
                        IpsUIPlugin.getDefault().openEditor(ipsObject);
                    }

                } else {
                    return;
                }
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }

    }

    protected String getSelectedText(IWorkbenchWindow activeWorkbenchWindow) {
        String selectedText = StringUtils.EMPTY;
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
