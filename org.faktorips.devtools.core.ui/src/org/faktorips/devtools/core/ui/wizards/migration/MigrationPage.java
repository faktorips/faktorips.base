/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.migration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * @author Joerg Ortmann
 */
public class MigrationPage extends WizardPage {

    private ProjectSelectionPage projectSelectionPage;
    private Composite overview;
    private Text description;

    public MigrationPage(ProjectSelectionPage projectSelectionPage) {
        super(Messages.MigrationPage_titleMigrationOperations);
        this.projectSelectionPage = projectSelectionPage;
        setMessage(Messages.MigrationPage_msgShortDescription);
        setPageComplete(false);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        IIpsProject[] projects = projectSelectionPage.getProjects();
        StringBuilder desc = new StringBuilder();
        setPageComplete(true);
        for (IIpsProject project : projects) {
            desc.append(Messages.MigrationPage_titleProject).append(project.getName())
                    .append(":").append(System.lineSeparator()).append(System.lineSeparator()); //$NON-NLS-1$
            try {
                desc.append(IpsPlugin.getDefault().getMigrationOperation(project).getDescription());
                desc.append(System.lineSeparator());
                desc.append(System.lineSeparator());
            } catch (CoreException e) {
                IpsPlugin.log(e);
                desc.append(Messages.MigrationPage_labelError + e.getMessage());
                desc.append(System.lineSeparator());
                desc.append(System.lineSeparator());
                setPageComplete(false);
            }
        }
        description.setText(desc.toString());
    }

    @Override
    public void createControl(Composite parent) {
        overview = new Composite(parent, SWT.NONE);
        overview.setLayout(new GridLayout(1, true));
        Label title = new Label(overview, SWT.NONE);
        title.setText(Messages.MigrationPage_labelHeader);
        description = new Text(overview, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        description.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        description.setEditable(false);

        super.setControl(overview);
    }

}
