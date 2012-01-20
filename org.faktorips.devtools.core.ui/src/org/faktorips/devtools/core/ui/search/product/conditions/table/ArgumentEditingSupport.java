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

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

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
    protected IpsCellEditor getCellEditorInternal(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        if (model.getConditionType().isArgumentIpsObject()) {
            UIToolkit toolkit = new UIToolkit(null);

            Text textControl = toolkit.createText(((TableViewer)getViewer()).getTable());

            KeyStroke keyStroke = null;
            try {
                keyStroke = KeyStroke.getInstance("Ctrl+Space"); //$NON-NLS-1$
            } catch (final ParseException e) {
                throw new IllegalArgumentException("KeyStroke \"Ctrl+Space\" could not be parsed.", e); //$NON-NLS-1$
            }

            ContentProposalAdapter contentProposalAdapter = new ContentProposalAdapter(textControl,
                    new TextContentAdapter(), new IpsObjectContentProposalProvider(model.getAllowedAttributeValues()),
                    keyStroke, new char[] { '.' });
            contentProposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

            return new TextCellEditor(textControl);
        }

        if (model.getConditionType().hasValueSet()) {
            ValueDatatype datatype = model.getConditionType().getValueDatatype(model.getSearchedElement());

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

        IValueSet valueSet = model.getConditionType().getValueSet(model.getSearchedElement());

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