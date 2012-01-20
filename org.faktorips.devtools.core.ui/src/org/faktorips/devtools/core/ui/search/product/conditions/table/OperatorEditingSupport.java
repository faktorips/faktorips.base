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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * This is the {@link EditingSupport} for the columns of operators. It provides a {@link Combo} for
 * choosing the {@link ISearchOperatorType} for the condition.
 * 
 * @author dicker
 */
final class OperatorEditingSupport extends EnhancedCellTrackingEditingSupport {

    public OperatorEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<? extends ISearchOperatorType> operatorTypes = model.getConditionType().getSearchOperatorTypes(
                model.getSearchedElement());

        List<String> operatorTypesLabels = new ArrayList<String>();

        for (ISearchOperatorType searchOperatorType : operatorTypes) {
            operatorTypesLabels.add(searchOperatorType.getLabel());
        }

        UIToolkit toolkit = new UIToolkit(null);
        Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

        combo.setItems(operatorTypesLabels.toArray(new String[0]));

        return new ComboCellEditor(combo);
    }

    @Override
    public boolean canEdit(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.isSearchedElementChosen();
    }

    @Override
    protected Object getValue(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.getOperatorType() == null ? StringUtils.EMPTY : model.getOperatorType().getLabel();
    }

    @Override
    protected void setValue(Object element, Object value) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<? extends ISearchOperatorType> searchOperatorTypes = model.getSearchOperatorTypes();

        for (ISearchOperatorType searchOperatorType : searchOperatorTypes) {
            if (searchOperatorType.getLabel().equals(value)) {
                model.setOperatorType(searchOperatorType);
                return;
            }
        }

        try {
            throw new RuntimeException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.setOperatorType(null);

        getViewer().refresh();
    }

    @Override
    public int getColumnIndex() {
        return 2;
    }
}