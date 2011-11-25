/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

final class ArgumentEditingSupport extends EnhancedCellTrackingEditingSupport {

    public ArgumentEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        if (model.getCondition().hasValueSet()) {
            ValueDatatype datatype = model.getCondition().getValueDatatype(model.getSearchedElement());

            return createValueDatatypeTableCellEditor(model, datatype);
        } else {

            UIToolkit toolkit = new UIToolkit(null);
            Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

            combo.setItems(model.getAllowedAttributeValues().toArray(new String[0]));

            return new ComboCellEditor(combo);
        }
    }

    private IpsCellEditor createValueDatatypeTableCellEditor(ProductSearchConditionPresentationModel model,
            ValueDatatype valueDatatype) {
        IpsCellEditor tableCellEditor;

        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                valueDatatype);

        IValueSet valueSet = model.getCondition().getValueSet(model.getSearchedElement());

        tableCellEditor = controlFactory.createTableCellEditor(new UIToolkit(null), valueDatatype, valueSet,
                (TableViewer)getViewer(), 3, model.getSearchedElement().getIpsProject());
        return tableCellEditor;
    }

    @Override
    public boolean canEdit(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.isSearchedElementChosen();

    }

    @Override
    protected Object getValue(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.getArgument();
    }

    @Override
    protected void setValue(Object element, Object value) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        model.setArgument((String)value);
        getViewer().refresh();
    }

    @Override
    public int getColumnIndex() {
        return 3;
    }
}