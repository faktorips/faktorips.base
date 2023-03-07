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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.internal.IpsStringUtils;

public class NewTableContentsOperation extends NewProductDefinitionOperation<NewTableContentsPMO> {

    protected NewTableContentsOperation(NewTableContentsPMO pmo) {
        super(pmo);
    }

    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof ITableContents table) {
            table.setTableStructure(getPmo().getSelectedStructure().getQualifiedName());
            ITableRows tabeleRows = table.newTableRows();
            ITableStructure structure = getPmo().getSelectedStructure();
            if (structure != null) {
                for (int i = 0; i < structure.getNumOfColumns(); i++) {
                    table.newColumn(IpsStringUtils.EMPTY, structure.getColumn(i).getName());
                }
            }
            if (getPmo().isOpenEditor()) {
                tabeleRows.newRow();
            }
        } else {
            throw new RuntimeException("Invalid object type created"); //$NON-NLS-1$
        }
    }

    @Override
    protected void postProcess(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) {
        if (getPmo().getAddToTableUsage() != null
                && IpsUIPlugin.isEditable(getPmo().getAddToTableUsage().getPropertyValueContainer().getIpsSrcFile())) {
            IIpsSrcFile addToIpsSrcFile = getPmo().getAddToTableUsage().getIpsSrcFile();
            boolean dirty = addToIpsSrcFile.isDirty();
            ITableContentUsage tableContentUsage = getPmo().getAddToTableUsage();
            tableContentUsage.setTableContentName(ipsSrcFile.getQualifiedNameType().getName());
            if (!dirty && getPmo().isAutoSaveAddToFile()) {
                addToIpsSrcFile.save(monitor);
            }
        }
    }

}
