/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

    public final static String PAGE_ID = "UnreachableFile"; //$NON-NLS-1$

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
