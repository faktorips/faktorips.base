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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ICondition;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

class ConditionTypeEditingSupport extends EnhancedCellTrackingEditingSupport {

    public ConditionTypeEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        UIToolkit toolkit = new UIToolkit(null);
        Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<ICondition> conditionTypes = model.getConditionsWithSearchableElements();
        List<String> conditionTypeNames = new ArrayList<String>(conditionTypes.size());

        for (ICondition condition : conditionTypes) {
            conditionTypeNames.add(condition.getName());
        }

        combo.setItems(conditionTypeNames.toArray(new String[0]));

        return new ComboCellEditor(combo);
    }

    @Override
    public boolean canEdit(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;
        return model.getConditionsWithSearchableElements().size() > 1;
    }

    @Override
    protected Object getValue(Object element) {
        return getCondition(element).getName();
    }

    private ICondition getCondition(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.getCondition();
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (value == null) {
            return;
        }

        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<ICondition> conditionTypes = model.getConditionsWithSearchableElements();
        for (ICondition condition : conditionTypes) {
            if (condition.getName().equals(value)) {
                model.setCondition(condition);
                getViewer().refresh();
                return;
            }
        }
    }

    @Override
    public int getColumnIndex() {
        return 0;
    }
}