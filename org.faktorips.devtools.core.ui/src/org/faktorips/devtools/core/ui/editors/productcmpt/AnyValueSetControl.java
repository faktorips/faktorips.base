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

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.util.memento.Memento;

/**
 * Control to define any kind of value set in the product component editor. A text field followed by
 * a button is provided. If the button is clicked, a special dialog to define the value set opens.
 * In the dialog it is also possible to switch the type of value set!
 * 
 * @see AnyValueSetEditDialog
 * 
 * @author Thorsten Guenther
 * @author Jan Ortmann
 */
public class AnyValueSetControl extends TextButtonControl implements IDataChangeableReadWriteAccess {

    /** The {@link IConfiguredValueSet} that owns the value set being shown and edited */
    private IConfiguredValueSet configValueSet;

    /** The shell to details dialog within. */
    private Shell shell;

    /**
     * The state of the config element before opening the detail edit dialog. Used to handle the
     * cancel button properly
     */
    private Memento state;

    /**
     * The state of the ips source file before opening the detail edit dialog. Used to handle the
     * cancel button properly
     */
    private boolean dirty;

    /** true if the value set can be edited, false if it read-only. */
    private boolean dataChangeable = true;

    /**
     * Provider for source and target enum value set.
     */
    private IEnumValueSetProvider enumValueSetProvider;

    /**
     * Creates a new control to show and edit the value set owned by the config element.
     * 
     * @param parent The parent composite to add this control to.
     * @param toolkit The toolkit used to create controls.
     * @param configuredValueSet The {@link IConfiguredValueSet} that contains the value set.
     * @param shell The shell to open the details edit dialog within.
     */
    public AnyValueSetControl(Composite parent, UIToolkit toolkit, IConfiguredValueSet configuredValueSet,
            Shell shell) {
        super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
        this.configValueSet = configuredValueSet;
        this.shell = shell;
        getTextControl().setEditable(true);
        setEnumValueSetProvider(new DefaultEnumValueSetProvider(configuredValueSet));
    }

    @Override
    protected void buttonClicked() {
        preserveState();
        try {
            IpsPartEditDialog2 dialog = createEnumSubsetDialogIfApplicable();
            if (dialog == null) {
                List<ValueSetType> valueSetTypes = configValueSet.getAllowedValueSetTypes(getIpsProject());
                dialog = new AnyValueSetEditDialog(configValueSet, valueSetTypes, shell, !dataChangeable);
            }
            dialog.setDataChangeable(isDataChangeable());
            if (dialog.open() == Window.OK) {
                String formattedValue = IpsUIPlugin.getDefault().getDatatypeFormatter()
                        .formatValueSet(configValueSet.getValueSet());
                getTextControl().setText(formattedValue);
            } else {
                resetState();
            }
        } catch (CoreRuntimeException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private ValueDatatype getValueDatatype(IPolicyCmptTypeAttribute attribute) {
        ValueDatatype datatype = null;
        if (attribute != null) {
            datatype = attribute.findDatatype(getIpsProject());
        }
        if (datatype == null) {
            return Datatype.STRING;
        }
        return datatype;
    }

    private IIpsProject getIpsProject() {
        return configValueSet.getIpsProject();
    }

    private IpsPartEditDialog2 createEnumSubsetDialogIfApplicable() throws CoreRuntimeException {
        IPolicyCmptTypeAttribute attribute = configValueSet.findPcTypeAttribute(getIpsProject());
        ValueDatatype datatype = getValueDatatype(attribute);

        if (attribute == null) {
            return null;
        }
        if (!attribute.getValueSet().isEnum()) {
            return null;
        }
        if (!configValueSet.getValueSet().isEnum()) {
            return null;
        }
        if (attribute.getValueSet().canBeUsedAsSupersetForAnotherEnumValueSet() || datatype.isEnum()) {
            return new EnumSubsetEditDialog(getEnumValueSetProvider(), datatype, shell, !dataChangeable);
        }
        return null;
    }

    /**
     * Stores the current state of the config element and the underlying sourcefile for later
     * recovery.
     */
    private void preserveState() {
        dirty = configValueSet.getIpsObject().getIpsSrcFile().isDirty();
        state = configValueSet.newMemento();
    }

    /**
     * Recovers an old state preserved by calling preserveState. If no state was preserved before
     * calling this method, nothing is done.
     */
    private void resetState() {
        if (state == null) {
            // no state was preserved, so dont do anything.
            return;
        }
        configValueSet.setState(state);
        if (!dirty) {
            configValueSet.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

    @Override
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        getTextControl().setEditable(dataChangeable);
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

}
