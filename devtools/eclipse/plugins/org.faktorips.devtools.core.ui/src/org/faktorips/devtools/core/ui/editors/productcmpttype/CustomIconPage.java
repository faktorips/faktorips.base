/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.core.ui.editors.type.TypeEditor;
import org.faktorips.devtools.model.type.IType;

public class CustomIconPage extends IpsObjectEditorPage {

    static final String PAGE_ID = "CustomIconPage"; //$NON-NLS-1$

    public CustomIconPage(TypeEditor editor) {
        super(editor, PAGE_ID, Messages.CustomIconPage_Page_Tab_Label);
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        formBody.setLayout(createPageLayout(1, true));
        new CustomIconSection((IType)getIpsObject(), formBody, toolkit);
    }

}
