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
        IpsPlugin.getDefault().getIpsModel().addChangeListener(this);
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
