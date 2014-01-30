/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;

/**
 * A page to display the generations.
 */
public class ProductCmptPropertiesPage extends IpsObjectEditorPage {

    final static String PAGE_ID = "PolicyCmpt"; //$NON-NLS-1$

    public ProductCmptPropertiesPage(IpsObjectEditor editor) {
        super(editor, PAGE_ID, Messages.ProductCmptPropertiesPage_pageTitle);
    }

    /**
     * Get owning editor.
     */
    ProductCmptEditor getProductCmptEditor() {
        return (ProductCmptEditor)getEditor();
    }

    /**
     * Get Product which is parent of the generations
     */
    IProductCmpt getProductCmpt() {
        return getProductCmptEditor().getProductCmpt();
    }

    @Override
    protected void createPageContent(Composite formBody, UIToolkit toolkit) {
        GridLayout layout = new GridLayout(1, true);
        layout.verticalSpacing = VERTICAL_SECTION_SPACE;
        layout.horizontalSpacing = HORIZONTAL_SECTION_SPACE;
        formBody.setLayout(layout);

        Composite top = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new ComponentPropertiesSection(getProductCmpt(), top, toolkit, getProductCmptEditor());
        new GenerationsSection(this, top, toolkit);
    }

    // Made public to get refresh from editor.
    @Override
    public void refresh() {
        super.refresh();
    }
}
