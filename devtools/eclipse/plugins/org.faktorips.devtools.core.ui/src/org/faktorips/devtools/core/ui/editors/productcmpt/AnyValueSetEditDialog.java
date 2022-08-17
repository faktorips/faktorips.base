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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetControlEditMode;
import org.faktorips.devtools.core.ui.controls.valuesets.ValueSetSpecificationControl;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * A dialog that allows to edit any kind of value set. If is also possible to switch the type of
 * value set! The value set types that are allowed are passed in the constructor. This dialog works
 * together with {@link AnyValueSetEditDialog}.
 */
public class AnyValueSetEditDialog extends IpsPartEditDialog2 {
    /**
     * Prefix to store the settings (size/position) of this dialog, used together with the
     * attribute.
     */
    private static final String SETTINGS_KEY_PREFIX = "AnyValueSetEditDialog_"; //$NON-NLS-1$

    /** Initial width of the dialog. */
    private static final int INITIAL_WIDTH = 650;

    /** Initial height of the dialog. */
    private static final int INITIAL_HEIGHT = 550;

    /** The {@link IConfiguredValueSet} that owns the value set being shown/edited. */
    private IConfiguredValueSet configuredValueSet;

    /** list of value set types the users can select */
    private List<ValueSetType> allowedValuesSetTypes;

    /** true if the dialog is used to just display the value set, no editing is possible */
    private boolean viewOnly;

    public AnyValueSetEditDialog(IConfiguredValueSet configuredValueSet, List<ValueSetType> allowedTypes,
            Shell parentShell) {
        this(configuredValueSet, allowedTypes, parentShell, false);
    }

    public AnyValueSetEditDialog(IConfiguredValueSet configuredValueSet, List<ValueSetType> allowedTypes,
            Shell parentShell, boolean viewOnly) {

        super(configuredValueSet, parentShell, Messages.PolicyAttributeEditDialog_editLabel, true);
        this.configuredValueSet = configuredValueSet;
        this.viewOnly = viewOnly;
        allowedValuesSetTypes = allowedTypes;
        enableDialogSizePersistence(SETTINGS_KEY_PREFIX, configuredValueSet.getPropertyName(), new Point(INITIAL_WIDTH,
                INITIAL_HEIGHT), null);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        return folder;
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        createValueSetControl(c);
        return c;
    }

    private Composite createValueSetControl(Composite parent) {
        ValueSetSpecificationControl vsEdit = new ValueSetSpecificationControl(parent, getToolkit(),
                getBindingContext(), configuredValueSet, allowedValuesSetTypes,
                ValueSetControlEditMode.ONLY_NONE_ABSTRACT_SETS);
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
}
