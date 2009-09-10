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

package org.faktorips.devtools.core.ui.wizards.ipsimport;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

/**
 * Base class for wizards to import tabular data.
 * 
 * @author Roman Grutza
 */
public abstract class IpsObjectImportWizard extends Wizard implements IImportWizard {

    protected final static String DIALOG_SETTINGS_KEY = "IpsObjectImportWizard"; //$NON-NLS-1$

    protected IStructuredSelection selection;
    protected boolean hasNewDialogSettings;

    // data that needs to be accessible across multiple wizard pages
    protected boolean importIntoExisting;
    protected String nullRepresentation;

    protected SelectFileAndImportMethodPage startingPage;

    public IpsObjectImportWizard() {
        IDialogSettings workbenchSettings = IpsUIPlugin.getDefault().getDialogSettings();
        IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
        if (section == null) {
            hasNewDialogSettings = true;
        } else {
            hasNewDialogSettings = false;
            setDialogSettings(section);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * Sets if the table content will be imported into the existing table content (<code>true</code>
     * ), or not (<code>false</code>)
     */
    public void setImportIntoExisting(boolean importIntoExisting) {
        this.importIntoExisting = importIntoExisting;
    }

    public boolean isExcelTableFormatSelected() {
        return startingPage.getFormat().getDefaultExtension().equals(".xls");
    }

    protected void saveDataToWizard() {
        nullRepresentation = startingPage.getNullRepresentation();
    }

}