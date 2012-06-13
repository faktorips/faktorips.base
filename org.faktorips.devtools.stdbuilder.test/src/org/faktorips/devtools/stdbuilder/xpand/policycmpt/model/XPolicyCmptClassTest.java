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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.xpand.model.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class XPolicyCmptClassTest {

    @Mock
    private IPolicyCmptType type;
    @Mock
    private GeneratorModelContext context;
    @Mock
    private ModelService modelService;
    private XPolicyAttribute attributeNode1;
    private XPolicyAttribute attributeNode2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void initAttributes() {
        setupAttributeList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, context, modelService);
        List<XPolicyAttribute> attributeNodeList = policyCmptClass.getAttributes();
        assertEquals(2, attributeNodeList.size());
        assertEquals(attributeNode1, attributeNodeList.get(0));
        assertEquals(attributeNode2, attributeNodeList.get(1));
    }

    @Test
    public void initAttributeList() {
        setupAttributeList();

        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, context, modelService);
        List<XPolicyAttribute> attributeList = policyCmptClass.getAttributes();
        List<XPolicyAttribute> secondAttributeList = policyCmptClass.getAttributes();
        // returns copies of the same list
        assertNotSame(attributeList, secondAttributeList);
        assertEquals(attributeList, secondAttributeList);
    }

    private void setupAttributeList() {
        attributeNode1 = mock(XPolicyAttribute.class);
        attributeNode2 = mock(XPolicyAttribute.class);
        IPolicyCmptTypeAttribute attr1 = mock(IPolicyCmptTypeAttribute.class);
        IPolicyCmptTypeAttribute attr2 = mock(IPolicyCmptTypeAttribute.class);
        List<IPolicyCmptTypeAttribute> attrList = new ArrayList<IPolicyCmptTypeAttribute>();
        attrList.add(attr1);
        attrList.add(attr2);

        doReturn(attributeNode1).when(modelService).createModelNode(attr1, XPolicyAttribute.class, context);
        doReturn(attributeNode2).when(modelService).createModelNode(attr2, XPolicyAttribute.class, context);
        when(type.getPolicyCmptTypeAttributes()).thenReturn(attrList);
    }

    @Test
    public void initAssociations() {

    }

    @Test
    public void initAssociationList() {
        XPolicyCmptClass policyCmptClass = new XPolicyCmptClass(type, context, modelService);
        List<XPolicyAssociation> assocList = policyCmptClass.getAssociations();
        List<XPolicyAssociation> secondAssocList = policyCmptClass.getAssociations();
        assertSame(assocList, secondAssocList);
    }

}
