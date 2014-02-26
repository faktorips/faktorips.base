/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.runtime.modeltype.IModelTypeAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class ModelTypeXmlBuilderTest extends AbstractStdBuilderTest {

    private ModelTypeXmlBuilder xmlBuilder;
    private Document doc;
    private Element modelTypeAssociation;

    @Mock
    private IpsObjectType ipsObjectType;
    @Mock
    private IProductCmptTypeAssociation productAssociation;
    @Mock
    private IAssociation matchingAssociation;
    @Mock
    private IIpsObject ipsObject;
    @Mock
    private IPolicyCmptTypeAssociation policyAssociation;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        xmlBuilder = new ModelTypeXmlBuilder(ipsObjectType, builderSet);
        doc = IpsPlugin.getDefault().getDocumentBuilder().newDocument();
        modelTypeAssociation = doc.createElement(IModelTypeAssociation.XML_TAG);
    }

    @Test
    public void testAddMatchingAssociation_ProductCmptAssociation() throws CoreException {
        when(productAssociation.findMatchingAssociation()).thenReturn(matchingAssociation);
        when(matchingAssociation.getName()).thenReturn("matchingAssociationName");
        when(matchingAssociation.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getQualifiedName()).thenReturn("matchingAssociationSource");

        xmlBuilder.addMatchingAssociation(productAssociation, modelTypeAssociation);

        assertTrue(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME));
        assertEquals("matchingAssociationName",
                modelTypeAssociation.getAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME));
        assertTrue(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE));
        assertEquals("matchingAssociationSource",
                modelTypeAssociation.getAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE));
    }

    @Test
    public void testAddMatchingAssociation_NoMatchAssociation() throws CoreException {
        when(productAssociation.findMatchingAssociation()).thenReturn(null);

        xmlBuilder.addMatchingAssociation(productAssociation, modelTypeAssociation);

        assertFalse(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME));
        assertFalse(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE));
    }

    @Test
    public void testAddMatchingAssociation_PolicyCmptTypeAssociaton() throws CoreException {
        when(policyAssociation.findMatchingAssociation()).thenReturn(matchingAssociation);
        when(matchingAssociation.getName()).thenReturn("matchingAssociationName");
        when(matchingAssociation.getIpsObject()).thenReturn(ipsObject);
        when(ipsObject.getQualifiedName()).thenReturn("matchingAssociationSource");

        xmlBuilder.addMatchingAssociation(policyAssociation, modelTypeAssociation);

        assertTrue(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_NAME));
        assertTrue(modelTypeAssociation.hasAttribute(IModelTypeAssociation.PROPERTY_MATCHING_ASSOCIATION_SOURCE));
    }

}
