/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.actions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

/**
 * The ActionDelegate for the HtmlExport
 * 
 * @author dicker
 */
public class CreateHtmlExportAction extends ActionDelegate {

    private IStructuredSelection selection = StructuredSelection.EMPTY;

    @Override
    public void selectionChanged(IAction action, ISelection newSelection) {
        if (newSelection instanceof IStructuredSelection) {
            selection = (IStructuredSelection)newSelection;
        } else {
            selection = StructuredSelection.EMPTY;
        }
    }

    private IIpsProject getIpsProject() {
        if (selection.size() != 1) {
            return null;
        }
        if (selection.getFirstElement() instanceof PlatformObject) {
            IProject project = (IProject)((PlatformObject)selection.getFirstElement()).getAdapter(IProject.class);
            if (project == null) {
                return null;
            }
            IIpsModel ipsModel = IpsPlugin.getDefault().getIpsModel();
            return ipsModel.getIpsProject(project.getProject());
        }
        return null;
    }

    @Override
    public void run(IAction action) {
        if (selection.size() > 1) {
            MessageDialog.openInformation(getShell(), Messages.CreateHtmlExportAction_HtmlExport,
                    Messages.CreateHtmlExportAction_SelectJustOneProject);
            return;
        }
        IIpsProject ipsProject = getIpsProject();
        if (ipsProject == null) {
            MessageDialog.openInformation(getShell(), Messages.CreateHtmlExportAction_HtmlExport,
                    Messages.CreateHtmlExportAction_SelectOneProject);
            return;
        }

        String selected = getDestinationFolder();

        if (new File(selected).isDirectory()) {
            exportHtml(selected);
        }

    }

    private void exportHtml(String selected) {

        DocumentationContext context = new DocumentationContext();

        context.setPath(selected);
        context.setIpsProject(getIpsProject());
        context.setLayouter(new HtmlLayouter(".resource")); //$NON-NLS-1$

        context.addDocumentorScript(new StandardDocumentorScript());
        context
                .setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        try {
            new HtmlExportOperation(context).run(new NullProgressMonitor());
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private String getDestinationFolder() {
        DirectoryDialog fd = createFileSaveDialog();
        return fd.open();
    }

    private DirectoryDialog createFileSaveDialog() {
        DirectoryDialog fd = new DirectoryDialog(getShell());
        fd.setText(Messages.CreateHtmlExportAction_Export);
        fd.setFilterPath(getIpsProject().getProject().getLocation() + File.separator + "html"); //$NON-NLS-1$
        return fd;
    }

    protected Shell getShell() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    }

}
