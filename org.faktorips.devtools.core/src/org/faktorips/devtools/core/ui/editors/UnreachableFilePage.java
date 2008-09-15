/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
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

    /**
     * @param editor
     */
    public UnreachableFilePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, "UnreachableFile"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void createFormContent(IManagedForm managedForm) {
        ScrolledForm form = managedForm.getForm();
        Composite formBody = form.getBody();
        UIToolkit toolkit = new UIToolkit(managedForm.getToolkit());
        formBody.setLayout(createPageLayout(1, false));
        Label infoLabel = toolkit.createLabel(formBody, Messages.UnreachableFilePage_msgUnreachableFile);
        infoLabel.setForeground(infoLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
    }
}
