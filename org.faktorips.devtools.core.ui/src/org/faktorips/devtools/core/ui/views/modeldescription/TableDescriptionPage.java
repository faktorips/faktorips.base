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

package org.faktorips.devtools.core.ui.views.modeldescription;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.editors.tablecontents.Messages;

/**
 * A page for presenting the properties of a {@link ITableStructure} or {@link ITableContents}. This
 * page is connected to a Editor similar to the outline view.
 * 
 * @author Quirin Stoll
 */
public class TableDescriptionPage extends DefaultModelDescriptionPage {

    public TableDescriptionPage(ITableStructure tableStructure) {
        super();
        setIpsObject(tableStructure);
        setDescriptionData();
    }

    @Override
    protected List<DescriptionItem> createDescriptions() throws CoreException {
        List<DescriptionItem> descriptions = new ArrayList<DescriptionItem>();
        if (getIpsObject() != null) {
            String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(getIpsObject());
            DescriptionItem structureDescription = new DescriptionItem(
                    Messages.TableModelDescriptionPage_generalInformation, localizedDescription);
            descriptions.add(structureDescription);
            IColumn[] columns = getIpsObject().getColumns();
            for (IColumn column : columns) {
                createDescriptionItem(column, descriptions);
            }
        }
        return descriptions;
    }

    @Override
    public ITableStructure getIpsObject() {
        return (ITableStructure)super.getIpsObject();

    }
}
