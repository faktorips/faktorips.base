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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;

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
