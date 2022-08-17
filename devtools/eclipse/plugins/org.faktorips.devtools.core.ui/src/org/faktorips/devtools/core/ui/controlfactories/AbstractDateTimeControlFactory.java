/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.DateControlField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controls.AbstractDateTimeControl;
import org.faktorips.devtools.core.ui.table.EditFieldCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * A factory for edit fields/controls for date/time datatypes.
 * 
 * @since 3.7
 */
public abstract class AbstractDateTimeControlFactory extends ValueDatatypeControlFactory {

    public AbstractDateTimeControlFactory() {
        super();
    }

    @Override
    public EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        AbstractDateTimeControl dateControl = createDateTimeControl(parent, toolkit);
        adaptEnumValueProposal(toolkit, dateControl.getTextControl(), valueSet, datatype, ipsProject);
        return new DateControlField<>(dateControl, getInputFormat(datatype,
                valueSet, ipsProject));
    }

    protected abstract AbstractDateTimeControl createDateTimeControl(Composite parent, UIToolkit toolkit);

    protected Button createButton(UIToolkit toolkit, Composite calendarComposite) {
        GridData buttonGridData = new GridData(SWT.FILL, SWT.FILL, false, false);
        Button button = toolkit.createButton(calendarComposite, ""); //$NON-NLS-1$
        button.setLayoutData(buttonGridData);
        button.setImage(IpsUIPlugin.getImageHandling().getSharedImage("Calendar.png", true)); //$NON-NLS-1$
        return button;
    }

    /**
     * @deprecated use {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *                 instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {
        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
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

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(),
                ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    /**
     * @param toolkit The ui toolkit to use for creating ui elements.
     * @param dataType The <code>ValueDatatype</code> to create a cell editor for.
     * @param valueSet An optional valueset.
     * @param ipsProject The ipsProject where the editor belongs to.
     */
    private IpsCellEditor createTextCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        Text text = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        EditField<String> editField = new FormattingTextField<>(text, getInputFormat(dataType, valueSet,
                ipsProject));
        return new EditFieldCellEditor(editField);
    }

    @Override
    public int getDefaultAlignment() {
        return SWT.RIGHT;
    }

}
