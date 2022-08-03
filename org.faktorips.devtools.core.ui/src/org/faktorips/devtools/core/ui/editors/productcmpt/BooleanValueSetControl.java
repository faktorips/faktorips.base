/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIDatatypeFormatter;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.core.ui.controls.ControlComposite;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.IValueSet;

/**
 * Control to define a boolean value set in the product component editor. Three check boxes are
 * provided, allowing the user to select which of the boolean values (true, false, and null), are
 * allowed.
 */
public class BooleanValueSetControl extends ControlComposite implements IDataChangeableReadWriteAccess {

    /** true if the value set can be edited, false if it read-only. */
    private boolean dataChangeable = true;

    /**
     * Provider for source and target enum value set.
     */
    private IEnumValueSetProvider enumValueSetProvider;

    private Checkbox trueBox;
    private Checkbox falseBox;
    private Checkbox nullBox;

    private final IPolicyCmptTypeAttribute attribute;

    private UIToolkit toolkit;

    /**
     * Creates a new control to show and edit the value set owned by the {@link IConfigElement}.
     * 
     * @param parent The parent composite to add this control to.
     * @param toolkit The toolkit used to create controls.
     * @param configuredValueSet The {@link IConfiguredValueSet} that contains the value set.
     */
    public BooleanValueSetControl(Composite parent, UIToolkit toolkit, IPolicyCmptTypeAttribute attribute,
            IConfiguredValueSet configuredValueSet) {
        super(parent, SWT.NONE);
        this.toolkit = toolkit;
        this.attribute = attribute;

        setEnumValueSetProvider(new DefaultEnumValueSetProvider(configuredValueSet));
        initControls();

    }

    private void initControls() {
        UIDatatypeFormatter datatypeFormatter = IpsUIPlugin.getDefault().getDatatypeFormatter();
        ValueDatatype valueDatatype = getDatatype(attribute);
        int components = 2;
        trueBox = toolkit.createCheckbox(this, datatypeFormatter.formatValue(valueDatatype, Boolean.TRUE.toString()));
        falseBox = toolkit.createCheckbox(this, datatypeFormatter.formatValue(valueDatatype, Boolean.FALSE.toString()));
        if (valueDatatype != null && !valueDatatype.isPrimitive()) {
            nullBox = toolkit.createCheckbox(this, IpsPlugin.getDefault().getIpsPreferences().getNullPresentation());
            components++;
        }
        initLayout(components);
    }

    private void initLayout(int components) {
        GridLayout layout = new GridLayout(components, false);
        layout.horizontalSpacing = 20;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);

        setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    }

    public ValueDatatype getDatatype(IPolicyCmptTypeAttribute attribute) {
        return attribute.findDatatype(attribute.getIpsProject());
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        updateEnabledState();
    }

    public void updateEnabledState() {
        updateEnabledState(trueBox, Boolean.TRUE.toString());
        updateEnabledState(falseBox, Boolean.FALSE.toString());
        if (nullBox != null) {
            updateEnabledState(nullBox, null);
        }
    }

    private void updateEnabledState(Checkbox checkbox, String valueId) {
        toolkit.setDataChangeable(checkbox, dataChangeable && isCheckedOrValueAvailable(checkbox, valueId));
    }

    /**
     * Returns <code>true</code> if the check box is checked, and/or if the respective value is
     * available. Enabling selected check boxes, even if the value is illegal/unavailable, is
     * necessary to let the user correct errors.
     */
    private boolean isCheckedOrValueAvailable(Checkbox checkbox, String valueID) {
        return checkbox.isChecked() || isValueAvailable(valueID);
    }

    private boolean isValueAvailable(String valueID) {
        IValueSet valueSet = attribute.getValueSet();
        return valueSet.containsValue(valueID, attribute.getIpsProject());
    }

    @Override
    public boolean isDataChangeable() {
        return dataChangeable;
    }

    public void setEnumValueSetProvider(IEnumValueSetProvider enumValueSetProvider) {
        this.enumValueSetProvider = enumValueSetProvider;
    }

    public IEnumValueSetProvider getEnumValueSetProvider() {
        return enumValueSetProvider;
    }

    public Checkbox getTrueCheckBox() {
        return trueBox;
    }

    public Checkbox getFalseCheckBox() {
        return falseBox;
    }

    public Checkbox getNullCheckBox() {
        return nullBox;
    }

}
