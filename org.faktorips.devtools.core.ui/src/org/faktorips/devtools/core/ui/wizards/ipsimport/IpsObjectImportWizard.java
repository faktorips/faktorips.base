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

    @Override
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
        return startingPage.getFormat().getDefaultExtension().equals(".xls"); //$NON-NLS-1$
    }

    /**
     * This method synchronizes the model with the current state of the GUI widgets.
     */
    protected void saveDataToWizard() {
        nullRepresentation = startingPage.getNullRepresentation();
    }

}
