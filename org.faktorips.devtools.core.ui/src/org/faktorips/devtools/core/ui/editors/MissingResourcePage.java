/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.productcmpt.Messages;

/**
 * Page to display the information that the resource is missing.
 * 
 * @author Thorsten Guenther
 */
public class MissingResourcePage extends IpsObjectEditorPage {

    /**
     * Id to identify the page.
     */
    public final static String PAGE_ID = "MissingResource"; //$NON-NLS-1$

    /**
     * Creates a new saying that the given file is missing.
     * 
     * @param editor The owner of this page
     */
    public MissingResourcePage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, ""); //$NON-NLS-1$
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        Composite root = new Composite(formBody, SWT.NONE);
        IIpsSrcFile file = getIpsObjectEditor().getIpsSrcFile();
        String filename = file == null ? "null" : file.getName(); //$NON-NLS-1$
        toolkit.createLabel(root, NLS.bind(Messages.MissingResourcePage_msgFileOutOfSync, filename));
    }
}
