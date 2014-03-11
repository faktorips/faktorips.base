/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A composite that allows to enter the Version of an {@link IpsObjectPartContainer}. Until now it
 * only can be entered since when this version is available.
 * 
 */
public final class VersionsComposite {

    private final UIToolkit toolkit;

    private Composite parent;

    public VersionsComposite(Composite parent, UIToolkit toolkit) {
        this.parent = parent;
        this.toolkit = toolkit;
        createLayout();
    }

    private void createLayout() {
        Composite composite = toolkit.createGridComposite(parent, 2, true, true);
        Composite sinceVersionComposite = toolkit.createGridComposite(composite, 2, false, true);
        Label label = toolkit.createFormLabel(sinceVersionComposite, Messages.IpsPartEditDialog_versionAvailableSince);
        GridData grid = new GridData(SWT.HORIZONTAL, SWT.VERTICAL, true, false);
        label.setLayoutData(grid);

        Text text = toolkit.createText(sinceVersionComposite);
        text.setToolTipText(Messages.IpsPartEditDialog_versionTooltip);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }
}
