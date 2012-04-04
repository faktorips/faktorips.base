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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.ui.controls.tableedit.EditTableControlFactory;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;

public class MultiValueDialog extends IpsPartEditDialog2 {

    private final IAttributeValue attributeValue;
    private MultiValueLableModel tabelModel;
    private ValueDatatype datatype;

    public MultiValueDialog(Shell parentShell, IAttributeValue attributeValue) {
        super(attributeValue, parentShell, Messages.MultiValueDialog_TitleText);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        Assert.isNotNull(attributeValue);
        this.attributeValue = attributeValue;
        tabelModel = new MultiValueLableModel(attributeValue);
        try {
            IIpsProject ipsProject = attributeValue.getIpsProject();
            datatype = attributeValue.findAttribute(ipsProject).findValueDatatype(ipsProject);
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    protected void okPressed() {
        tabelModel.applyValueList();
        super.okPressed();
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        String description = NLS.bind(Messages.MultiValueDialog_TableDescription, attributeValue.getAttribute());
        EditTableControlFactory.createListEditTable(getToolkit(), parent, attributeValue.getIpsProject(), datatype,
                tabelModel, new MultiValueElementModifier(), description);
        ((GridData)parent.getLayoutData()).heightHint = 300;
        return parent;
    }

}
