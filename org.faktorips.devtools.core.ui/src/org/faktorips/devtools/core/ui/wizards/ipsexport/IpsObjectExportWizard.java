/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.ipsexport;

import java.util.Map;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.tableconversion.ITableFormat;

/**
 * Base class for wizards exporting <code>IIpsObject</code> types like <code>IEnumType</code>,
 * <code>IEnumContent</code> or <code>ITableContents</code>.
 * 
 * @author Roman Grutza
 */
public abstract class IpsObjectExportWizard extends Wizard implements IExportWizard {

    protected static String DIALOG_SETTINGS_KEY = "IpsObjectExportWizard"; //$NON-NLS-1$
    protected boolean hasNewDialogSettings;

    protected IStructuredSelection selection;
    protected Map<ITableFormat, TableFormatPropertiesPage> customPages;

    /**
     * Saves dialog settings, like size and position information.
     */
    protected void saveDialogSettings() {
        if (hasNewDialogSettings) {
            IDialogSettings workbenchSettings = IpsPlugin.getDefault().getDialogSettings();
            IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
            section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
            setDialogSettings(section);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
    }

    /**
     * Persists the current settings in the wizard's pages.
     */
    public abstract void saveWidgetSettings();

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        IpsObjectExportPage exportPage = (IpsObjectExportPage)getStartingPage();
        if (page == exportPage) {
            exportPage.validateObjectToExport();
            boolean isValid = exportPage.getErrorMessage() == null;

            for (WizardPage customPage : customPages.values()) {
                customPage.setPageComplete(isValid);
            }

            ITableFormat tableFormat = exportPage.getFormat();
            TableFormatPropertiesPage nextPage = customPages.get(tableFormat);
            return nextPage;
        }
        return null;
    }
}
