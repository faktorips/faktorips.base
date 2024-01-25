/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.ui.controlfactories.EnumerationControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalAdapter;
import org.faktorips.devtools.core.ui.controller.fields.enumproposal.EnumerationProposalProvider;
import org.faktorips.devtools.core.ui.inputformat.IInputFormat;
import org.faktorips.devtools.core.ui.table.EditFieldCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A factory to create controls and edit fields that allow to edit values for one or more value
 * datatypes.
 *
 * @author Joerg Ortmann
 */
public abstract class ValueDatatypeControlFactory {

    private static final int ARROW_DOWN_BUTTON_WIDTH = 22;

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
     * @param ipsProject The project the value set's parent object belongs to.
     */
    public abstract EditField<String> createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject);

    /**
     * Creates a control that allows to edit a value of the value datatype this is a factory for.
     *
     * @param toolkit The toolkit used to create the control.
     * @param parent The parent composite to which the control is added.
     * @param datatype The value datatype a control should be created for.
     * @param valueSet An optional @Deprecated valueset.Future Implementations should use
     *            ValueSetOwner instead.
     * @param ipsProject The ipsProject where the control belongs to.
     */
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            @Deprecated IValueSet valueSet,
            IIpsProject ipsProject) {
        return createTextAndAdaptEnumProposal(toolkit, parent, datatype, valueSet, ipsProject);
    }

    protected Text createTextAndAdaptEnumProposal(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = createPotentialEnumTextControl(toolkit, parent, getDefaultAlignment());
        adaptEnumValueProposal(toolkit, text, valueSet, datatype, ipsProject);
        return text;
    }

    /**
     * In case a value set is defined ,enumeration support is added to the text control by adding an
     * {@link EnumerationProposalProvider}.
     *
     * @param toolkit The toolkit used to create the control.
     * @param textControl the text control to add enumeration support to
     * @param valueSet the value set that provides the values
     * @param datatype the data type of the field. May also provide enumeration values if it is an
     *            enum data type.
     * @param ipsProject The current project
     */
    protected void adaptEnumValueProposal(UIToolkit toolkit,
            Text textControl,
            IValueSet valueSet,
            ValueDatatype datatype,
            IIpsProject ipsProject) {
        if (valueSet != null) {
            IValueSetOwner valueSetOwner = valueSet.getValueSetOwner();
            IInputFormat<String> inputFormat = getInputFormat(datatype, valueSet, ipsProject);
            Button button = createArrowDownButton(toolkit, textControl.getParent());
            EnumerationProposalAdapter proposalAdapter = EnumerationProposalAdapter.createAndActivateOnAnyKey(
                    textControl, button, datatype, valueSetOwner,
                    inputFormat);
            registerUpdateListener(valueSet.getValueSetOwner(), button, proposalAdapter, datatype);
            updateRequiresEnumValueProposal(button, proposalAdapter, valueSet, datatype);
        }
    }

    private void registerUpdateListener(IValueSetOwner valueSetOwner,
            Button button,
            EnumerationProposalAdapter proposalAdapter,
            ValueDatatype datatype) {
        final ContentsChangeListener contentChangeListener = ContentsChangeListener.forEventsAffecting(valueSetOwner,
                $ -> updateRequiresEnumValueProposal(button, proposalAdapter, valueSetOwner.getValueSet(), datatype));
        valueSetOwner.getIpsModel().addChangeListener(contentChangeListener);
        button.addDisposeListener(e -> valueSetOwner.getIpsModel().removeChangeListener(contentChangeListener));
    }

    private void updateRequiresEnumValueProposal(
            Button button,
            EnumerationProposalAdapter proposalAdapter,
            IValueSet newValueSet,
            ValueDatatype datatype) {
        if (!button.isDisposed()) {
            boolean requiresEnumValueProposal = requiresEnumValueProposal(newValueSet)
                    || (datatype != null && datatype.isEnum());
            button.setVisible(requiresEnumValueProposal);
            proposalAdapter.setEnabled(requiresEnumValueProposal);
            GridData layoutData = (GridData)button.getLayoutData();
            layoutData.widthHint = requiresEnumValueProposal ? ARROW_DOWN_BUTTON_WIDTH : 0;
            button.setLayoutData(layoutData);
            button.getParent().layout();
        }
    }

    /**
     * This method returns <code>true</code> if:
     * <ul>
     * <li>the value set is not <code>null</code> and at the same time</li>
     * <li>the value set is an enum</li>
     * </ul>
     *
     * For enum datatypes, content proposal will be added by the implementation in
     * {@link EnumerationControlFactory} .
     */
    private boolean requiresEnumValueProposal(IValueSet valueSet) {
        return valueSet != null && valueSet.isEnum();
    }

    protected IInputFormat<String> getInputFormat(ValueDatatype datatype, IValueSet valueSet, IIpsProject ipsProject) {
        IInputFormat<String> inputFormat = IpsUIPlugin.getDefault().getInputFormat(datatype, ipsProject);
        setNewNullString(valueSet, datatype, inputFormat);
        return inputFormat;
    }

    private void setNewNullString(IValueSet valueSet, ValueDatatype datatype, IInputFormat<String> inputFormat) {
        if (isControlForDefaultValue(valueSet, datatype)) {
            inputFormat.setNullString(Messages.DefaultValueRepresentation_EditField);
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
        Button button = toolkit.createButton(parent, IpsStringUtils.EMPTY);
        GridData buttonData = new GridData();
        // Matches height of the text field in Win7
        buttonData.heightHint = 28;
        // minimum width so the arrow is still visible
        buttonData.widthHint = ARROW_DOWN_BUTTON_WIDTH;
        button.setLayoutData(buttonData);

        Image arrowDown = IpsUIPlugin.getImageHandling().getSharedImage("ArrowDown_grey.gif", true); //$NON-NLS-1$
        button.setImage(arrowDown);

        return button;
    }

    /**
     * Creates a cell editor that allows to edit a value of the value datatype this is a factory
     * for.
     *
     * @deprecated use {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *                 instead.
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

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(),
                ipsProject);
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

        EditField<String> editField = createEditFieldForTable(toolkit, parent, dataType, valueSet, ipsProject);
        return new EditFieldCellEditor(editField);
    }

    /**
     * Creates a text control and a corresponding {@link FormattingTextField} for use in a table.
     * Subclasses may override to create different controls or different edit fields.
     * <p>
     * This method is called by the default implementation of
     * {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     * . If subclasses override createTableCellEditor() this method can be ignored.
     *
     * @param ipsProject The ipsProject where the control belongs to.
     */
    protected EditField<String> createEditFieldForTable(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {
        Text text = toolkit.createTextAppendStyle(parent, getDefaultAlignment());
        return new FormattingTextField<>(text, getInputFormat(datatype, valueSet, ipsProject));
    }

    public abstract int getDefaultAlignment();

    protected boolean isControlForDefaultValue(IValueSet valueSet, ValueDatatype datatype) {
        return valueSet != null && isConfigElement(valueSet) && !datatype.isPrimitive();
    }

    private boolean isConfigElement(IValueSet valueSet) {
        return valueSet.getValueSetOwner() instanceof IConfigElement;
    }
}
