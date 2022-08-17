/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;

public class AddNewTableContentsHandler extends AbstractAddTableContentsHandler {

    public static final String COMMAND_ID = "org.faktorips.devtools.core.ui.wizards.tablecontents.newTableContents"; //$NON-NLS-1$

    public static final String PARAMETER_TABLE_USAGE = "org.faktorips.devtools.core.ui.wizards.tablecontents.newTableContents.tableUsage"; //$NON-NLS-1$

    /**
     * Opens the {@link NewTableContentsWizard} and set the defaults.
     * 
     * @param setToUsage The {@link ITableContentUsage} where the new table content should be set
     *            to.
     * @param shell The shell to open the wizard dialog
     * @param autoSave true for automatically safe the product component where the table was added
     *            to, false if the wizard dialog is opened from editor and you do not want to safe
     *            automatically
     */
    @Override
    public void openDialog(ITableContentUsage setToUsage, Shell shell, boolean autoSave) {
        NewTableContentsWizard newTableContentsWizard = new NewTableContentsWizard();
        newTableContentsWizard.initDefaults(setToUsage.getIpsSrcFile().getIpsPackageFragment(), null);
        newTableContentsWizard.setAddToTableUsage(setToUsage, autoSave);
        WizardDialog dialog = new WizardDialog(shell, newTableContentsWizard);
        dialog.open();
    }

    @Override
    protected String getTableUsageParameter() {
        return PARAMETER_TABLE_USAGE;
    }

}
