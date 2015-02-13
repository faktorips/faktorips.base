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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.internal.xtend.expression.parser.SyntaxConstants;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.PolicyCmptClassBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ProductCmptGenerationClassBuilderTest {

    private ProductCmptGenerationClassBuilder productCmptGenerationClassBuilder;

    @Before
    public void setUp() {
        productCmptGenerationClassBuilder = new ProductCmptGenerationClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);
    }

    @Test
    public void testGetTemplate_exists() {
        PolicyCmptClassBuilder policyCmptClassBuilder = new PolicyCmptClassBuilder(false,
                mock(StandardBuilderSet.class), mock(GeneratorModelContext.class), null);

        String template = policyCmptClassBuilder.getTemplate();
        template = template.substring(0, template.lastIndexOf(SyntaxConstants.NS_DELIM));
        String templatePath = template.replaceAll(SyntaxConstants.NS_DELIM, "/") + ".xpt";
        URL resource = PolicyCmptClassBuilder.class.getClassLoader().getResource(templatePath);

        assertNotNull(resource);
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_SrcFileExists() throws CoreException {
        testIsBuilderFor(true, true, true);
    }

    @Test
    public void testIsBuilderFor_falseIf_ProductCmptType_NotChangingOverTime_SrcFileExists() throws CoreException {
        testIsBuilderFor(false, false, true);
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_ChangingOverTime_SrcFileDoesNotExist() throws CoreException {
        testIsBuilderFor(true, true, false);
    }

    @Test
    public void testIsBuilderFor_trueIf_ProductCmptType_NotChangingOverTime_SrcFileDoesNotExist() throws CoreException {
        testIsBuilderFor(true, false, false);
    }

    private void testIsBuilderFor(boolean isBuilder, boolean changingOverTime, boolean srcFileExists)
            throws CoreException {
        IIpsSrcFile ipsSrcFile = mock(IIpsSrcFile.class, Mockito.RETURNS_DEEP_STUBS);
        when(ipsSrcFile.exists()).thenReturn(srcFileExists);
        when(ipsSrcFile.getIpsObjectType()).thenReturn(IpsObjectType.PRODUCT_CMPT_TYPE);
        when(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_CHANGING_OVER_TIME)).thenReturn(
                Boolean.valueOf(changingOverTime).toString());

        assertEquals(isBuilder, productCmptGenerationClassBuilder.isBuilderFor(ipsSrcFile));
    }

}
