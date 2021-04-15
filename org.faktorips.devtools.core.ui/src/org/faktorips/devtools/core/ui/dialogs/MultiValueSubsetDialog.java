/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controls.chooser.MultiValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.chooser.SubsetChooserViewer;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;

public class MultiValueSubsetDialog extends IpsPartEditDialog2 {

    private final MultiValueSubsetChooserModel model;

    public MultiValueSubsetDialog(Shell parentShell, IAttributeValue attributeValue,
            MultiValueSubsetChooserModel model) {
        super(attributeValue, parentShell, Messages.MultiValueDialog_TitleText);
        this.model = model;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        Assert.isNotNull(attributeValue);
    }

    @Override
    protected void setDataChangeableThis(boolean changeable) {
        /*
         * Do not set data changeable (or unchangeable respectively). This dialog can never be
         * opened in browse mode, the Multi-Value button next to the attribute value's field is
         * disabled in that case.
         */
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        SubsetChooserViewer viewer = new SubsetChooserViewer(parent, getToolkit());
        viewer.init(model);
        return parent;
    }

}
