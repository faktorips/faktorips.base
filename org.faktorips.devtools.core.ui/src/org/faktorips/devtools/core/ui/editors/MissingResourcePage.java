/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
