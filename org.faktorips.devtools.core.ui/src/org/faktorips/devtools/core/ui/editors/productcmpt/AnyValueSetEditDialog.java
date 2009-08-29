/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controller.fields.DefaultEditField;
import org.faktorips.devtools.core.ui.controller.fields.EnumDatatypeField;
import org.faktorips.devtools.core.ui.controller.fields.EnumValueSetField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controls.TableElementValidator;
import org.faktorips.devtools.core.ui.controls.valuesets.EnumValueSetChooser;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 *
 */
public class AnyValueSetEditDialog extends IpsPartEditDialog {

    private IConfigElement configElement;

    // edit fields
    private DefaultEditField defaultValueField;

    private boolean viewOnly;

    private List<ValueSetType> allowedValuesSetTypes;
    private ValueDatatype valueDatatype;

    /**
     * @param parentShell
     * @param title
     */
    public AnyValueSetEditDialog(IConfigElement configElement, ValueDatatype valueDatatype,
            List<ValueSetType> allowedTypes, Shell parentShell) {
        this(configElement, valueDatatype, allowedTypes, parentShell, false);
    }

    public AnyValueSetEditDialog(IConfigElement configElement, ValueDatatype valueDatatype,
            List<ValueSetType> allowedTypes, Shell parentShell, boolean viewOnly) {

        super(configElement, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.configElement = configElement;
        this.valueDatatype = valueDatatype;
        this.viewOnly = viewOnly;
        allowedValuesSetTypes = allowedTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Composite createWorkArea(Composite parent) throws CoreException {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        createDescriptionTabItem(folder);
        super.setEnabledDescription(!viewOnly);

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);

        Composite workArea = uiToolkit.createLabelEditColumnComposite(c);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        IValueSet valueSet = configElement.getValueSet();
        createControlsForDefaultValue(workArea, valueSet);

        Control valueSetControl = createValueSetControl(workArea);
        GridData valueSetGridData = new GridData(GridData.FILL_BOTH);
        valueSetGridData.horizontalSpan = 2;
        valueSetControl.setLayoutData(valueSetGridData);
        valueSetControl.setEnabled(!viewOnly);
        return c;
    }

    private void createControlsForDefaultValue(Composite workArea, IValueSet valueSet) {
        uiToolkit.createFormLabel(workArea, Messages.PolicyAttributeEditDialog_defaultValue);
        if (valueSet.isEnum()) {
            Combo combo = uiToolkit.createCombo(workArea);
            defaultValueField = new EnumValueSetField(combo, (IEnumValueSet)valueSet, valueDatatype);
        } else if (valueDatatype.isEnum()) {
            Combo combo = uiToolkit.createCombo(workArea);
            defaultValueField = new EnumDatatypeField(combo, (EnumDatatype)valueDatatype);
        } else {
            Text defaultValueText = uiToolkit.createText(workArea);
            defaultValueText.setEnabled(!viewOnly);
            defaultValueField = new TextField(defaultValueText);
        }
    }

    private Composite createValueSetControl(Composite workArea) {
        ((GridData)workArea.getLayoutData()).heightHint = 250;
        IpsObjectUIController uiController = new IpsObjectUIController(configElement);
        Composite vsEditComposite = uiToolkit.createComposite(workArea);
        vsEditComposite.setLayout(uiToolkit.createNoMarginGridLayout(1, true));
        vsEditComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        ((GridData)vsEditComposite.getLayoutData()).horizontalSpan = 2;
        ValueSetSpecificationControl vsEdit = new ValueSetSpecificationControl(vsEditComposite, uiToolkit,
                uiController, configElement, allowedValuesSetTypes, new Validator(),
                ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
        vsEdit.setAllowedValueSetTypes(allowedValuesSetTypes);
        return vsEdit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void connectToModel() {
        super.connectToModel();
        uiController.add(defaultValueField, IConfigElement.PROPERTY_VALUE);
    }

    class Chooser extends EnumValueSetChooser {

        public Chooser(Composite parent, UIToolkit toolkit, IEnumValueSet source, IEnumValueSet target,
                EnumDatatype type, DefaultUIController uiController) {
            super(parent, toolkit, source, target, type, uiController);
        }

        @Override
        public MessageList getMessagesForValue(String valueId) {
            MessageList list = new MessageList();
            if (getSourceValueSet().containsValue(valueId)) {
                return list;
            }
            String text = NLS.bind(Messages.DefaultsAndRangesEditDialog_valueNotContainedInValueSet, valueId,
                    getSourceValueSet().toShortString());
            list.add(new Message("", text, Message.ERROR)); //$NON-NLS-1$
            return list;
        }

    }

    private class Validator implements TableElementValidator {

        public MessageList validate(String element) {
            MessageList list;
            try {
                list = configElement.validate(configElement.getIpsProject());
                return list.getMessagesFor(element);
            } catch (CoreException e) {
                IpsPlugin.log(e);
                return new MessageList();
            }
        }
    }
}
