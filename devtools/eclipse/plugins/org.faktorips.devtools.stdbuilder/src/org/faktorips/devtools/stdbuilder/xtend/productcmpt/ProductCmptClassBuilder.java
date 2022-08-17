/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xtend.productcmpt;

import org.faktorips.datatype.util.LocalizedStringsSet;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentInterfaceTmpl;
import org.faktorips.devtools.stdbuilder.xtend.productcmpt.template.ProductComponentTmpl;

public class ProductCmptClassBuilder extends ProductClassBuilder<XProductCmptClass> {

    public ProductCmptClassBuilder(boolean interfaceBuilder, StandardBuilderSet builderSet,
            GeneratorModelContext modelContext, ModelService modelService) {
        super(interfaceBuilder, builderSet, modelContext, modelService,
                new LocalizedStringsSet(ProductCmptClassBuilder.class));
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
        return IpsObjectType.PRODUCT_CMPT_TYPE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected Class<XProductCmptClass> getGeneratorModelRootType() {
        return XProductCmptClass.class;
    }

    @Override
    protected String generateBody(IIpsObject ipsObject) {
        if (generatesInterface()) {
            return ProductComponentInterfaceTmpl.body(getGeneratorModelRoot(ipsObject));
        } else {
            return ProductComponentTmpl.body(getGeneratorModelRoot(ipsObject));
        }
    }

}
