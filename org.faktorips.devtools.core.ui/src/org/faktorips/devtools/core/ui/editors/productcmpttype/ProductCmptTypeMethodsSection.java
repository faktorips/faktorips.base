/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.editors.EditDialog;
import org.faktorips.devtools.core.ui.editors.IpsPartsComposite;
import org.faktorips.devtools.core.ui.editors.type.MethodsSection;

/**
 * @author Jan Ortmann
 */
public class ProductCmptTypeMethodsSection extends MethodsSection {

    public ProductCmptTypeMethodsSection(IProductCmptType type, Composite parent, IWorkbenchPartSite site,
            UIToolkit toolkit) {

        super(type, parent, site, toolkit);
        setText(Messages.ProductCmptTypeMethodsSection_title);
    }

    @Override
    protected IpsPartsComposite createIpsPartsComposite(Composite parent, UIToolkit toolkit) {
        return new ProductCmptTypeMethodComposite(getType(), parent, getSite(), toolkit);
    }

    private static class ProductCmptTypeMethodComposite extends MethodsComposite {

        public ProductCmptTypeMethodComposite(IType type, Composite parent, IWorkbenchPartSite site, UIToolkit toolkit) {
            super(type, parent, site, toolkit);
        }

        @Override
        protected EditDialog createEditDialog(IIpsObjectPart part, Shell shell) {
            return new ProductCmptTypeMethodEditDialog((IProductCmptTypeMethod)part, shell);
        }
    }

}
