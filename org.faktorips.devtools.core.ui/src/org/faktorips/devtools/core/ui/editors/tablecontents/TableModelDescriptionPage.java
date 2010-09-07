/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablecontents;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructure;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.ui.views.modeldescription.DefaultModelDescriptionPage;
import org.faktorips.devtools.core.ui.views.modeldescription.DescriptionItem;

/**
 * A page for presenting information of a {@link TableStructure} similiar to the outline view.
 * 
 * @author Markus Blum
 */
public class TableModelDescriptionPage extends DefaultModelDescriptionPage {

    private ArrayList<DescriptionItem> columnsList;
    private String tableName;

    public TableModelDescriptionPage(ITableContents tableContents) throws CoreException {
        super();

        columnsList = new ArrayList<DescriptionItem>();
        tableName = tableContents.getName();

        ITableStructure tableStructure = tableContents.findTableStructure(tableContents.getIpsProject());

        String localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(tableStructure);
        DescriptionItem structureDescription = new DescriptionItem(
                Messages.TableModelDescriptionPage_generalInformation, localizedDescription);
        columnsList.add(structureDescription);

        IColumn[] columns = tableStructure.getColumns();

        /*
         * Default sort order is same as table definition.
         */
        for (IColumn column : columns) {
            localizedDescription = IpsPlugin.getMultiLanguageSupport().getLocalizedDescription(column);
            DescriptionItem item = new DescriptionItem(column.getName(), localizedDescription);
            columnsList.add(item);
        }

        DescriptionItem[] itemList = new DescriptionItem[columnsList.size()];
        itemList = columnsList.toArray(itemList);

        super.setTitle(tableName);
        super.setDescriptionItems(itemList);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
    }

    @Override
    public void dispose() {
        columnsList.clear();

        super.dispose();
    }

}
