/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * A control to edit table structure references.
 */
public class TableStructureRefControl extends IpsObjectRefControl {

    public TableStructureRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.TableStructureRefControl_title,
                Messages.TableStructureRefControl_description);
    }

    /**
     * Returns the table structure entered in this control. Returns <code>null</code> if the text in
     * the control does not identify a table structure.
     * 
     * @throws CoreRuntimeException if an exception occurs while searching for the table structure.
     */
    public ITableStructure findTableStructure() throws CoreRuntimeException {
        return (ITableStructure)findIpsObject(IpsObjectType.TABLE_STRUCTURE);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreRuntimeException {
        return findIpsSrcFilesByType(IpsObjectType.TABLE_STRUCTURE);
    }
}
