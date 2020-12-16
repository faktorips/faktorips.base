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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A control to edit table content references.
 */
public class TableContentsRefControl extends IpsObjectRefControl {

    public TableContentsRefControl(IIpsProject project, Composite parent, UIToolkit toolkit) {
        this(Arrays.asList(project), parent, toolkit);
    }

    public TableContentsRefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit) {
        super(projects, parent, toolkit, Messages.TableContentsRefControl_title,
                Messages.TableContentsRefControl_description);
    }

    /**
     * Returns the table contents entered in this control. Returns <code>null</code> if the text in
     * the control does not identify a table contents.
     * 
     * @throws CoreException if an exception occurs while searching for the table contents.
     */
    public ITableContents findTableContents() throws CoreException {
        return (ITableContents)findIpsObject(IpsObjectType.TABLE_CONTENTS);
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        return findIpsSrcFilesByType(IpsObjectType.TABLE_CONTENTS);
    }
}
