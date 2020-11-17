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
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class ProductCmptTypeBehaviourPage extends ProductCmptTypeEditorPage {

    public ProductCmptTypeBehaviourPage(ProductCmptTypeEditor editor) {
        super(editor, true, Messages.BehaviourPage_title_behaviour, "PolicyCmptTypeBehaviourPage"); //$NON-NLS-1$
        setNumberlayoutColumns(2);
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getIpsObject();
    }

    @Override
    protected void createContentForSingleStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new ProductCmptTypeMethodsSection(getProductCmptType(), parentContainer, getSite(), toolkit);
        new TableStructureUsageSection(getProductCmptType(), parentContainer, getSite(), toolkit);

    }

    @Override
    protected void createContentForSplittedStructurePage(Composite parentContainer, UIToolkit toolkit) {
        new ProductCmptTypeMethodsSection(getProductCmptType(), parentContainer, getSite(), toolkit);
        new TableStructureUsageSection(getProductCmptType(), parentContainer, getSite(), toolkit);
    }

    @Override
    protected void createGeneralPageInfoSection(Composite parentContainer, UIToolkit toolkit) {
        // Nothing to do.
    }

}
