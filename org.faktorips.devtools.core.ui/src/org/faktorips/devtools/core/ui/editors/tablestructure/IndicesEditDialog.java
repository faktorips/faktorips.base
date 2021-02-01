/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.ui.controls.Checkbox;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKey;

/**
 * A dialog to edit indices.
 */
public class IndicesEditDialog extends KeyEditDialog {

    private Checkbox checkbox;

    public IndicesEditDialog(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogIndices_titleText);
    }

    @Override
    protected void addPageTopControls(Composite pageComposite) {
        createCheckbox(pageComposite);
        bind();
    }

    protected void createCheckbox(Composite pageComposite) {
        Composite itemEditComposite = getToolkit().createGridComposite(pageComposite, 3, false, false);
        Composite top = getToolkit().createGridComposite(itemEditComposite, 1, true, true);

        checkbox = getToolkit().createCheckbox(top, Messages.KeyEditDialog_checkboxUniqueKey);
    }

    private void bind() {
        getBindingContext().bindContent(checkbox, getIpsPart(), IIndex.PROPERTY_UNIQUE_KEY);
    }
}
