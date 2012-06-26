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
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ImportStatement;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.internal.ProductComponent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XProductCmptClassTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;

    @Mock
    private IIpsArtefactBuilderSetConfig config;

    private GeneratorModelContext modelContext;

    private ModelService modelService;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
        modelContext = new GeneratorModelContext(config);
        modelService = new ModelService();
        ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "test.TestType");
    }

    @Test
    public void testGetBaseSuperclassName() throws Exception {
        XProductCmptClass xProductCmptClass = new XProductCmptClass(productCmptType, modelContext, modelService);
        String baseSuperclassName = xProductCmptClass.getBaseSuperclassName();
        assertEquals(ProductComponent.class.getSimpleName(), baseSuperclassName);
        assertTrue(modelContext.getImports().contains(new ImportStatement(ProductComponent.class)));
    }

    @Test
            public void testGetMethodNameGetProductComponentGeneration() throws Exception {
                XProductCmptClass xProductCmptClass = new XProductCmptClass(productCmptType, modelContext, modelService);
                String getterMethodNameForGeneration = xProductCmptClass.getMethodNameGetProductComponentGeneration();
                assertEquals("getTestTypeGen", getterMethodNameForGeneration);
            }

}
