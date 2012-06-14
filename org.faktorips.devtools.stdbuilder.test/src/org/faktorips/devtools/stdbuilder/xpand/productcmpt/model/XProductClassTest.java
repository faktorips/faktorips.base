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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XProductClassTest {
    @Mock
    private IProductCmptType type;
    @Mock
    private GeneratorModelContext modelContext;
    @Mock
    private ModelService modelService;
    @Mock
    private XProductAttribute attrNode1;
    @Mock
    private XProductAttribute attrNode2;
    @Mock
    private XProductAttribute attrNode3;
    @Mock
    private XProductAssociation assocNode1;
    @Mock
    private XProductAssociation assocNode2;
    @Mock
    private XProductAssociation assocNode3;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        setUpAttributes();
        setUpAssociations();
    }

    private void setUpAssociations() {
        List<IProductCmptTypeAssociation> assocList = new ArrayList<IProductCmptTypeAssociation>();
        IProductCmptTypeAssociation assoc1 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation assoc2 = mock(IProductCmptTypeAssociation.class);
        IProductCmptTypeAssociation assoc3 = mock(IProductCmptTypeAssociation.class);
        assocList.add(assoc1);
        assocList.add(assoc2);
        assocList.add(assoc3);

        when(type.getProductCmptTypeAssociations()).thenReturn(assocList);
        when(modelService.getModelNode(assoc1, XProductAssociation.class, modelContext)).thenReturn(assocNode1);
        when(modelService.getModelNode(assoc2, XProductAssociation.class, modelContext)).thenReturn(assocNode2);
        when(modelService.getModelNode(assoc3, XProductAssociation.class, modelContext)).thenReturn(assocNode3);
    }

    private void setUpAttributes() {
        List<IProductCmptTypeAttribute> attrList = new ArrayList<IProductCmptTypeAttribute>();
        IProductCmptTypeAttribute attr1 = mock(IProductCmptTypeAttribute.class);
        IProductCmptTypeAttribute attr2 = mock(IProductCmptTypeAttribute.class);
        IProductCmptTypeAttribute attr3 = mock(IProductCmptTypeAttribute.class);
        attrList.add(attr1);
        attrList.add(attr2);
        attrList.add(attr3);

        when(attr1.isChangingOverTime()).thenReturn(true);
        when(attr2.isChangingOverTime()).thenReturn(false);
        when(attr3.isChangingOverTime()).thenReturn(true);

        when(type.getProductCmptTypeAttributes()).thenReturn(attrList);
        when(modelService.getModelNode(attr1, XProductAttribute.class, modelContext)).thenReturn(attrNode1);
        when(modelService.getModelNode(attr2, XProductAttribute.class, modelContext)).thenReturn(attrNode2);
        when(modelService.getModelNode(attr3, XProductAttribute.class, modelContext)).thenReturn(attrNode3);
    }

    @Test
    public void getChangableProductAttributes() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        List<XProductAttribute> attributes = productClass.getAttributes();
        assertEquals(1, attributes.size());
        assertEquals(attrNode2, attributes.get(0));
    }

    @Test
    public void getStaticProductAttributes() {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        List<XProductAttribute> attributes = productClass.getAttributes();
        assertEquals(2, attributes.size());
        assertEquals(attrNode1, attributes.get(0));
        assertEquals(attrNode3, attributes.get(1));
    }

    @Test
    public void getChangableProductAssociations() {
        XProductCmptClass productClass = new XProductCmptClass(type, modelContext, modelService);
        List<XProductAssociation> associations = productClass.getAssociations();
        assertEquals(0, associations.size());
    }

    @Test
    public void getStaticProductAssociations() {
        XProductCmptGenerationClass productClass = new XProductCmptGenerationClass(type, modelContext, modelService);
        List<XProductAssociation> associations = productClass.getAssociations();
        assertEquals(3, associations.size());
        assertEquals(assocNode1, associations.get(0));
        assertEquals(assocNode2, associations.get(1));
        assertEquals(assocNode3, associations.get(2));
    }

}
