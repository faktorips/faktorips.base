/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.formulalibrary.ui.editors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameterContainer;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.editors.type.ParametersEditControl;
import org.faktorips.devtools.core.ui.forms.IpsSection;
import org.faktorips.devtools.formulalibrary.ui.Messages;

/**
 * The <tt>SelectedFormulaSignatureSection</tt> generates an <tt>IpsSection</tt> with
 * <tt>DatatypeRefControl</tt> and <tt>ParameterEditControl</tt>.
 * <p>
 * This shows the formula name, return type and parameters of selected formula in
 * <tt>FormulaFunctionListSection</tt> .
 * 
 * @author HBaagil
 */

public class SelectedFormulaSignatureSection extends IpsSection {

    private static final String ID = "org.faktorips.devtools.formulalibrary.ui.editors.SelectedFormulaSignatureSection"; //$NON-NLS-1$

    private Composite selectedFormulaSignatureComposite;
    private FormulaFunctionPmo formulaFunctionPmo;
    private ParametersEditControl parameterEditControl;
    private DatatypeRefControl dataTypeRefControl;
    private FormulaFunctionChangedListener formulaFunctionChangedListener;
    private IIpsProject ipsProject;
    private Text formulaNameText;

    /**
     * Create a new <tt>SelectedFormulaSignatureSection</tt>.
     * 
     * @param parent The <tt>Composite</tt> this <tt>IpsSection</tt> belongs to.
     * @param toolkit The <tt>UIToolkit</tt> for look and feel controls.
     * @param formulaFunctionPmo The <tt>IpsObjectPartPmo</tt> as presentation model object for
     *            <tt>IFormulaFunction</tt>.
     */
    public SelectedFormulaSignatureSection(Composite parent, UIToolkit toolkit, FormulaFunctionPmo formulaFunctionPmo,
            IIpsProject ipsProject) {
        super(ID, parent, GridData.FILL_BOTH, toolkit);
        this.formulaFunctionPmo = formulaFunctionPmo;
        this.ipsProject = ipsProject;
        initControls();
        toolkit.paintBordersForComposite(parent);
    }

    @Override
    protected void initClientComposite(Composite client, UIToolkit toolkit) {
        createSignatureSection(client);
        formulaFunctionChangedListener = new FormulaFunctionChangedListener(formulaFunctionPmo, parameterEditControl,
                dataTypeRefControl, formulaNameText);
        formulaFunctionPmo.addPropertyChangeListener(formulaFunctionChangedListener);
        getBindingContext().updateUI();
    }

    private void createSignatureSection(Composite parent) {
        selectedFormulaSignatureComposite = getToolkit().createGridComposite(parent, 3, false, false);
        GridLayout rightTopGrid = new GridLayout();
        rightTopGrid.numColumns = 3;
        selectedFormulaSignatureComposite.setLayout(rightTopGrid);

        cretateSignatureWidgets();
    }

    private void cretateSignatureWidgets() {
        createFormulaNameLabel();
        createFormulaNameText();
        createReturnTypeField();
        createParameterTable();
    }

    private void createFormulaNameLabel() {
        GridData fNLabelGridData = createGridDataForFormulaNameLabel();
        Label formulaNameLabel = new Label(selectedFormulaSignatureComposite, SWT.NONE);
        formulaNameLabel.setText(Messages.SelectedFormulaSignatureSection_FormulaNameLabel);
        formulaNameLabel.setLayoutData(fNLabelGridData);
    }

    private void createFormulaNameText() {
        GridData fNTextGridData = createGridDataForFormulaNameText();
        formulaNameText = new Text(selectedFormulaSignatureComposite, SWT.BORDER);
        formulaNameText.setLayoutData(fNTextGridData);
        checkIfFormulaNameTextIsEditable();

        bindFormulaName(formulaNameText);
    }

    private void checkIfFormulaNameTextIsEditable() {
        if (formulaFunctionPmo.getFormulaFunction() == null) {
            formulaNameText.setEditable(false);
        }
    }

    private void createReturnTypeField() {
        GridData gridDadta = createGridDataForReturnTypeField();
        getToolkit().createFormLabel(selectedFormulaSignatureComposite,
                Messages.SelectedFormulaSignatureSection_ReturnTypeLabel);
        cretateDataRefControl(gridDadta);
        checkIfReturnTypeFieldIsEditable();

        bindReturnType();
    }

    private void checkIfReturnTypeFieldIsEditable() {
        if (formulaFunctionPmo.getFormulaFunction() == null) {
            dataTypeRefControl.getToolkit().setDataChangeable(dataTypeRefControl, false);
        }
    }

    private void createParameterTable() {
        GridData gridData = createGridDataForParameterTable();
        createParameterEditControl(gridData);
    }

    private void cretateDataRefControl(GridData gridDadta) {
        dataTypeRefControl = getToolkit().createDatatypeRefEdit(ipsProject, selectedFormulaSignatureComposite);
        dataTypeRefControl.setLayoutData(gridDadta);
        dataTypeRefControl.setOnlyValueDatatypesAllowed(true);
    }

    private void createParameterEditControl(GridData gridData) {
        parameterEditControl = new ParametersEditControl(selectedFormulaSignatureComposite, getToolkit(), SWT.NONE,
                Messages.SelectedFormulaSignatureSection_ParamtersLabel, ipsProject);
        parameterEditControl.setDataChangeable(true);
        parameterEditControl.initControl();
        parameterEditControl.setLayoutData(gridData);
    }

    private GridData createGridDataForFormulaNameLabel() {
        GridData fNLabelGridData = new GridData();
        fNLabelGridData.horizontalAlignment = SWT.BEGINNING;
        return fNLabelGridData;
    }

    private GridData createGridDataForFormulaNameText() {
        GridData fNTextGridData = createGridDataForReturnTypeField();
        fNTextGridData.grabExcessHorizontalSpace = true;
        fNTextGridData.verticalAlignment = SWT.CENTER;
        return fNTextGridData;
    }

    private GridData createGridDataForReturnTypeField() {
        GridData gridDadta = new GridData();
        gridDadta.horizontalSpan = 2;
        gridDadta.horizontalAlignment = SWT.FILL;
        return gridDadta;
    }

    private GridData createGridDataForParameterTable() {
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        return gridData;
    }

    private void bindFormulaName(Text formulaNameText) {
        getBindingContext().bindContent(formulaNameText, formulaFunctionPmo, FormulaFunctionPmo.PROPERTY_FORMULA_NAME);
    }

    private void bindReturnType() {
        getBindingContext().bindContent(dataTypeRefControl, formulaFunctionPmo,
                FormulaFunctionPmo.PROPERTY_RETURN_TYPE, false);
    }

    @Override
    public void widgetDisposed(DisposeEvent e) {
        super.widgetDisposed(e);
        formulaFunctionPmo.removePropertyChangeListener(formulaFunctionChangedListener);
    }

    private static final class FormulaFunctionChangedListener implements PropertyChangeListener {

        private final FormulaFunctionPmo formulaFunctionPmo;
        private final ParametersEditControl parameterEditControl;
        private final DatatypeRefControl dataTypeRefControl;
        private final Text text;

        public FormulaFunctionChangedListener(FormulaFunctionPmo formulaFunctionPmo,
                ParametersEditControl parametersEditControl, DatatypeRefControl dataTypeRefControl, Text text) {
            this.formulaFunctionPmo = formulaFunctionPmo;
            this.parameterEditControl = parametersEditControl;
            this.dataTypeRefControl = dataTypeRefControl;
            this.text = text;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (FormulaFunctionPmo.PROPERTY_IPS_OBJECT_PART_CONTAINER.equals(evt.getPropertyName())) {
                boolean isEditable = false;
                IParameterContainer parameterContainer = null;
                if (formulaFunctionPmo.getFormulaFunction() != null) {
                    parameterContainer = formulaFunctionPmo.getFormulaFunction().getFormulaMethod();
                    isEditable = true;
                }
                parameterEditControl.setInput(parameterContainer);
                dataTypeRefControl.getToolkit().setDataChangeable(dataTypeRefControl, isEditable);
                text.setEditable(isEditable);
            }
        }
    }
}
