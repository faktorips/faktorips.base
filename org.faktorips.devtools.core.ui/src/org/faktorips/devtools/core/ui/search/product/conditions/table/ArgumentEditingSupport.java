/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.contentproposal.ContentProposals;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TextCellEditor;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * This class is the {@link EditingSupport} for the column of arguments in the table of search
 * conditions.
 * <p>
 * It chooses the control for editing of the argument depending on the {@link IConditionType} and
 * the {@link ValueDatatype} of the searched element.
 * 
 * 
 * @author dicker
 */
final class ArgumentEditingSupport extends EnhancedCellTrackingEditingSupport {

    public ArgumentEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(ProductSearchConditionPresentationModel element) {
        ProductSearchConditionPresentationModel model = element;

        if (model.getConditionType().isArgumentIpsObject()) {
            UIToolkit toolkit = new UIToolkit(null);

            Text textControl = toolkit.createText(((TableViewer)getViewer()).getTable());

            ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(textControl,
                    new TextContentAdapter(), new IpsObjectContentProposalProvider(model.getAllowedAttributeValues()),
                    ContentProposals.AUTO_COMPLETION_KEY_STROKE, new char[] { '.' });
            contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

            return new TextCellEditor(textControl);
        }

        if (model.getConditionType().hasValueSet()) {
            ValueDatatype datatype = model.getConditionType().getValueDatatype(model.getSearchedElement());

            return createValueDatatypeTableCellEditor(model, datatype);
        } else {

            UIToolkit toolkit = new UIToolkit(null);
            Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

            combo.setItems(model.getAllowedAttributeValues().toArray(
                    new String[model.getAllowedAttributeValues().size()]));

            return new ComboCellEditor(combo);
        }
    }

    private IpsCellEditor createValueDatatypeTableCellEditor(ProductSearchConditionPresentationModel model,
            ValueDatatype valueDatatype) {
        ValueDatatypeControlFactory controlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                valueDatatype);

        IValueSet valueSet = model.getConditionType().getValueSet(model.getSearchedElement());

        return controlFactory.createTableCellEditor(new UIToolkit(null), valueDatatype, valueSet,
                (TableViewer)getViewer(), 3, model.getSearchedElement().getIpsProject());
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
