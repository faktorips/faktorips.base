/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationFieldPainter;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationProposalAdapter;
import org.faktorips.devtools.core.ui.controller.fields.EnumerationProposalProvider;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.table.EditFieldCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;

/**
 * A factory to create controls and edit fields that allow to edit values for one or more value
 * datatypes.
 * 
 * @author Joerg Ortmann
 */
public abstract class ValueDatatypeControlFactory {

    /**
     * Returns <code>true</code> if this factory can create controls for the given datatype,
     * otherwise <code>false</code>.
     * 
     * @param datatype Datatype controls are needed for - might be <code>null</code>.
     */
    public abstract boolean isFactoryFor(ValueDatatype datatype);

    /**
     * Creates a control and edit field that allows to edit a value of one of the value datatypes
     * this is a factory for.
     * 
     * @param toolkit The toolkit used to create the control.
     * @param parent The parent composite to which the control is added.
     * @param datatype The value datatype a control should be created for.
     * @param valueSet An optional @Deprecated valueset. Future Implementations should use
     *            ValueSetOwner instead.
     * 
     */
    public abstract EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject);

    /**
     * In case a value set is defined ,enumeration support is added to the text control by adding an
     * {@link EnumerationProposalProvider}.
     * 
     * @param textControl the text control to add enumeration support to
     * @param valueSet the value set that provides the values
     * @param datatype the data type of the field. May also provide enumeration values if it is an
     *            enum data type.
     */
    protected void adaptEnumValueSetProposal(Text textControl, IValueSet valueSet, ValueDatatype datatype) {
        if (valueSet != null) {
            IValueSetOwner valueSetOwner = valueSet.getValueSetOwner();
            EnumerationFieldPainter.addPainterTo(textControl, datatype, valueSetOwner);
            IInputFormat<String> inputFormat = getInputFormat(datatype, valueSet);
            EnumerationProposalAdapter.createAndActivateOnAnyKey(textControl, datatype, valueSetOwner, inputFormat);
        }
    }

    protected IInputFormat<String> getInputFormat(ValueDatatype datatype, IValueSet valueSet) {
        IIpsProject ipsProject = null;
        if (valueSet != null) {
            ipsProject = valueSet.getIpsProject();
        }
        IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(datatype, ipsProject);
        if (isControlForDefaultValue(valueSet)) {
            inputFormat.setNullString(getNullStringRepresentation(valueSet));
        }
        return inputFormat;
    }

    /**
     * Creates a control that allows to edit a value of the value datatype this is a factory for.
     * 
     * @param toolkit The toolkit used to create the control.
     * @param parent The parent composite to which the control is added.
     * @param datatype The value datatype a control should be created for.
     * @param valueSet An optional @Deprecated valueset.Future Implementations should use
     *            ValueSetOwner instead.
     */
    public abstract Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject);

    /**
     * Creates a cell editor that allows to edit a value of the value datatype this is a factory
     * for.
     * 
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a cell editor that allows to edit a value of the value datatype this is a factory
     * for.
     * 
     * @param toolkit The ui toolkit to use for creating ui elements.
     * @param dataType The <code>ValueDatatype</code> to create a cell editor for.
     * @param valueSet An optional valueset.
     * @param tableViewer The viewer
     * @param columnIndex The index of the column.
     */
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(), ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    private IpsCellEditor createTextCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        EditField<String> editField = createEditField(toolkit, parent, dataType, valueSet, ipsProject);
        IpsCellEditor tableCellEditor = new EditFieldCellEditor(editField);
        return tableCellEditor;
    }

    public abstract int getDefaultAlignment();

    protected boolean isControlForDefaultValue(IValueSet valueSet) {
        return valueSet != null && valueSet.getValueSetOwner() instanceof IConfigElement;
    }

    protected String getNullStringRepresentation(IValueSet valueSet) {
        if (isControlForDefaultValue(valueSet)) {
            return Messages.DefaultValueRepresentation_EditField;
        } else {
            return StringUtils.EMPTY;
        }
    }

}
