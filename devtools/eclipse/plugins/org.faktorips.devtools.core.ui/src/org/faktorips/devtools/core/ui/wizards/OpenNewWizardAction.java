/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.faktorips.devtools.core.ui.IpsUIPlugin.ImageHandling;
import org.osgi.framework.Bundle;

/**
 * A workbench window action delegate to open a new wizard dialog. The concrete wizard has to be
 * supplied by implementing <code>createWizard()</code> in the subclass.
 *
 * @author Jan Ortmann
 */
public abstract class OpenNewWizardAction implements IWorkbenchWindowActionDelegate {

    private static final String ICONS_FOLDER = "icons/";

    private IWorkbenchWindow window;

    @Override
    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {

        templateCheck(action);

        if (ImageHandling.isSvgSupported()) {
            ImageDescriptor descriptor = loadIcon(ICONS_FOLDER + getSvgIconName() + ".svg", getIconBundleId());
            if (descriptor != null) {
                action.setImageDescriptor(descriptor);
            }
        }
    }

    protected void templateCheck(IAction action) {
        // nothing to do
    }

    protected ImageDescriptor loadIcon(String path, String bundleLocation) {
        Bundle bundle = Platform.getBundle(bundleLocation);
        URL iconUrl = FileLocator.find(bundle, new Path(path), null);
        return iconUrl != null ? ImageDescriptor.createFromURL(iconUrl) : null;
    }

    /**
     * Implementations return the Wizard to be displayed in the WizardDialog.
     */
    public abstract INewWizard createWizard();

    @Override
    public void run(IAction action) {
        INewWizard wizard = createWizard();
        IStructuredSelection selection = getCurrentSelection();
        wizard.init(window.getWorkbench(), selection);
        WizardDialog dialog = new WizardDialog(window.getShell(), wizard);
        dialog.open();
    }

    protected IStructuredSelection getCurrentSelection() {
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                return (IStructuredSelection)selection;
            }
            IWorkbenchPart part = window.getPartService().getActivePart();
            if (part instanceof IEditorPart) {
                IEditorInput input = ((IEditorPart)part).getEditorInput();
                if (input instanceof IFileEditorInput) {
                    return new StructuredSelection(((IFileEditorInput)input).getFile());
                }
            }
        }
        return null;
    }

    protected String getIconBundleId() {
        return "org.faktorips.devtools.core.ui";
    }

    protected String getSvgIconName() {
        String className = this.getClass().getSimpleName();
        return className.replaceFirst("^Open", "").replaceFirst("Action$", "");
    }
}
