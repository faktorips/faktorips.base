/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.junit.Test;
import org.mockito.Mockito;

public class ProductCmptGenerationClassBuilderTest {

    @Test
    public void testGetTemplate_exists() throws Exception {
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);
        String template = policyCmptClassBuilder.getTemplate();
        int lastIndexOf = template.lastIndexOf(SyntaxConstants.NS_DELIM);
        template = template.substring(0, lastIndexOf);
        String templatePath = template.replaceAll(SyntaxConstants.NS_DELIM, "/") + ".xpt";
        URL resource = PolicyCmptClassBuilder.class.getClassLoader().getResource(templatePath);
        assertNotNull(resource);
    }

    @Test
    public void testIsBuilderFor_notGenerateGenerationClassIfProductCpmtTypeIsNotChangingOverTime() throws Exception {
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(productCmptType.isChangingOverTime()).thenReturn(false);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, Mockito.RETURNS_DEEP_STUBS);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmptType);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);

        ProductCmptGenerationClassBuilder productCmptClassBuilder = new ProductCmptGenerationClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);

        ProductCmptGenerationClassBuilder spy = spy(productCmptClassBuilder);
        assertFalse(spy.isBuilderFor(ipsSrcFile));
        verify(spy).delete(ipsSrcFile);
    }

    @Test
    public void testIsBuilderFor_generateGenerationClassIfProductCpmtTypeIsChangingOverTime() throws Exception {
        IProductCmptType productCmptType = mock(IProductCmptType.class);
        when(productCmptType.isChangingOverTime()).thenReturn(true);
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, Mockito.RETURNS_DEEP_STUBS);
        when(ipsSrcFile.getIpsObject()).thenReturn(productCmptType);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);

        ProductCmptGenerationClassBuilder productCmptClassBuilder = new ProductCmptGenerationClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);

        ProductCmptGenerationClassBuilder spy = spy(productCmptClassBuilder);

        assertTrue(spy.isBuilderFor(ipsSrcFile));
        verify(spy, never()).delete(ipsSrcFile);
    }

}
