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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.IDataChangeableReadWriteAccess;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.TextButtonControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.memento.Memento;

/**
 * Control to define a value set in the product component editor. A textfeld followed by a button is
 * provided. If the button is clicked, a special editor to define the values is opened.
 * 
 * @author Thorsten Guenther
 * @auhtor Jan Ortmann
 */
public class AnyValueSetControl extends TextButtonControl implements IDataChangeableReadWriteAccess {

    // The config element that owns the value set being shown and edited
    private IConfigElement configElement;

    // The shell to details dialog within.
    private Shell shell;

    // The controller
    private IpsObjectUIController controller;

    // The state of the config element before opening the detail edit dialog. Used to handle the
    // cancel button properly
    private Memento state;

    // The state of the ips source file before opening the detail edit dialog. Used to handle the
    // cancel button properly
    private boolean dirty;

    // true if the value set can be edited, false if it read-only.
    private boolean dataChangeable;

    /**
     * Creates a new control to show and edit the value set owned by the config element.
     * 
     * @param parent The parent composite to add this control to.
     * @param toolkit The toolkit used to create controls.
     * @param configElement The config element that contains the value set.
     * @param shell The shell to open the details edit dialog within.
     * @param controller The controller to notify uppon changes to update the ui.
     */
    public AnyValueSetControl(Composite parent, UIToolkit toolkit, IConfigElement configElement, Shell shell,
            IpsObjectUIController controller) {
        super(parent, toolkit, "...", true, 15); //$NON-NLS-1$
        this.configElement = configElement;
        this.shell = shell;
        this.controller = controller;
        getTextControl().setEditable(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void buttonClicked() {
        preserveState();
        IIpsProject ipsProject = configElement.getIpsProject();
        try {
            // TODO null handling for attribute
            IPolicyCmptTypeAttribute attribute = configElement.findPcTypeAttribute(ipsProject);
            List<ValueSetType> valueSetTypes = configElement.getAllowedValueSetTypes(ipsProject);
            ValueDatatype datatype = attribute.findDatatype(ipsProject);
            IpsPartEditDialog dialog = createEnumSubsetDialogIfApplicable(attribute, datatype);
            if (dialog == null) {
                dialog = new AnyValueSetEditDialog(configElement, datatype, valueSetTypes, shell, !dataChangeable);
            }
            dialog.setDataChangeable(isDataChangeable());
            if (dialog.open() == Window.OK) {
                super.getTextControl().setText(configElement.getValueSet().toShortString());
                controller.updateUI();
            } else {
                resetState();
            }
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
        }
    }

    private IpsPartEditDialog createEnumSubsetDialogIfApplicable(IPolicyCmptTypeAttribute attribute,
            ValueDatatype datatype) {

        if (!attribute.getValueSet().isEnum()) {
            return null;
        }
        if (!configElement.getValueSet().isEnum()) {
            return null;
        }
        if (attribute.getValueSet().canBeUsedAsSupersetForAnotherEnumValueSet()) {
            IEnumValueSet sourceSet = (IEnumValueSet)attribute.getValueSet();
            return new EnumSubsetEditDialog(sourceSet, configElement, datatype, shell, !dataChangeable);
        }
        if (datatype.isEnum()) {
            return new EnumSubsetEditDialog(configElement, (EnumDatatype)datatype, shell, !dataChangeable);
        }
        return null;
    }

    /**
     * Stores the current state of the config element and the underlying sourcefile for later
     * recovery.
     */
    private void preserveState() {
        dirty = configElement.getIpsObject().getIpsSrcFile().isDirty();
        state = configElement.newMemento();
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
        configElement.setState(state);
        if (!dirty) {
            configElement.getIpsObject().getIpsSrcFile().markAsClean();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDataChangeable(boolean changeable) {
        dataChangeable = changeable;
        getTextControl().setEnabled(dataChangeable);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDataChangeable() {
        return dataChangeable;
    }
}
