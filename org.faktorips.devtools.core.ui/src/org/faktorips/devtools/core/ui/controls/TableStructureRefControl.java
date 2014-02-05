/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.UIToolkit;

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
     * @throws CoreException if an exception occurs while searching for the table structure.
     */
    public ITableStructure findTableStructure() throws CoreException {
        if (getIpsProject() == null) {
            return null;
        }
        return (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, getText());
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        return getIpsProject().findIpsSrcFiles(IpsObjectType.TABLE_STRUCTURE);
    }
}
