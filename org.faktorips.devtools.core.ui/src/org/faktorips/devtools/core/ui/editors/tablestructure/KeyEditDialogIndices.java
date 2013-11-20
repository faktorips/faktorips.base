/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.tablestructure;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.ui.controls.Checkbox;

/**
 * A dialog to edit indices.
 */
public class KeyEditDialogIndices extends KeyEditDialog {

    private IKey key;
    private Checkbox checkbox;

    public KeyEditDialogIndices(IKey key, Shell parentShell) {
        super(key, parentShell, Messages.KeyEditDialogIndices_titleText);
        this.key = key;
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
        getBindingContext().bindContent(checkbox, key, IIndex.PROPERTY_UNIQUE_KEY);
    }
}
