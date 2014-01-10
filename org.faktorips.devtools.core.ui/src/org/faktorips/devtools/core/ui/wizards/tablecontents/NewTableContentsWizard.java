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

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.FolderAndPackagePage;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionPMO;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionWizard;

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
            try {
                getPmo().setSelectedStructure(tableContent.findTableStructure(tableContent.getIpsProject()));
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
    }

    public void setAddToTableUsage(ITableContentUsage tableUsage, boolean autosave) {
        getPmo().setAddToTableUsage(tableUsage, autosave);
    }

}
