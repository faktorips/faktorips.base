/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xpand.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ModelServiceTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private IPolicyCmptType policyCmptType;

    @Mock
    private IProductCmptType productCmptType;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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
        XProductClass node3 = modelService.getModelNode(productCmptType,
                XProductCmptGenerationClass.class, modelContext);
        assertNotNull(node3);
        assertSame(node, modelService.getModelNode(policyCmptType, XPolicyCmptClass.class, modelContext));
        assertSame(node2, modelService.getModelNode(productCmptType, XProductCmptClass.class, modelContext));
    }

}
