/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.productcmpt;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.CommonTypeFinder;

public class InferTemplatePmo extends NewProductCmptPMO {

    private List<IProductCmpt> productCmptsToInferTemplate;

    private final NewProductCmptValidator validator;

    public InferTemplatePmo() {
        super(true);
        validator = new InferTemplateValidator(this);
    }

    @Override
    protected NewProductCmptValidator getValidator() {
        return validator;
    }

    public List<IProductCmpt> getProductCmptsToInferTemplate() {
        return productCmptsToInferTemplate;
    }

    public void setProductCmptsToInferTemplateFrom(List<IProductCmpt> selectedProductCmpts) {
        productCmptsToInferTemplate = selectedProductCmpts;
        IProductCmptType commonTypeOf = CommonTypeFinder.commonTypeOf(selectedProductCmpts);
        setSingleProductCmptType(commonTypeOf);
        setEffectiveDate(getEarliestValidFrom());
        setKindId(getCommonProductCmptNamePrefix());
    }

    public GregorianCalendar getEarliestValidFrom() {
        if (productCmptsToInferTemplate.isEmpty()) {
            return null;
        }
        Iterator<IProductCmpt> iterator = productCmptsToInferTemplate.iterator();
        GregorianCalendar firstValidFrom = iterator.next().getValidFrom();
        while (iterator.hasNext()) {
            IProductCmpt productCmpt = iterator.next();
            if (productCmpt.getValidFrom().before(firstValidFrom)) {
                firstValidFrom = productCmpt.getValidFrom();
            }
        }
        return firstValidFrom;
    }

    private String getCommonProductCmptNamePrefix() {
        String[] versionIds = productCmptsToInferTemplate.stream().map(cmpt -> {
            assert cmpt != null;
            return cmpt.getKindId().getName();
        }).toArray(String[]::new);

        return StringUtils.getCommonPrefix(versionIds);
    }

}
