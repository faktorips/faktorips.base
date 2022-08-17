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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.ui.DefaultLabelProvider;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.AttributesSection;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * @author Jan Ortmann
 */
public class ProductCmptTypeAttributesSection extends AttributesSection {

    private ProductCmptTypeAttributesComposite attributesComposite;

    public ProductCmptTypeAttributesSection(IProductCmptType productCmptType, Composite parent,
            IWorkbenchPartSite site, UIToolkit toolkit) {

        super(productCmptType, parent, site, toolkit);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        attributesComposite = new ProductCmptTypeAttributesComposite(getProductCmptType(), parent, getSite(), toolkit);
        return attributesComposite;
    }

    private IProductCmptType getProductCmptType() {
        return (IProductCmptType)getType();
    }

    @Override
    protected void performRefresh() {
        super.performRefresh();
    }

    private static class ProductCmptTypeAttributesComposite extends AttributesComposite {

        public ProductCmptTypeAttributesComposite(IProductCmptType productCmptType, Composite parent,
                IWorkbenchPartSite site, UIToolkit toolkit) {
            super(productCmptType, parent, site, toolkit);
        }

        @Override
        protected ILabelProvider createLabelProvider() {
            return new DefaultLabelProvider();
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new AttributeEditDialog((IProductCmptTypeAttribute)part, shell);
        }

    }

}
