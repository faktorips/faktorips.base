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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.DefaultUIController;
import org.faktorips.devtools.core.ui.controller.IpsObjectUIController;
import org.faktorips.devtools.core.ui.controls.valuesets.EnumSubsetChooser;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * A dialog that allows to edit any kind of value set. If is also possible to switch the type of
 * value set! The value set types that are allowed are passed in the constructor. This dialog works
 * together with {@link AnyValueSetEditDialog}.
 */
public class AnyValueSetEditDialog extends IpsPartEditDialog {

    // The config element that owns the value set being shown/edited.
    private IConfigElement configElement;

    // The value datatype values in the value set are "instances" of.
    private ValueDatatype valueDatatype;

    // list of value set types the users can select
    private List<ValueSetType> allowedValuesSetTypes;

    // true if the dialog is used to just display the value set, no editing is possible
    private boolean viewOnly;

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
        // createControlsForDefaultValue(c);
        createValueSetControl(c);
        return c;
    }

    private Composite createValueSetControl(Composite parent) {
        IpsObjectUIController uiController = new IpsObjectUIController(configElement);
        ValueSetSpecificationControl vsEdit = new ValueSetSpecificationControl(parent, uiToolkit, uiController,
                configElement, allowedValuesSetTypes, ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
        vsEdit.setAllowedValueSetTypes(allowedValuesSetTypes);
        vsEdit.setEnabled(!viewOnly);
        Object layoutData = vsEdit.getLayoutData();
        if (layoutData instanceof GridData) {
            // set the minimum height to show at least the maximum size of the selected
            // ValueSetEditControl
            GridData gd = (GridData)layoutData;
            gd.heightHint = 250;
        }
        return vsEdit;
    }

    class Chooser extends EnumSubsetChooser {

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

}
