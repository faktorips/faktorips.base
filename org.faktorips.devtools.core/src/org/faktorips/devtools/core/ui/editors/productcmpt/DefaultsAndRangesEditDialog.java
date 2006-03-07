package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IEnumValueSet;
import org.faktorips.devtools.core.model.IValueSet;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.EnumValueSetEditControl;
import org.faktorips.devtools.core.ui.controls.RangeEditControl;
import org.faktorips.devtools.core.ui.controls.TableElementValidator;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.MessageList;

/**
 *
 */
public class DefaultsAndRangesEditDialog extends IpsPartEditDialog {

    private IConfigElement configElement;
    
    // edit fields
    private TextField defaultValueField;

    /**
     * @param parentShell
     * @param title
     */
    public DefaultsAndRangesEditDialog(IConfigElement configElement, Shell parentShell) {
        super(configElement, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.configElement = configElement;
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.EditDialog#createWorkArea(org.eclipse.swt.widgets.Composite)
     */
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;

        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        createDescriptionTabItem(folder);
        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        uiToolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
        Text defaultValueText = uiToolkit.createText(workArea);

        defaultValueField = new TextField(defaultValueText);
        Control valueSetControl = createValueSetControl(workArea);
        if (valueSetControl != null) {
            GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
            valueSetGridData.horizontalSpan = 2;
            valueSetControl.setLayoutData(valueSetGridData);
        }
        return c;
    }

    private Composite createValueSetControl(Composite workArea) {
        try {
            IValueSet valueSet = configElement.getValueSet();
            IAttribute attribute = configElement.findPcTypeAttribute();
            if (attribute!=null) {
                if (valueSet.getValueSetType() == ValueSetType.ALL_VALUES) {
                    valueSet = attribute.getValueSet();
                }
            }
            
            if (valueSet.getValueSetType() == ValueSetType.RANGE) {
                RangeEditControl rangeEditControl = new RangeEditControl(workArea, uiToolkit, (RangeValueSet)valueSet, uiController);
                return rangeEditControl;
            } 
            if (valueSet.getValueSetType() == ValueSetType.ENUM) {
                EnumValueSetEditControl valueSetControl = new EnumValueSetEditControl((IEnumValueSet)valueSet,
                        workArea, new ProductElementValidator());
                return valueSetControl;
            } 
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.ui.editors.IpsPartEditDialog#connectToModel()
     */
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(defaultValueField, IConfigElement.PROPERTY_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    public void valueSetChanged(IValueSet valueSet) {
    	configElement.setValueSetCopy(valueSet);
    }

    private class ProductElementValidator implements TableElementValidator {

        /**
         * Overridden IMethod.
         * @see org.faktorips.devtools.core.ui.controls.TableElementValidator#validate(java.lang.Object)
         */
        public MessageList validate(String element) {
            MessageList list = new MessageList();
            try {
                list = configElement.validate();
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
            return list.getMessagesFor(element);
        }
    }

}
