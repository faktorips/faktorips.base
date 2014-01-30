/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.MoneyField;
import org.faktorips.devtools.core.ui.table.EditFieldCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

public class MoneyControlFactory extends ValueDatatypeControlFactory {

    public MoneyControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.MONEY.equals(datatype);
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text control = createControl(toolkit, parent, datatype, valueSet, ipsProject);
        return new MoneyField(control, ipsProject.getReadOnlyProperties().getDefaultCurrency());
    }

    @Override
    public Text createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text control = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        return control;
    }

    private IpsCellEditor createMoneyCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        IpsCellEditor tableCellEditor = new EditFieldCellEditor(createEditField(toolkit, parent, datatype, valueSet,
                ipsProject));
        return tableCellEditor;
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype datatype,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        return createTableCellEditor(toolkit, datatype, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link TableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createMoneyCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(),
                ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }

}
