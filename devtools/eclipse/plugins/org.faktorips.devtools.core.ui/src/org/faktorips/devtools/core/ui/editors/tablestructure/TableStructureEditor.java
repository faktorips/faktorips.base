/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.views.modeldescription.IModelDescriptionSupport;
import org.faktorips.devtools.core.ui.views.modeldescription.TableDescriptionPage;
import org.faktorips.devtools.model.tablestructure.ITableStructure;

/**
 * Editor to edit table structures.
 */
public class TableStructureEditor extends IpsObjectEditor implements IModelDescriptionSupport {

    protected ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObject();
    }

    @Override
    protected void addPagesForParsableSrcFile() throws PartInitException {
        addPage(new TableStructureEditorStructurePage(this));
    }

    @Override
    protected String getUniformPageTitle() {
        return Messages.TableStructureEditor_title + getIpsObject().getName();
    }

    @Override
    public IPage createModelDescriptionPage() {
        return new TableDescriptionPage(getTableStructure());
    }
}
