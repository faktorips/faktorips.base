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

package org.faktorips.devtools.htmlexport.wizards;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.htmlexport.HtmlExportOperation;
import org.faktorips.devtools.htmlexport.HtmlExportPlugin;
import org.faktorips.devtools.htmlexport.documentor.DocumentationContext;
import org.faktorips.devtools.htmlexport.generators.html.HtmlLayouter;
import org.faktorips.devtools.htmlexport.standard.StandardDocumentorScript;

public class IpsProjectHtmlExportWizard extends Wizard implements IExportWizard {

    private static String DIALOG_SETTINGS_KEY = "org.faktorips.devtools.htmlexport.ipsProjectHtmlExportWizard"; //$NON-NLS-1$
    private IpsProjectHtmlExportWizardPage ipsProjectHtmlExportWizardPage;
    /**
     * Create a new IpsArExportWizard
     */
    private IStructuredSelection selection;
    private boolean hasNewDialogSettings;

    public IpsProjectHtmlExportWizard() {
        super();
        setWindowTitle(Messages.IpsProjectHtmlExportWizard_windowTitle);
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
        exportHtml();

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
        context.setDescriptionLocale(ipsProjectHtmlExportWizardPage.getSupportedLanguage());

        context.setIpsProject(ipsProjectHtmlExportWizardPage.getSelectedIpsProject());
        context.setLayouter(new HtmlLayouter(".resource")); //$NON-NLS-1$

        context.addDocumentorScript(new StandardDocumentorScript());
        context.setDocumentedIpsObjectTypes(context.getIpsProject().getIpsModel().getIpsObjectTypes());

        IWorkspaceRunnable op = new HtmlExportOperation(context);

        WorkbenchRunnableAdapter workbenchRunnableAdapter = new WorkbenchRunnableAdapter(op);
        try {
            getContainer().run(true, true, workbenchRunnableAdapter);
        } catch (InterruptedException e) {
            return false;
        } catch (Exception e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;

    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        ipsProjectHtmlExportWizardPage = new IpsProjectHtmlExportWizardPage(selection);
        addPage(ipsProjectHtmlExportWizardPage);
    }

}
