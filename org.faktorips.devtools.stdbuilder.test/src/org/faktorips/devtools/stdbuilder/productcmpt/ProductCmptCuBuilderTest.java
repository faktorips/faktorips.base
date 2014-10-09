/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.productcmpt;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.ProductCmptClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProductCmptCuBuilderTest extends AbstractStdBuilderTest {

    private static final String PRODUCT_CMPT_NAME = "Product";

    private static final String PRODUCT_CMPT_TYPE_NAME = "ProductType";

    private ProductCmptCuBuilder builder;

    private IProductCmpt productCmpt;

    private ProductCmptType productCmptType;

    @Mock
    private ProductCmptClassBuilder prodCmptImplBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmptType = newProductCmptType(ipsProject, PRODUCT_CMPT_TYPE_NAME);
        productCmpt = newProductCmpt(productCmptType, PRODUCT_CMPT_NAME);
        IIpsSrcFile ipsSrcFile = productCmptType.getIpsSrcFile();
        when(prodCmptImplBuilder.getQualifiedClassName(ipsSrcFile)).thenReturn("productIpsSrcFile");

        builder = new ProductCmptCuBuilder(builderSet);
        builder.setProductCmptImplBuilder(prodCmptImplBuilder);

    }

    @Test
    public void testGetGeneratedJavaElements() throws CoreException {
        generatedJavaElements = builder.getGeneratedJavaElements(productCmpt);
        String qualifiedClassNameOfProductCmpt = builder.getQualifiedClassNameOfProductCmpt(productCmpt);
        assertTrue(qualifiedClassNameOfProductCmpt.contains(PRODUCT_CMPT_NAME));

    }

}
