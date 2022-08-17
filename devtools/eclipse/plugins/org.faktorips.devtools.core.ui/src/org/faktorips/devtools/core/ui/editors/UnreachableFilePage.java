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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * A page to show the contents of an unreachable file.
 * 
 * @author Peter Erzberger
 */
public class UnreachableFilePage extends IpsObjectEditorPage {

    public static final String PAGE_ID = "UnreachableFile"; //$NON-NLS-1$

    public UnreachableFilePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, "UnreachableFile"); //$NON-NLS-1$
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        Composite formBody = form.getBody();
        UIToolkit toolkit = new UIToolkit(managedForm.getToolkit());
        formBody.setLayout(createPageLayout(1, false));
        Label infoLabel = toolkit.createLabel(formBody, Messages.UnreachableFilePage_msgUnreachableFile);
        infoLabel.setForeground(infoLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
    }
}
