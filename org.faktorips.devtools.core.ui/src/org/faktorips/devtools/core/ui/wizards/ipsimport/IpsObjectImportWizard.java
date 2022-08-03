/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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

    private IStructuredSelection selection;
    private boolean hasNewDialogSettings;

    // data that needs to be accessible across multiple wizard pages
    private boolean importIntoExisting;
    private String nullRepresentation;

    private SelectFileAndImportMethodPage startingPage;

    public IpsObjectImportWizard() {
        IDialogSettings workbenchSettings = IpsUIPlugin.getDefault().getDialogSettings();
        IDialogSettings settings = workbenchSettings.getSection(getDialogSettingsKey());
        setHasNewDialogSettings((settings == null));
        setDialogSettings(settings);
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        setSelection(selection);
    }

    /**
     * Sets if the table content will be imported into the existing table content (<code>true</code>
     * ), or not (<code>false</code>)
     */
    public void setImportIntoExisting(boolean importIntoExisting) {
        this.importIntoExisting = importIntoExisting;
    }

    protected boolean isImportIntoExisting() {
        return importIntoExisting;
    }

    public boolean isExcelTableFormatSelected() {
        return ".xls".equals(getIpsOIWStartingPage().getFormat().getDefaultExtension()); //$NON-NLS-1$
    }

    /**
     * This method synchronizes the model with the current state of the GUI widgets.
     */
    protected void saveDataToWizard() {
        nullRepresentation = getIpsOIWStartingPage().getNullRepresentation();
    }

    protected String getNullRepresentation() {
        return nullRepresentation;
    }

    protected abstract String getDialogSettingsKey();

    public IStructuredSelection getSelection() {
        return selection;
    }

    public void setSelection(IStructuredSelection selection) {
        this.selection = selection;
    }

    public SelectFileAndImportMethodPage getIpsOIWStartingPage() {
        return startingPage;
    }

    public void setIpsOIWStartingPage(SelectFileAndImportMethodPage startingPage) {
        this.startingPage = startingPage;
    }

    public boolean isHasNewDialogSettings() {
        return hasNewDialogSettings;
    }

    public void setHasNewDialogSettings(boolean hasNewDialogSettings) {
        this.hasNewDialogSettings = hasNewDialogSettings;
    }

}
