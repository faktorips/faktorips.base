package org.faktorips.devtools.core.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.model.Range;
import org.faktorips.devtools.core.model.ValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.fields.TextField;

/**
 * A composite that consits of three textfields for input. if there is a uicontroller supplied it is used to establish a
 * mapping between the modell object and the control which represents the object property.
 */
public class RangeEditControl extends ControlComposite implements InternalValueSetEditControl, ModifyListener {

    // lower and button controls
    private Text lower;
    private Text upper;
    private Text step;
    private Range range;
    private TextField lowerfield;
    private TextField upperfield;
    private TextField stepfield;
    private DefaultUIController uicontroller;
    private ValueSetChangeListener valuesetchangelistener;

    /**
     */
    public RangeEditControl(Composite parent, UIToolkit toolkit, Range range, DefaultUIController uiController) {
        super(parent, SWT.NONE);
        this.uicontroller = uiController;
        this.range = range;
        setLayout();
        Group group = createRangeGroup(toolkit);
        Composite workArea = createWorkArea(toolkit, group);
        createTextControls(toolkit, workArea);
        connectToModel();
    }

    private void setLayout() {
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 10;
        layout.marginWidth = 0;
        setLayout(layout);
    }

    private Group createRangeGroup(UIToolkit toolkit) {
        Group group = toolkit.createGroup(this, " Range ");
        GridLayout grouplayout = new GridLayout(1, false);
        grouplayout.marginHeight = 10;
        group.setLayout(grouplayout);
        return group;
    }

    private Composite createWorkArea(UIToolkit toolkit, Composite parent) {
        Composite workArea;
        GridLayout layoutWorkArea = new GridLayout(2, false);
        if (toolkit.getFormToolkit() == null) {
            workArea = toolkit.createComposite(parent);
            layoutWorkArea.marginHeight = 0;
            layoutWorkArea.marginWidth = 0;
            layoutWorkArea.horizontalSpacing = 12; // this is important for the diplayed icons !!
    
        } else {
            workArea = toolkit.getFormToolkit().createComposite(parent);
            layoutWorkArea.marginHeight = 3;
            layoutWorkArea.marginWidth = 1;
        }
        workArea.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END | GridData.FILL_HORIZONTAL));
        workArea.setLayout(layoutWorkArea);
        return workArea;
    }

    private void createTextControls(UIToolkit toolkit, Composite workArea) {
        toolkit.createFormLabel(workArea, "Minimum");
        lower = toolkit.createText(workArea);
        lower.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        
        toolkit.createLabel(workArea, "Maximum");        
        upper = toolkit.createText(workArea);
        upper.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        
        toolkit.createFormLabel(workArea, "Step");        
        step = toolkit.createText(workArea);
        step.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        
        if (toolkit.getFormToolkit() != null) {
            toolkit.getFormToolkit().paintBordersFor(workArea);
            toolkit.getFormToolkit().adapt(workArea);
        }
    }

    private void connectToModel() {
        upperfield = new TextField(upper);
        lowerfield = new TextField(lower);
        stepfield = new TextField(step);
        uicontroller.add(upperfield, range, Range.PROPERTY_UPPERBOUND);
        uicontroller.add(lowerfield, range, Range.PROPERTY_LOWERBOUND);
        uicontroller.add(stepfield, range, Range.PROPERTY_STEP);
        uicontroller.updateUI();
        upper.addModifyListener(this);
        lower.addModifyListener(this);
        step.addModifyListener(this);
    }

    public Range getRange() {
        return range;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controls.InternalValueSetEditControl#getValueSet()
     */
    public ValueSet getValueSet() {
        return range;
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.ui.controls.InternalValueSetEditControl#setValueSet(org.faktorips.devtools.core.model.ValueSet)
     */
    public void setValueSet(ValueSet valueSet) {
        range = (Range)valueSet;
    }

    /**
     * sets a valuesetchangelistener which listens to changes of a valueset
     */
    public void setValueSetChangeListener(ValueSetChangeListener valuesetchangelistener) {
        this.valuesetchangelistener = valuesetchangelistener;
    }

    public void setLower(String newText) {
        lower.setText(newText);
    }

    public String getLower() {
        return lower.getText();
    }

    public void setUpper(String newText) {
        upper.setText(newText);
    }

    public String getUpper() {
        return upper.getText();
    }

    public void setStep(String newText) {
        step.setText(newText);
    }

    public String getStep() {
        return step.getText();
    }

    public Text getLowerControl() {
        return lower;
    }

    public Text getStepControl() {
        return step;
    }

    public Text getUpperControl() {
        return upper;
    }

    public boolean setFocus() {
        return lower.setFocus();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    /**
     * Overridden IMethod.
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    public void modifyText(ModifyEvent e) {
        valuesetchangelistener.valueSetChanged(range);
    }

}
