/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.ui.messagesimport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.WorkbenchRunnableAdapter;
import org.faktorips.devtools.stdbuilder.policycmpttype.validationrule.ValidationRuleMessagesPropertiesImporter;

/**
 * Wizard for importing translated validation rule messages.
 * 
 * TODO This implementation is not completed yet. Have a look at FIPS-626
 * 
 * @author dirmeier
 */
public class MessagesImportWizard extends Wizard implements IImportWizard {

    protected final static String DIALOG_SETTINGS_KEY = "MessagesImportWizard"; //$NON-NLS-1$
    private IStructuredSelection selection;
    private MessagesImportPage page;

    public MessagesImportWizard() {
        IDialogSettings workbenchSettings = IpsUIPlugin.getDefault().getDialogSettings();
        IDialogSettings settingSection = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (settingSection != null) {
            setDialogSettings(settingSection);
        }
        setWindowTitle(Messages.MessagesImportWizard_windowTitle);
        // TODO
        // setDefaultPageImageDescriptor(StdBuilderUIPlugin.getImageHandling().createImageDescriptor(
        //                "")); //$NON-NLS-1$
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
        File file = new File(pmo.getFileName());
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ValidationRuleMessagesPropertiesImporter importer = new ValidationRuleMessagesPropertiesImporter(
                    fileInputStream, pmo.getIpsPackageFragmentRoot(), pmo.getLocale().getLocale());
            getContainer().run(true, false, new WorkbenchRunnableAdapter(importer));
            IStatus importStatus = importer.getResultStatus();
            if (!importStatus.isOK()) {
                IpsPlugin.logAndShowErrorDialog(importStatus);
            }
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

}
