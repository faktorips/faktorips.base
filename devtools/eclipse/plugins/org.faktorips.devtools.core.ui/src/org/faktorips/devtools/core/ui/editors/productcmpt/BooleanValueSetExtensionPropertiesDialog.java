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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.faktorips.devtools.core.ui.ExtensionPropertyControlFactory;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.extproperties.IExtensionPropertyDefinition;
import org.faktorips.devtools.model.productcmpt.IConfiguredValueSet;

/**
 * A dialog that shows the extension properties registered for {@link IConfiguredValueSet}. The
 * boolean value set itself is always edited inline via {@link BooleanValueSetControl}.
 */
public class BooleanValueSetExtensionPropertiesDialog extends IpsPartEditDialog2 {

    private static final String SETTINGS_KEY_PREFIX = "BooleanValueSetExtensionPropertiesDialog_"; //$NON-NLS-1$

    private static final int INITIAL_WIDTH = 450;

    private static final int INITIAL_HEIGHT = 200;

    private final IConfiguredValueSet configuredValueSet;

    private final ExtensionPropertyControlFactory extPropControlFactory;

    public BooleanValueSetExtensionPropertiesDialog(IConfiguredValueSet configuredValueSet, Shell parentShell) {
        super(configuredValueSet, parentShell, Messages.BooleanValueSetExtensionPropertiesDialog_title, true);
        this.configuredValueSet = configuredValueSet;
        enableDialogSizePersistence(SETTINGS_KEY_PREFIX, configuredValueSet.getPropertyName(), new Point(
                INITIAL_WIDTH, INITIAL_HEIGHT), null);
        extPropControlFactory = new ExtensionPropertyControlFactory(configuredValueSet);
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        TabFolder folder = (TabFolder)parent;
        TabItem firstPage = new TabItem(folder, SWT.NONE);
        firstPage.setText(Messages.PolicyAttributeEditDialog_properties);
        firstPage.setControl(createFirstPage(folder));

        return folder;
    }

    @Override
    public void create() {
        super.create();
        Shell shell = getShell();
        Point currentSize = shell.getSize();
        Point preferredSize = shell.computeSize(currentSize.x, SWT.DEFAULT, true);
        if (preferredSize.y > currentSize.y) {
            shell.setSize(currentSize.x, preferredSize.y);
        }
    }

    private Control createFirstPage(TabFolder folder) {
        Composite c = createTabItemComposite(folder, 1, false);
        extPropControlFactory.createControls(c, getToolkit(), configuredValueSet,
                IExtensionPropertyDefinition.POSITION_TOP);
        extPropControlFactory.createControls(c, getToolkit(), configuredValueSet,
                IExtensionPropertyDefinition.POSITION_BOTTOM);
        extPropControlFactory.bind(getBindingContext());
        return c;
    }
}
