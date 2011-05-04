/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
