/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.controls.chooser.MultiValueSubsetChooserModel;
import org.faktorips.devtools.core.ui.controls.chooser.SubsetChooserViewer;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

public class MultiValueSubsetDialog extends IpsPartEditDialog2 {

    private final MultiValueSubsetChooserModel model;

    public MultiValueSubsetDialog(Shell parentShell, IAttributeValue attributeValue, MultiValueSubsetChooserModel model) {
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
