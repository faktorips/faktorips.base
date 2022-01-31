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

import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.FolderAndPackagePage;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionWizard;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

public class NewTableContentsWizard extends NewProductDefinitionWizard {

    public static final String ID = "newTableContentsWizard"; //$NON-NLS-1$

    public NewTableContentsWizard() {
        super(new NewTableContentsPMO());
        setWindowTitle(Messages.NewTableContentsWizard_title);
        setDefaultPageImageDescriptor(IpsUIPlugin.getImageHandling().createImageDescriptor(
                "wizards/NewTableContentsWizard.png")); //$NON-NLS-1$
    }

    @Override
    protected NewTableContentsPMO getPmo() {
        return (NewTableContentsPMO)super.getPmo();
    }

    @Override
    protected NewProductDefinitionOperation<? extends NewProductDefinitionPMO> getOperation() {
        return new NewTableContentsOperation(getPmo());
    }

    @Override
    public void addPages() {
        addPage(new TableContentsPage(getPmo()));
        addPage(new FolderAndPackagePage(getPmo()));
    }

    @Override
    protected String getDialogId() {
        return ID;
    }

    @Override
    protected void initDefaults(IIpsPackageFragment selectedPackage, IIpsObject selectedIpsObject) {
        getPmo().setIpsProject(selectedPackage.getIpsProject());
        getPmo().setPackageRoot(selectedPackage.getRoot());
        getPmo().setIpsPackage(selectedPackage);
        if (selectedIpsObject instanceof ITableStructure) {
            getPmo().setSelectedStructure((ITableStructure)selectedIpsObject);
        } else if (selectedIpsObject instanceof ITableContents) {
            ITableContents tableContent = (ITableContents)selectedIpsObject;
            getPmo().setSelectedStructure(tableContent.findTableStructure(tableContent.getIpsProject()));
        }
    }

    public void setAddToTableUsage(ITableContentUsage tableUsage, boolean autosave) {
        getPmo().setAddToTableUsage(tableUsage, autosave);
    }

}
