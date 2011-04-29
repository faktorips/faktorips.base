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

package org.faktorips.devtools.core.ui.editors.projectproperties.sections;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.devtools.core.ui.controller.fields.StringValueComboField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.EditDialog;

public class DefinedDataypeDialog extends EditDialog {

    private DatatypeRefControl datatypeControl;
    private Text supportingNameText;
    private Text javaClassText;
    private Text valueObjectText;
    private Combo valueSetTypesCombo;
    private StringValueComboField valueSetTypeField;
    private Datatype datatype;
    private ComboViewer enumtypeComboViewer;
    private ComboViewer supportingNameComboViewer;
    private ComboViewer valueObjectComboViewer;
    private ComboViewer nullTextComboViewer;
    private Text nameText;

    public DefinedDataypeDialog(Datatype datatype, Shell shell, String windowTitle) {
        super(shell, windowTitle);
        this.datatype = datatype;
    }

    @Override
    protected Composite createWorkArea(Composite parent) {
        Composite workArea = uiToolkit.createLabelEditColumnComposite(parent);

        uiToolkit.createLabel(workArea, Messages.DefinedDataypeDialog_name);
        nameText = uiToolkit.createText(workArea);

        uiToolkit.createLabel(workArea, Messages.DatatypesSection_javaClass);
        javaClassText = uiToolkit.createText(workArea);

        uiToolkit.createLabel(workArea, Messages.DatatypesSection_valueObject);
        Combo valueObjectCombo = uiToolkit.createCombo(workArea);
        valueObjectComboViewer = createComboViewer(valueObjectCombo);
        valueObjectComboViewer.addSelectionChangedListener(new SelectionChangedListener());

        uiToolkit.createLabel(workArea, Messages.DatatypesSection_null);
        Combo nullTextCombo = uiToolkit.createCombo(workArea);
        nullTextComboViewer = createComboViewer(nullTextCombo);

        uiToolkit.createLabel(workArea, Messages.DatatypesSection_enumtype);
        Combo enumtypeCombo = uiToolkit.createCombo(workArea);
        enumtypeComboViewer = createComboViewer(enumtypeCombo);
        enumtypeComboViewer.addPostSelectionChangedListener(new SelectionChangedListener());

        uiToolkit.createLabel(workArea, Messages.DatatypesSection_supportingName);
        Combo supportingNameCombo = uiToolkit.createCombo(workArea);
        supportingNameComboViewer = createComboViewer(supportingNameCombo);

        if (datatype != null) {
            setComboViewer(enumtypeComboViewer, datatype.isEnum());
            setComboViewer(supportingNameComboViewer, datatype.isValueDatatype());
            setComboViewer(nullTextComboViewer, datatype.hasNullObject());
            javaClassText.setText(datatype.getJavaClassName());
            nameText.setText(datatype.getName());
            if (datatype instanceof EnumDatatype) {
                EnumDatatype enumDatatype = (EnumDatatype)datatype;
                setComboViewer(supportingNameComboViewer, enumDatatype.isSupportingNames());
            }
        }
        // datatypeControl = uiToolkit.createDatatypeRefEdit(null, workArea);
        // datatypeControl.setVoidAllowed(false);
        // datatypeControl.setOnlyValueDatatypesAllowed(true);
        // bindingContext.bindContent(datatypeControl, null, IAttribute.PROPERTY_DATATYPE);
        // extFactory.createControls(workArea, uiToolkit, null,
        // IExtensionPropertyDefinition.POSITION_BOTTOM);
        // return extFactory.bind(bindingContext);
        return workArea;
    }

    // private Control showControlForValueSet() {
    // StackLayout layout = (StackLayout)valueSetArea.getLayout();
    // layout.topControl = updateControlWithCurrentValueSetOrCreateNewIfNeccessary(valueSetArea);
    // setDataChangeable(isDataChangeable()); // set data changeable state of controls
    // return layout.topControl;
    // }
    //
    // private Control updateControlWithCurrentValueSetOrCreateNewIfNeccessary(Composite parent) {
    // IValueSet valueSet = getValueSet();
    // if (valueSet.isAbstract() || valueSet.isUnrestricted()) {
    // // no further editing possible, return empty composite
    // return toolkit.createComposite(parent);
    // }
    // ValueDatatype valueDatatype = getValueDatatype();
    // if (getValueSetEditControl() != null && getValueSetEditControl().canEdit(valueSet,
    // valueDatatype)) {
    // // the current composite can be reused to edit the current value set
    // getValueSetEditControl().setValueSet(valueSet, valueDatatype);
    // return valueSetEditControl.getParent(); // have to return the parent here, as there is a
    // // group control (see below) around the edit control. There has to be a better way to do
    // // this!
    // }
    // // Creates a new composite to edit the current value set
    // Group group = createGroupAroundValueSet(parent, valueSet.getValueSetType().getName());
    // ValueSetEditControlFactory factory = new ValueSetEditControlFactory();
    // Control c = factory.newControl(valueSet, valueDatatype, group, toolkit, uiController);
    // c.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_BOTH));
    // setValueSetEditControl(c);
    // return group;
    // }

    public ComboViewer createComboViewer(Combo combo) {
        ComboViewer comboViewer = new ComboViewer(combo);
        comboViewer.setContentProvider(new ArrayContentProvider());
        Boolean[] input = new Boolean[] { true, false };
        comboViewer.setInput(input);
        return comboViewer;
    }

    private void setComboViewer(ComboViewer cv, boolean selected) {
        if (selected) {
            cv.setSelection(new StructuredSelection(true), true);
        } else {
            cv.setSelection(new StructuredSelection(false), true);
        }
    }

    private class SelectionChangedListener implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            // TODO Auto-generated method stub

        }
    }

    public void createNewDatatype() {
        Datatype newDatatype = null;
        if (getValue(enumtypeComboViewer.getSelection())) {
            // datatype = new ValueClassDatatype();
        } else if (getValue(enumtypeComboViewer.getSelection())) {
            // datatype = new
        }
        // iIpsProjectProperties.addDefinedDatatype();

    }

    public Boolean getValue(ISelection selection) {
        if (selection instanceof StructuredSelection) {
            StructuredSelection selected = (StructuredSelection)selection;
            return (Boolean)selected.getFirstElement();
        }
        return null;
    }
}
