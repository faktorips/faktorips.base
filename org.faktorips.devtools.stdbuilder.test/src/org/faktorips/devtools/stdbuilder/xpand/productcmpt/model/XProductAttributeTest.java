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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XProductAttributeTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private ProductCmptType productCmptType;

    @Mock
    private IProductCmptTypeAttribute attribute;

    private XProductAttribute xProductAttribute;

    @Before
    public void setUp() throws Exception {
        when(ipsProject.getJavaNamingConvention()).thenReturn(new JavaNamingConvention());
        when(productCmptType.getIpsProject()).thenReturn(ipsProject);
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        when(attribute.getProductCmptType()).thenReturn(productCmptType);

    }

    @Before
    public void createXProductAttribute() {
        xProductAttribute = new XProductAttribute(attribute, modelContext, modelService);
    }

    @Test
    public void testDefaultIsAttributeOverwrite() {
        assertEquals(false, xProductAttribute.isOverwrite());
    }

    @Test
    public void testOverwrittenAttributeIsOverwrite() {
        attribute.setOverwrite(true);
        verify(attribute, times(1)).setOverwrite(true);
        when(attribute.isOverwrite()).thenReturn(true);
        assertEquals(true, xProductAttribute.isOverwrite());
    }
}
