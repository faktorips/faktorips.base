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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * 
 * @author Jan Ortmann
 */
class ProductCmptTypeStructurePage extends ProductCmptTypeEditorPage {

    /**
     * Creates a new <code>PolicyCmptTypeStructurePage</code>.
     * 
     * @param editor The editor to which the new page belongs to.
     */
    public ProductCmptTypeStructurePage(ProductCmptTypeEditor editor, boolean twoSectionsWhenTrueOtherwiseFour) {
        super(editor, twoSectionsWhenTrueOtherwiseFour, Messages.StructurePage_structurePageTitle,
                "ProductCmptTypeStructurePage"); //$NON-NLS-1$
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected void createGeneralPageInfoSection(Composite formBody, UIToolkit toolkit) {
        new GeneralInfoSection(getProductCmptType(), formBody, toolkit);
    }

    @Override
    protected void createContentForSingleStructurePage(Composite formBody, UIToolkit toolkit) {
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new ProductCmptTypeAttributesSection(getProductCmptType(), members, getSite(), toolkit);
        new ProductCmptTypeAssociationsSection(getProductCmptType(), members, getSite(), toolkit);
        new ProductCmptTypeMethodsSection(getProductCmptType(), members, getSite(), toolkit);
        new TableStructureUsageSection(getProductCmptType(), members, getSite(), toolkit);
    }

    @Override
    protected void createContentForSplittedStructurePage(Composite formBody, UIToolkit toolkit) {
        Composite members = createGridComposite(toolkit, formBody, 2, true, GridData.FILL_BOTH);
        new ProductCmptTypeAttributesSection(getProductCmptType(), members, getSite(), toolkit);
        new ProductCmptTypeAssociationsSection(getProductCmptType(), members, getSite(), toolkit);
    }

}
