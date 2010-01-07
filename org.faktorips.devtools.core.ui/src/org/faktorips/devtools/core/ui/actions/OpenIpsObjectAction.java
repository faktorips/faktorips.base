/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsProductDefinitionPerspectiveFactory;
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

    private String perspective;

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
        Shell parent = IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
        perspective = getCurrentPerspective().getId();
        try {
            boolean onlyProdDefs = perspective
                    .equals(IpsProductDefinitionPerspectiveFactory.PRODUCTDEFINITIONPERSPECTIVE_ID);
            if (perspective.equals("org.eclipse.team.ui.TeamSynchronizingPerspective")) { //$NON-NLS-1$
                return;
            }
            OpenIpsObjectSelectionDialog dialog = new OpenIpsObjectSelectionDialog(parent,
                    Messages.OpenIpsObjectAction_dialogTitle, new OpenIpsObjectContext(onlyProdDefs));
            dialog.setFilter(StringUtil.unqualifiedName(super.getText()));
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

    public IPerspectiveDescriptor getCurrentPerspective() {
        return IpsPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
    }

    // ---- IWorkbenchWindowActionDelegate ------------------------------------------------

    public void run(IAction action) {
        run();
    }

    public void dispose() {
        // do nothing.
    }

    public void init(IWorkbenchWindow window) {
        // do nothing.
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }
}
