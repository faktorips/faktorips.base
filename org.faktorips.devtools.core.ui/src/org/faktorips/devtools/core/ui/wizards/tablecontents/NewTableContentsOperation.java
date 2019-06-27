/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.tablecontents;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableRows;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.wizards.productdefinition.NewProductDefinitionOperation;

public class NewTableContentsOperation extends NewProductDefinitionOperation<NewTableContentsPMO> {

    protected NewTableContentsOperation(NewTableContentsPMO pmo) {
        super(pmo);
    }

    @Override
    protected void finishIpsSrcFile(IIpsSrcFile ipsSrcFile, IProgressMonitor monitor) throws CoreException {
        IIpsObject ipsObject = ipsSrcFile.getIpsObject();
        if (ipsObject instanceof ITableContents) {
            ITableContents table = (ITableContents)ipsObject;
            table.setTableStructure(getPmo().getSelectedStructure().getQualifiedName());
            ITableRows tabeleRows = table.newTableRows();
            ITableStructure structure = getPmo().getSelectedStructure();
            if (structure != null) {
                for (int i = 0; i < structure.getNumOfColumns(); i++) {
                    table.newColumn(StringUtils.EMPTY, structure.getColumn(i).getName());
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
                try {
                    addToIpsSrcFile.save(true, monitor);
                } catch (CoreException e) {
                    throw new CoreRuntimeException(e);
                }
            }
        }
    }

}
