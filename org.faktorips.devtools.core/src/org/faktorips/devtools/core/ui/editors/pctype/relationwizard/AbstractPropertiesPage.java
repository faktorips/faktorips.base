package org.faktorips.devtools.core.ui.editors.pctype.relationwizard;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsPartUIController;
import org.faktorips.devtools.core.ui.controller.fields.CardinalityField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.util.StringUtil;

/**
 * Abstract class for the relation property wizard property pages.
 */
public abstract class AbstractPropertiesPage extends AbstractPcTypeRelationWizardPage {

	// edit fields
	//   properties
    protected CardinalityField minCardinalityField;
    protected CardinalityField maxCardinalityField;
    protected TextField targetRoleSingularField;
    protected TextField targetRolePluralField;    
    //   product relevant properties
    protected CheckboxField productRelevantField;
    protected CardinalityField minCardinalityProdRelevantField;
    protected CardinalityField maxCardinalityProdRelevantField;	
    protected TextField targetRoleSingularProdRelevantField;
    protected TextField targetRolePluralProdRelevantField;
    
	public AbstractPropertiesPage(String pageId, String title,
			String description, NewPcTypeRelationWizard newPcTypeRelationWizard) {
		super(pageId, title, description, newPcTypeRelationWizard);
	}
	
	/**
	 * {@inheritDoc}
	 */
	protected void createControls(Composite parent) {
        UIToolkit uiToolkit = wizard.getUiToolkit();

        Composite mainComposite;
        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 12;
        mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setBackground(parent.getBackground());
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        mainComposite.setLayout(layout);
        mainComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite propertiesGroup = uiToolkit.createGroup(mainComposite,
                Messages.NewPcTypeRelationWizard_properties_labelGrpBoxPolicySide);
        createPropertiesFields(uiToolkit, propertiesGroup);

        uiToolkit.createVerticalSpacer(mainComposite, 12);

        Composite propertiesGroupProdRelevant = uiToolkit.createGroup(mainComposite,
                Messages.NewPcTypeRelationWizard_properties_labelGrpBoxProductSide);
        createProdRelevantPropertiesFields(uiToolkit, propertiesGroupProdRelevant);
    }
	
	/**
	 * {@inheritDoc}
	 */
	protected boolean updateControlStatus() {
		if (getCurrentRelation().getRelationType().isReverseComposition()){
			minCardinalityField.getControl().setEnabled(false);
			maxCardinalityField.getControl().setEnabled(false);
			targetRoleSingularField.getControl().setEnabled(true);
			targetRolePluralField.getControl().setEnabled(true);
			
			productRelevantField.getControl().setEnabled(false);
			setProdRelevantEnabled(false);
		}else{
			minCardinalityField.getControl().setEnabled(true);
			maxCardinalityField.getControl().setEnabled(true);
			targetRoleSingularField.getControl().setEnabled(true);
			targetRolePluralField.getControl().setEnabled(true);
			
			productRelevantField.getControl().setEnabled(true);
			setProdRelevantEnabled(getCurrentRelation().isProductRelevant());
		}
		return true;
	}
	
	/**
	 * Sets the enabled state of all controls.
	 */
	protected void setEnabledAllPropertyControls(boolean enabled){
		minCardinalityField.getControl().setEnabled(enabled);
		maxCardinalityField.getControl().setEnabled(enabled);		
		targetRoleSingularField.getControl().setEnabled(enabled);
		targetRolePluralField.getControl().setEnabled(enabled);

		productRelevantField.getControl().setEnabled(enabled);
		
		setProdRelevantEnabled(enabled);
	}
	
	/**
	 * Create the property controls of the relation.
	 */
	private void createPropertiesFields(UIToolkit uiToolkit, Composite c) {
		// create controls
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        // create top extension controls
        wizard.getExtensionFactory().createControls(workArea, uiToolkit, (IpsObjectPartContainer)getCurrentRelation(), 
                IExtensionPropertyDefinition.POSITION_TOP);
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelMinCardinality);
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelMaxCardinality);
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelTargetRoleSingular);
        final Text targetRoleSingularText = uiToolkit.createText(workArea);
        targetRoleSingularText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRoleSingularField.getText())) {
                    String targetName = getCurrentRelation().getTarget();
                    int pos = targetName.lastIndexOf('.');
                    if (pos!=-1) {
                        targetName = targetName.substring(pos+1);
                        targetRoleSingularField.setText(targetName);
                    }
                }
            }
        });
        targetRoleSingularText.addListener (SWT.Modify, new Listener () {
			public void handleEvent (Event ev) {
				// store the new name in the reverse relation field of the relation or reverse
				if (getReverseOfCurrentRelation() != null){
					getReverseOfCurrentRelation().setReverseRelation(targetRoleSingularField.getText());
				}
			}
        });
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelTargetRolePlural);
        Text targetRolePluralText = uiToolkit.createText(workArea);
        targetRolePluralText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralField.getText())) {
                    targetRolePluralField.setText(targetRoleSingularText.getText());
                }
            }
        });
    
        // create fields
        minCardinalityField = new CardinalityField(minCardinalityText);
        addFocusListenerUpdateButtons(minCardinalityField);
        maxCardinalityField = new CardinalityField(maxCardinalityText); 
        addFocusListenerUpdateButtons(maxCardinalityField);
        targetRoleSingularField = new TextField(targetRoleSingularText);
        addFocusListenerUpdateButtons(targetRoleSingularField);
        targetRolePluralField = new TextField(targetRolePluralText);
        addFocusListenerUpdateButtons(targetRolePluralField);
        
        // create bottom extension controls
        wizard.getExtensionFactory().createControls(workArea, uiToolkit, (IpsObjectPartContainer)getCurrentRelation(), 
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        
        // Connect the extension controls to the ui controller
        wizard.getExtensionFactory().connectToModel(getCurrentUiController());
	}

	/**
	 * Create the product relevant property controls.
	 */
	private void createProdRelevantPropertiesFields(UIToolkit uiToolkit, Composite c) {
		Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
		uiToolkit.createFormLabel(workArea,
				Messages.NewPcTypeRelationWizard_properties_labelProdRelevant);
		final Checkbox productRelevantCheckbox = uiToolkit
				.createCheckbox(workArea);
		productRelevantCheckbox.getButton().addSelectionListener(
				new SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						// first store the current modified value, because the
						// controller updates the model later
						getCurrentRelation().setProductRelevant(
								productRelevantCheckbox.getButton()
										.getSelection());
						setProdRelevantEnabled(getCurrentRelation()
								.isProductRelevant());
					}
				});
		productRelevantField = new CheckboxField(productRelevantCheckbox);
		productRelevantField.addChangeListener(new ValueChangeListener (){
			public void valueChanged(FieldValueChangedEvent e) {
				getContainer().updateButtons();
			}
		});
		
		uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelMinCardinalityProdRelevant);
        Text minCardinalityTextProdRelevant = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelMaxCardinalityProdRelevant);
        Text maxCardinalityTextProdRelevant = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelTargetRoleSingularProdRelevant);
        Text targetRoleSingularTextProdRelevant = uiToolkit.createText(workArea);
        targetRoleSingularTextProdRelevant.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRoleSingularProdRelevantField.getText())) {
                    String targetName = getCurrentRelation().getPolicyCmptType().getProductCmptType();
                    targetName = StringUtil.unqualifiedName(targetName);
                    targetRoleSingularProdRelevantField.setText(targetName);
                }
            }
        });
        
        uiToolkit.createFormLabel(workArea, Messages.NewPcTypeRelationWizard_properties_labelTargetRolePluralProdRelevant);
        Text targetRolePluralTextProdRelevant = uiToolkit.createText(workArea);
        targetRolePluralTextProdRelevant.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (StringUtils.isEmpty(targetRolePluralProdRelevantField.getText())) {
                    String targetName = getCurrentRelation().getPolicyCmptType().getProductCmptType();
                    targetName = StringUtil.unqualifiedName(targetName);
                    targetRolePluralProdRelevantField.setText(targetName);
                }
            }
        });        
        
        // create fields
        minCardinalityProdRelevantField = new CardinalityField(minCardinalityTextProdRelevant);
        
        maxCardinalityProdRelevantField = new CardinalityField(maxCardinalityTextProdRelevant);
        addFocusListenerUpdateButtons(maxCardinalityProdRelevantField);
        targetRoleSingularProdRelevantField = new TextField(targetRoleSingularTextProdRelevant);
        addFocusListenerUpdateButtons(targetRoleSingularProdRelevantField);
        targetRolePluralProdRelevantField = new TextField(targetRolePluralTextProdRelevant);
        addFocusListenerUpdateButtons(targetRolePluralProdRelevantField);
	}
	
	/**
	 * Sets the enabled state of the product relevant property controls.
	 */	
	private void setProdRelevantEnabled(boolean isProdRelevantEnabled) {
		minCardinalityProdRelevantField.getControl().setEnabled(isProdRelevantEnabled);
		maxCardinalityProdRelevantField.getControl().setEnabled(isProdRelevantEnabled);	
		targetRoleSingularProdRelevantField.getControl().setEnabled(isProdRelevantEnabled);
		targetRolePluralProdRelevantField.getControl().setEnabled(isProdRelevantEnabled);
	}
	
	/**
	 * Returns the relation which will be changed by this wizard page.
	 */
	abstract protected IRelation getCurrentRelation();
	
    /**
     * Returns the ui controller of the relation which will be changed by this wizard page.
     */
    abstract protected IpsPartUIController getCurrentUiController();
    
	/**
	 * Returns the reverse relation of the relation which will changed by this wizard page.
	 */
	abstract protected IRelation getReverseOfCurrentRelation();
	
	/**
	 * Adds a focus listener to the given field.
	 */
	abstract protected void addFocusListenerUpdateButtons(EditField field);
}
