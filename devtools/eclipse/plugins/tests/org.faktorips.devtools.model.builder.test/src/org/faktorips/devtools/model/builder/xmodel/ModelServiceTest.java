/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.xmodel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptGenerationClass;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ModelServiceTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private GeneratorConfig generatorConfig;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IProductCmptType productCmptType;

    private AutoCloseable openMocks;

    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(modelContext.getBaseGeneratorConfig()).thenReturn(generatorConfig);
    }

    @AfterEach
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Test
    public void testGetModelNode() throws Exception {
        ModelService modelService = new ModelService();
        XClass node = modelService.getModelNode(policyCmptType, XPolicyCmptClass.class, modelContext);
        assertNotNull(node);
        XProductCmptClass node2 = modelService.getModelNode(productCmptType, XProductCmptClass.class, modelContext);
        assertNotNull(node2);

        // repeatable
        assertSame(node, modelService.getModelNode(policyCmptType, XPolicyCmptClass.class, modelContext));
        assertSame(node2, modelService.getModelNode(productCmptType, XProductCmptClass.class, modelContext));

        // same ipsObjectPart other type
        XProductClass node3 = modelService.getModelNode(productCmptType, XProductCmptGenerationClass.class,
                modelContext);
        assertNotNull(node3);
        assertSame(node, modelService.getModelNode(policyCmptType, XPolicyCmptClass.class, modelContext));
        assertSame(node2, modelService.getModelNode(productCmptType, XProductCmptClass.class, modelContext));
    }

}
