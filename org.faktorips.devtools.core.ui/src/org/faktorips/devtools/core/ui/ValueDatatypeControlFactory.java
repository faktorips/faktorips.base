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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalAdapter;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalProvider;
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
     * @param toolkit TODO
     * @param textControl the text control to add enumeration support to
     * @param valueSet the value set that provides the values
     * @param datatype the data type of the field. May also provide enumeration values if it is an
     *            enum data type.
     */
    protected void adaptEnumValueSetProposal(UIToolkit toolkit,
            Text textControl,
            IValueSet valueSet,
            ValueDatatype datatype) {
        if (valueSet != null && valueSet.isEnum()) {
            IValueSetOwner valueSetOwner = valueSet.getValueSetOwner();
            IInputFormat<String> inputFormat = getInputFormat(datatype, valueSet);
            Button button = createArrowDownButton(toolkit, textControl.getParent());
            EnumerationProposalAdapter.createAndActivateOnAnyKey(textControl, button, datatype, valueSetOwner,
                    inputFormat);
        }
    }

    /**
     * Creates a composite with the text control inside. This allows adding a button later on in
     * case of an enum datatype or enum value set.
     * <p>
     * Layout is optimized for Win7. Sorry linux users. :-/
     * 
     * @see ValueDatatypeControlFactory#createArrowDownButton(UIToolkit, Composite)
     */
    protected Text createPotentialEnumTextControl(UIToolkit toolkit, Composite parent, int textStyle) {
        Composite composite = toolkit.createGridComposite(parent, 2, false, false);
        GridLayout gridLayout = (GridLayout)composite.getLayout();
        /*
         * Allow placement of the potential button directly next to the text control. Makes it look
         * more like a combo.
         */
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 1;
        // avoid line of pixels below and above button. It should fit in perfectly.
        gridLayout.marginHeight = 2;

        Text text = toolkit.createTextAppendStyle(composite, textStyle);
        toolkit.paintBorderFor(text);

        return text;
    }

    /**
     * Creates a composite with the text control inside. This allows adding a button later on in
     * case of an enum datatype or enum value set.
     * <p>
     * Layout is optimized for Win7. Sorry linux users. :-/
     * 
     * @see ValueDatatypeControlFactory#createArrowDownButton(UIToolkit, Composite)
     */
    protected Text createPotentialEnumTextControl(UIToolkit toolkit, Composite parent) {
        return createPotentialEnumTextControl(toolkit, parent, SWT.NONE);
    }

    /**
     * Creates an button with an arrow down image that opens the context proposal for enum values.
     * 
     */
    protected Button createArrowDownButton(UIToolkit toolkit, Composite parent) {
        Button button = toolkit.createButton(parent, null);
        GridData buttonData = new GridData();
        buttonData.heightHint = 28;
        button.setLayoutData(buttonData);

        Image arrowDown = IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown_grey.gif", true); //$NON-NLS-1$
        button.setImage(arrowDown);

        return button;
    }

    protected IInputFormat<String> getInputFormat(ValueDatatype datatype, IValueSet valueSet) {
        IIpsProject ipsProject = null;
        if (valueSet != null) {
            ipsProject = valueSet.getIpsProject();
        }
        IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(datatype, ipsProject);
        setNewNullString(valueSet, inputFormat);
        return inputFormat;
    }

    private void setNewNullString(IValueSet valueSet, IInputFormat<String> inputFormat) {
        if (isControlForDefaultValue(valueSet)) {
            inputFormat.setNullString(Messages.DefaultValueRepresentation_EditField);
        }
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
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject) {
        return createTextAndAdaptEnum(toolkit, parent, datatype, valueSet);
    }

    protected Text createTextAndAdaptEnum(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet) {
        Text text = createPotentialEnumTextControl(toolkit, parent, getDefaultAlignment());
        adaptEnumValueSetProposal(toolkit, text, valueSet, datatype);
        return text;
    }

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
}
