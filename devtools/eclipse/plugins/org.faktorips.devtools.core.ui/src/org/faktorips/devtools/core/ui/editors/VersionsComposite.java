/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartContainer;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

/**
 * A composite that allows defining since which version of the model a new
 * {@link IpsObjectPartContainer} is available.
 * 
 */
public final class VersionsComposite {

    private final UIToolkit toolkit;

    private Composite parent;

    private Text text;

    private BindingContext bindingContext;

    private IVersionControlledElement part;

    public VersionsComposite(Composite parent, IVersionControlledElement part, UIToolkit toolkit,
            BindingContext bindingContext) {
        this.parent = parent;
        this.toolkit = toolkit;
        this.part = part;
        this.bindingContext = bindingContext;

        createLayout();
        bind();
    }

    private void createLayout() {
        Composite composite = toolkit.createGridComposite(parent, 2, true, true);
        Composite sinceVersionComposite = toolkit.createGridComposite(composite, 2, false, true);
        Label label = toolkit.createFormLabel(sinceVersionComposite, Messages.IpsPartEditDialog_versionAvailableSince);
        GridData grid = new GridData(SWT.HORIZONTAL, SWT.VERTICAL, true, false);
        label.setLayoutData(grid);

        text = toolkit.createText(sinceVersionComposite);

        text.setToolTipText(Messages.IpsPartEditDialog_versionTooltip);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        toolkit.paintBordersForComposite(sinceVersionComposite);
    }

    private void bind() {
        bindingContext.bindContent(text, part, IVersionControlledElement.PROPERTY_SINCE_VERSION_STRING);
        bindingContext.updateUI();
    }
}
