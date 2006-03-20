/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;


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

    private IIpsSrcFile missing;
	
	/**
	 * Creates a new saying that the given file is missing.
	 * 
	 * @param editor The owner of this page
	 */
    public MissingResourcePage(IpsObjectEditor editor, IIpsSrcFile missing) {
        super(editor, PAGE_ID, ""); //$NON-NLS-1$
        this.missing = missing;
    }
    
    /**
     * {@inheritDoc}
     */
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
    	Composite root = new Composite(formBody, SWT.NONE);
    	String filename = missing==null?"null":missing.getName();
    	toolkit.createLabel(root, NLS.bind(Messages.MissingResourcePage_msgFileOutOfSync, filename));
    }
}
