package org.faktorips.devtools.core.ui.editors.pctype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPartContainer;
import org.faktorips.devtools.core.model.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.ui.CompletionUtil;
import org.faktorips.devtools.core.ui.contentassist.ContentAssistHandler;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueField;
import org.faktorips.devtools.core.ui.controller.fields.IntegerField;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.PcTypeRefControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;


/**
 * A dialog to edit a relation.
 */
public class RelationEditDialog extends IpsPartEditDialog {
    
    private final static String PROPERTY_PM_CLASSRELATION_ID = "de.bbv.faktorips.relation.pmClassRelationId";
    private final static String PROPERTY_PM_TARGET_OBJECT_NAME = "de.bbv.faktorips.relation.pmTargetObjectName";
    
    private IRelation relation;
    
    // edit fields
    private EnumValueField typeField;
    private CheckboxField abstractContainerField;
    private TextButtonField targetField;
    private TextField targetRoleSingularField;
    private TextField targetRolePluralField;
    private IntegerField minCardinalityField;
    private TextField maxCardinalityField;
    private CheckboxField productRelevantField;
    private TextField containerRelationField;
    private TextField reverseRelationField;
    
    private EditField[] extensionEditFields;
    
    private EditField pmRelationIdField; // TODO remove from core
    private EditField pmTargetObjectName; // TODO remove from core

    /**
     * @param parentShell
     * @param title
     */
    public RelationEditDialog(IRelation relation, Shell parentShell) {
        super(relation, parentShell, "Edit Relation", true);
        this.relation = relation;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText("Properties");
        firstPage.setControl(createFirstPage(folder));
        
        createDescriptionTabItem(folder);
        return folder;
    }
    
    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        uiToolkit.createFormLabel(workArea, "Type:");
        Combo typeCombo = uiToolkit.createCombo(workArea, RelationType.getEnumType());
        typeCombo.setFocus();
        
        uiToolkit.createFormLabel(workArea, "Read-Only Container:");
        Checkbox abstractContainerCheckbox = uiToolkit.createCheckbox(workArea);

        uiToolkit.createFormLabel(workArea, "Target:");
        PcTypeRefControl targetControl = uiToolkit.createPcTypeRefControl(relation.getIpsProject(), workArea);
        targetControl.addFocusListener(new FocusAdapter() {
            
            public void focusLost(FocusEvent e) {
                if (StringUtils.isEmpty(targetRoleSingularField.getText())) {
                    String targetName = targetField.getText();
                    int pos = targetName.lastIndexOf('.');
                    if (pos!=-1) {
                        targetName = targetName.substring(pos+1);
                    }
                    targetRoleSingularField.setText(targetName);
                }
            }
        });
        
        uiToolkit.createFormLabel(workArea, "Target Role (Singular):");
        Text targetRoleSingularText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, "Target Role (Plural):");
        Text targetRolePluralText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, "Minimum Cardinality:");
        Text minCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, "Maximum Cardinality:");
        Text maxCardinalityText = uiToolkit.createText(workArea);
        
        uiToolkit.createFormLabel(workArea, "Product Relevant:");
        Checkbox productRelevantCheckbox = uiToolkit.createCheckbox(workArea);
        
        uiToolkit.createFormLabel(workArea, "Reverse Relation:");
        Text reverseRelationText = uiToolkit.createText(workArea);
        ReverseRelationCompletionProcessor reverseRelationCompletionProcessor = new ReverseRelationCompletionProcessor(relation);
        reverseRelationCompletionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(reverseRelationText, CompletionUtil.createContentAssistant(reverseRelationCompletionProcessor));

        uiToolkit.createFormLabel(workArea, "Container Relation:");
        Text containerRelationText = uiToolkit.createText(workArea);
        ContainerRelationCompletionProcessor completionProcessor = new ContainerRelationCompletionProcessor(relation);
        completionProcessor.setComputeProposalForEmptyPrefix(true);
        ContentAssistHandler.createHandlerForText(containerRelationText, CompletionUtil.createContentAssistant(completionProcessor));
        
        // create fields
        typeField = new EnumValueField(typeCombo, RelationType.getEnumType());
        abstractContainerField = new CheckboxField(abstractContainerCheckbox);
        targetField = new TextButtonField(targetControl);
        targetRoleSingularField = new TextField(targetRoleSingularText);
        targetRolePluralField = new TextField(targetRolePluralText);
        minCardinalityField = new IntegerField(minCardinalityText);
        maxCardinalityField = new TextField(maxCardinalityText);
        productRelevantField = new CheckboxField(productRelevantCheckbox);
        containerRelationField = new TextField(containerRelationText);
        reverseRelationField = new TextField(reverseRelationText);
        
        
        
        
        //IExtensionPropertyDefinition
        // TODO pm extension, remove from core
        IExtensionPropertyDefinition[] extProps = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinitions(IRelation.class,true);
        for(int i=0; i< extProps.length; i++)
        {
        	//extProps[i];
        }
        IExtensionPropertyDefinition extProp = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinition(IRelation.class, PROPERTY_PM_CLASSRELATION_ID, true);
        if (extProp!=null) {
            uiToolkit.createFormLabel(workArea, extProp.getDisplayName() + ":");
        	pmRelationIdField = extProp.newEditField((IpsObjectPartContainer)relation, workArea, uiToolkit);
        }
        extProp = IpsPlugin.getDefault().getIpsModel().getExtensionPropertyDefinition(IRelation.class, PROPERTY_PM_TARGET_OBJECT_NAME, true);
        if (extProp!=null) {
            uiToolkit.createFormLabel(workArea, extProp.getDisplayName() + ":");
            pmTargetObjectName = extProp.newEditField((IpsObjectPartContainer)relation, workArea, uiToolkit);
        }
        return c;
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(targetField, IRelation.PROPERTY_TARGET);
        uiController.add(abstractContainerField, IRelation.PROPERTY_READONLY_CONTAINER);
        uiController.add(targetRoleSingularField, IRelation.PROPERTY_TARGET_ROLE_SINGULAR);
        uiController.add(targetRolePluralField, IRelation.PROPERTY_TARGET_ROLE_PLURAL);
        uiController.add(typeField, IRelation.PROPERTY_RELATIONTYPE);
        uiController.add(minCardinalityField, IRelation.PROPERTY_MIN_CARDINALITY);
        uiController.add(maxCardinalityField, IRelation.PROPERTY_MAX_CARDINALITY);
        uiController.add(containerRelationField, IRelation.PROPERTY_CONTAINER_RELATION);
        uiController.add(reverseRelationField, IRelation.PROPERTY_REVERSE_RELATION);
        uiController.add(productRelevantField, IRelation.PROPERTY_PRODUCT_RELEVANT);
        
        // TODO remove from core
        if (pmRelationIdField!=null) {
            uiController.add(pmRelationIdField, PROPERTY_PM_CLASSRELATION_ID);
        }
        if (pmTargetObjectName!=null) {
            uiController.add(pmTargetObjectName, PROPERTY_PM_TARGET_OBJECT_NAME);
        }
    }
    
}
