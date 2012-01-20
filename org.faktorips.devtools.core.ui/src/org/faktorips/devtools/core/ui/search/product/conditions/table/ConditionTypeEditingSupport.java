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

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * This is the {@link EditingSupport} for the columns of conditions. It provides a {@link Combo} for
 * choosing the {@link IConditionType}.
 * 
 * 
 * @author dicker
 */
class ConditionTypeEditingSupport extends EnhancedCellTrackingEditingSupport {

    public ConditionTypeEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        UIToolkit toolkit = new UIToolkit(null);
        Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<IConditionType> conditionTypes = model.getConditionsWithSearchableElements();
        List<String> conditionTypeNames = new ArrayList<String>(conditionTypes.size());

        for (IConditionType conditionType : conditionTypes) {
            conditionTypeNames.add(conditionType.getName());
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

    private IConditionType getCondition(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        return model.getConditionType();
    }

    @Override
    protected void setValue(Object element, Object value) {
        if (value == null) {
            return;
        }

        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<IConditionType> conditionTypes = model.getConditionsWithSearchableElements();
        for (IConditionType conditionType : conditionTypes) {
            if (conditionType.getName().equals(value)) {
                model.setCondition(conditionType);
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