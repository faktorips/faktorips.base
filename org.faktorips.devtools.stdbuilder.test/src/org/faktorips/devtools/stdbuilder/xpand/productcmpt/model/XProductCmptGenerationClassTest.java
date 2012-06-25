/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.productcmpt.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductCmptGenerationClassTest {

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private XProductCmptClass xProductCmptClass;

    private XProductCmptGenerationClass xProductCmptGenerationClass;

    @Before
    public void createXProductCmptGenerationClass() throws Exception {
        xProductCmptGenerationClass = new XProductCmptGenerationClass(productCmptType, modelContext, modelService);
    }

    @Test
    public void testGetProductCmptClassName() throws Exception {
        when(modelService.getModelNode(productCmptType, XProductCmptClass.class, modelContext)).thenReturn(
                xProductCmptClass);
        when(xProductCmptClass.getQualifiedName(BuilderAspect.IMPLEMENTATION)).thenReturn("test.ProductCmpt");

        String productCmptClassName = xProductCmptGenerationClass.getProductCmptClassName(BuilderAspect.IMPLEMENTATION);
        assertEquals("ProductCmpt", productCmptClassName);
        verify(modelContext).addImport("test.ProductCmpt");
    }

    @Test
    public void testGetMethodNameGetProductCmpt() throws Exception {
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(xProductCmptGenerationClass.getName()).thenReturn("productCmpt");

        assertEquals("getProductCmpt", xProductCmptGenerationClass.getMethodNameGetProductCmpt());
    }

}
