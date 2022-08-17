/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.messagesimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleCsvImporter;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleMessagesImportOperation;
import org.faktorips.devtools.core.internal.model.pctype.validationrule.ValidationRuleMessagesPropertiesImporter;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;

/**
 * Wizard for importing translated validation rule messages.
 * 
 */
public class MessagesImportWizard extends Wizard implements IImportWizard {

    protected static final String DIALOG_SETTINGS_KEY = "MessagesImportWizard"; //$NON-NLS-1$
    private IStructuredSelection selection;
    private MessagesImportPage page;

    public MessagesImportWizard() {
        IDialogSettings workbenchSettings = IpsUIPlugin.getDefault().getDialogSettings();
        IDialogSettings settingSection = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (settingSection != null) {
            setDialogSettings(settingSection);
        }
        setWindowTitle(Messages.MessagesImportWizard_windowTitle);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    @Override
    public void addPages() {
        super.addPages();
        page = new MessagesImportPage(Messages.MessagesImportWizard_pageName, selection);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        MessagesImportPMO pmo = page.getMessagesImportPMO();
        File file = new File(pmo.getFilename());
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            ValidationRuleMessagesImportOperation importer = getImporter(pmo, fileInputStream);
            getContainer().run(true, false, new WorkbenchRunnableAdapter(importer));
            IStatus importStatus = importer.getResultStatus();
            if (!importStatus.isOK()) {
                createImportResultDialog(importStatus);
            }
        } catch (InvocationTargetException | InterruptedException | FileNotFoundException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                }
            }
        }
        return true;
    }

    private void createImportResultDialog(final IStatus importStatus) {
        IpsPlugin.getDefault().getLog().log(importStatus);
        Display display = Display.getCurrent() != null ? Display.getCurrent() : Display.getDefault();
        display.asyncExec(() -> ErrorDialog.openError(Display.getDefault().getActiveShell(),
                Messages.MessagesImportWizard_windowTitle,
                null, importStatus));
    }

    private ValidationRuleMessagesImportOperation getImporter(MessagesImportPMO pmo, FileInputStream fileInputStream) {
        ValidationRuleMessagesImportOperation importer;
        if (pmo.isCsvFileFormat()) {
            importer = getCsvImporter(pmo, fileInputStream);
        } else {
            importer = getPropertiesImporter(pmo, fileInputStream);
        }
        importer.setEnableWarningsForMissingMessages(pmo.isEnableWarningsForMissingMessages());
        return importer;
    }

    private ValidationRuleCsvImporter getCsvImporter(MessagesImportPMO pmo, FileInputStream fileInputStream) {
        ValidationRuleCsvImporter csvImporter = new ValidationRuleCsvImporter(fileInputStream,
                pmo.getIpsPackageFragmentRoot(), pmo.getSupportedLanguage().getLocale());
        csvImporter.setDelimiter(pmo.getColumnDelimiter());
        csvImporter.setKeyAndValueColumn(Integer.parseInt(pmo.getIdentifierColumnIndex()) - 1,
                Integer.parseInt(pmo.getTextColumnIndex()) - 1);
        csvImporter.setMethodOfIdentification(pmo.getRuleIdentifier());
        return csvImporter;
    }

    private ValidationRuleMessagesPropertiesImporter getPropertiesImporter(MessagesImportPMO pmo,
            FileInputStream fileInputStream) {
        ValidationRuleMessagesPropertiesImporter propertiesImporter = new ValidationRuleMessagesPropertiesImporter(
                fileInputStream, pmo.getIpsPackageFragmentRoot(), pmo.getSupportedLanguage().getLocale());
        propertiesImporter.setMethodOfIdentification(pmo.getRuleIdentifier());
        return propertiesImporter;
    }

}
