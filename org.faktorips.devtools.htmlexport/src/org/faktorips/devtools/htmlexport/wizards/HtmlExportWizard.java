/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.htmlexport.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.HtmlExportPlugin;
import org.faktorips.devtools.htmlexport.context.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

public class HtmlExportWizard extends Wizard implements IExportWizard {

    private static String DIALOG_SETTINGS_KEY = "org.faktorips.devtools.htmlexport.ipsProjectHtmlExportWizard"; //$NON-NLS-1$
    private HtmlExportWizardPage ipsProjectHtmlExportWizardPage;
    /**
     * Create a new IpsArExportWizard
     */
    private IStructuredSelection selection;
    private boolean hasNewDialogSettings;

    public HtmlExportWizard() {
        super();
        setWindowTitle(Messages.HtmlExportWizard_windowTitle);
        setDefaultPageImageDescriptor(HtmlExportPlugin.getImageDescriptor("icons/HtmlExportWizard.png")); //$NON-NLS-1$

        IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);

        if (section == null) {
            hasNewDialogSettings = true;
        } else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    @Override
    public boolean performFinish() {
        if (!exportHtml()) {
            return false;
        }

        // save the dialog settings
        if (hasNewDialogSettings) {
            IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
            IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
            section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
            setDialogSettings(section);
        }

        ipsProjectHtmlExportWizardPage.saveWidgetValues();

        return true;
    }

    private boolean exportHtml() {
        DocumentationContext context = new DocumentationContext();

        context.setPath(ipsProjectHtmlExportWizardPage.getDestinationDirectory());
        context.setShowValidationErrors(ipsProjectHtmlExportWizardPage.getShowValidationErrors());
        context.setShowInheritedObjectPartsInTable(ipsProjectHtmlExportWizardPage.getShowObjectPartsInTableCheckBox());
        context.setDocumentationLocale(ipsProjectHtmlExportWizardPage.getSupportedLanguage());

        context.setIpsProject(ipsProjectHtmlExportWizardPage.getSelectedIpsProject());
        context.setLayouter(new HtmlLayouter(context, ".resource")); //$NON-NLS-1$

        context.addDocumentorScript(new StandardDocumentorScript());
        context.setDocumentedIpsObjectTypes(ipsProjectHtmlExportWizardPage.getSelectedIpsObjectTypes());

        IWorkspaceRunnable op = new HtmlExportOperation(context);

        WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(op);
        try {
            getContainer().run(true, true, workbenchRunnableAdapter);
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus(e));
            return false;
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(new IpsStatus(e.getTargetException()));
            return false;
        }

        IStatus exportStatus = context.getExportStatus();

        switch (exportStatus.getSeverity()) {
            case IStatus.ERROR:
                ErrorDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.HtmlExportWizard_errorHtmlExport,
                        Messages.HtmlExportWizard_messageErrorHtmlExport, exportStatus);
                return false;

            case IStatus.WARNING:
                ErrorDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.HtmlExportWizard_warningHtmlExport,
                        Messages.HtmlExportWizard_messageWarningHtmlExport, exportStatus);

                return true;

            default:
                return true;
        }
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        ipsProjectHtmlExportWizardPage = new HtmlExportWizardPage(selection);
        addPage(ipsProjectHtmlExportWizardPage);
    }

}
