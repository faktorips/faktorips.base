/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.List;

import org.faktorips.devtools.core.internal.model.type.CommonTypeFinder;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

public class InferTemplatePmo extends NewProductCmptPMO {

    private List<IProductCmpt> productCmptsToInferTemplate;

    public InferTemplatePmo() {
        super(true);
    }

    public List<IProductCmpt> getProductCmptsToInferTemplate() {
        return productCmptsToInferTemplate;
    }

    public void setProductCmptsToInferTemplate(List<IProductCmpt> selectedProductCmpts) {
        this.productCmptsToInferTemplate = selectedProductCmpts;
        IProductCmptType commonTypeOf = CommonTypeFinder.commonTypeOf(selectedProductCmpts);
        setSingleProductCmptType(commonTypeOf);
    }

}
