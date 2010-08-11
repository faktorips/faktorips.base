/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IExtensionPropertyDefinition;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.EnumTypeDatatypeField;
import org.faktorips.devtools.core.ui.controls.DatatypeRefControl;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

/**
 * Dialog to edit a product cmpt type attribute.
 * 
 * @author Jan Ortmann
 */
public class AttributeEditDialog extends IpsPartEditDialog2 {

    /**
     * Folder which contains the pages shown by this dialog. Used to modify which page is shown.
     */
    private TabFolder folder;

    private IIpsProject ipsProject;
    private IProductCmptTypeAttribute attribute;

    /**
     * placeholder for the default edit field, the edit field for the default value depends on the
     * attributes datatype
     */
    private Composite defaultEditFieldPlaceholder;
    private EditField defaultValueField;

    private ValueSetSpecificationControl valueSetEditControl;

    private ValueDatatype currentDatatype;
    private ValueSetType currentValueSetType;

    private ExtensionPropertyControlFactory extFactory;

    public AttributeEditDialog(IProductCmptTypeAttribute productCmptTypeAttribute, Shell parentShell) {
        super(productCmptTypeAttribute, parentShell, Messages.AttributeEditDialog_title, true);

        attribute = productCmptTypeAttribute;
        ipsProject = attribute.getIpsProject();

        try {
            currentDatatype = productCmptTypeAttribute.findDatatype(ipsProject);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        currentValueSetType = productCmptTypeAttribute.getValueSet().getValueSetType();
        extFactory = new ExtensionPropertyControlFactory(attribute.getClass());
    }

    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        folder = (TabFolder)parent;

        TabItem generalItem = new TabItem(folder, SWT.NONE);
        generalItem.setText(Messages.AttributeEditDialog_general);
        generalItem.setControl(createGeneralPage(folder));

        createDescriptionTabItem(folder);
        createLabelTabItem(folder);

        return folder;
    }

    private Control createGeneralPage(TabFolder folder) throws CoreException {
        Composite c = createTabItemComposite(folder, 1, false);
        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_TOP);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_nameLabel);
        Text nameText = uiToolkit.createText(workArea);
        nameText.setFocus();
        bindingContext.bindContent(nameText, attribute, IIpsElement.PROPERTY_NAME);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_datatypeLabel);
        DatatypeRefControl datatypeControl = uiToolkit.createDatatypeRefEdit(attribute.getIpsProject(), workArea);
        datatypeControl.setVoidAllowed(false);
        datatypeControl.setOnlyValueDatatypesAllowed(true);
        bindingContext.bindContent(datatypeControl, attribute, IAttribute.PROPERTY_DATATYPE);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_modifierLabel);
        Combo modifierCombo = uiToolkit.createCombo(workArea, Modifier.class);
        bindingContext.bindContent(modifierCombo, attribute, IAttribute.PROPERTY_MODIFIER, Modifier.class);

        uiToolkit.createFormLabel(workArea, Messages.AttributeEditDialog_defaultvalueLabel);
        defaultEditFieldPlaceholder = uiToolkit.createComposite(workArea);
        defaultEditFieldPlaceholder.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        defaultEditFieldPlaceholder.setLayoutData(new GridData(GridData.FILL_BOTH));
        createDefaultValueEditField();

        uiToolkit.createVerticalSpacer(c, 4);
        uiToolkit.createHorizonzalLine(c);
        uiToolkit.createVerticalSpacer(c, 4);

        IpsObjectUIController uiController = new IpsObjectUIController(attribute);
        Composite temp = uiToolkit.createGridComposite(c, 1, true, false);
        uiToolkit.createLabel(temp, Messages.AttributeEditDialog_valueSetSection);
        uiToolkit.createVerticalSpacer(temp, 8);
        List<ValueSetType> valueSetTypes = attribute.getAllowedValueSetTypes(attribute.getIpsProject());
        valueSetEditControl = new ValueSetSpecificationControl(temp, uiToolkit, uiController, attribute, valueSetTypes,
                ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
        updateValueSetTypes();

        Object layoutData = valueSetEditControl.getLayoutData();
        if (layoutData instanceof GridData) {
            /*
             * set the minimum height to show at least the maximum size of the selected
             * <code>ValueSetEditControl</code>
             */
            GridData gd = (GridData)layoutData;
            gd.heightHint = 260;
        }

        extFactory.createControls(workArea, uiToolkit, attribute, IExtensionPropertyDefinition.POSITION_BOTTOM);
        extFactory.bind(bindingContext);

        return c;
    }

    private void createDefaultValueEditField() {
        ValueDatatypeControlFactory datatypeCtrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(
                currentDatatype);
        defaultValueField = datatypeCtrlFactory.createEditField(uiToolkit, defaultEditFieldPlaceholder,
                currentDatatype, null, ipsProject);
        if (defaultValueField instanceof EnumTypeDatatypeField) {
            ((EnumTypeDatatypeField)defaultValueField).setEnableEnumContentDisplay(false);
        }
        defaultValueField.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        defaultEditFieldPlaceholder.layout();
        defaultEditFieldPlaceholder.getParent().getParent().layout();
        bindingContext.bindContent(defaultValueField, attribute, IAttribute.PROPERTY_DEFAULT_VALUE);
    }

    @Override
    public void contentsChanged(ContentChangeEvent event) {
        super.contentsChanged(event);
        try {
            ValueDatatype newDatatype = attribute.findDatatype(ipsProject);
            boolean enabled = newDatatype != null;
            defaultValueField.getControl().setEnabled(enabled);
            valueSetEditControl.setDataChangeable(enabled);
            if (newDatatype == null || newDatatype.equals(currentDatatype)) {
                return;
            }

            currentDatatype = newDatatype;
            if (defaultValueField != null) {
                bindingContext.removeBindings(defaultValueField.getControl());
                defaultValueField.getControl().dispose();
            }

            createDefaultValueEditField();
            updateValueSetTypes();
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }

    }

    private void updateValueSetTypes() throws CoreException {
        currentValueSetType = valueSetEditControl.getValueSetType();
        valueSetEditControl.setAllowedValueSetTypes(attribute.getAllowedValueSetTypes(ipsProject));
        if (currentValueSetType != null) {
            /*
             * if the previous selction was a valid selection use this one as new selection in drop
             * down, otherwise the default (first one) is selected
             */
            valueSetEditControl.setValueSetType(currentValueSetType);
        }
    }

}
