/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditor;
import org.faktorips.devtools.core.ui.editors.IpsObjectEditorPage;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;

/**
 * A page to display the generations.
 */
public class ProductCmptPropertiesPage extends IpsObjectEditorPage {

    static final String PAGE_ID = "ProductCmpt"; //$NON-NLS-1$

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

        Composite top = createGridComposite(toolkit, formBody, 2, false, GridData.FILL_BOTH);
        new ComponentPropertiesSection(getProductCmpt(), top, toolkit, getProductCmptEditor());
        if (getProductCmpt().allowGenerations()) {
            new GenerationsSection(this, top, toolkit);
        }
    }

    // Made public to get refresh from editor.
    @Override
    public void refresh() {
        super.refresh();
    }
}
