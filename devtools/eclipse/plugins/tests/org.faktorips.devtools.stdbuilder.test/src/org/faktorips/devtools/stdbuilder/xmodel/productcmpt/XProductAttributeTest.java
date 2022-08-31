/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.productcmpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class XProductAttributeTest {

    private static final String DATATYPE = "Datatype";

    private static final String ABSTRACT_DATATYPE = "AbstractDatatype";

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IProductCmptType productCmptType;

    @Mock
    private IProductCmptTypeAttribute attribute;

    @Mock
    private IProductCmptTypeAttribute superAttribute;

    private XProductAttribute xProductAttribute;

    private XProductAttribute superXAttribute;

    @Before
    public void setUp() throws Exception {
        when(attribute.getIpsProject()).thenReturn(ipsProject);
        when(superAttribute.getIpsProject()).thenReturn(ipsProject);
    }

    @Before
    public void createXProductAttribute() {
        xProductAttribute = new XProductAttribute(attribute, modelContext, modelService);
        superXAttribute = new XProductAttribute(superAttribute, modelContext, modelService);
        when(modelService.getModelNode(superAttribute, XProductAttribute.class, modelContext)).thenReturn(
                superXAttribute);
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

    @Test
    public void testIsAbstract_true() throws Exception {
        mockDatatype(attribute, ABSTRACT_DATATYPE, true);

        assertTrue(xProductAttribute.isAbstract());
    }

    @Test
    public void testIsAbstract_false() throws Exception {
        mockDatatype(attribute, DATATYPE, false);

        assertFalse(xProductAttribute.isAbstract());
    }

    private ValueDatatype mockDatatype(IAttribute attributeMock, String datatypeName, boolean abstractFlag) {
        ValueDatatype datatype = mock(ValueDatatype.class);
        DatatypeHelper datatypeHelper = mock(DatatypeHelper.class);
        when(ipsProject.findDatatypeHelper(datatypeName)).thenReturn(datatypeHelper);
        when(attributeMock.getDatatype()).thenReturn(datatypeName);
        when(datatypeHelper.getDatatype()).thenReturn(datatype);
        when(datatype.isAbstract()).thenReturn(abstractFlag);
        return datatype;
    }

    @Test
    public void testIsGenerateContentCode_notAbstractNoOverwrite() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(attribute.isOverwrite()).thenReturn(false);

        assertTrue(xProductAttribute.isGenerateContentCode());
    }

    @Test
    public void testIsGenerateContentCode_abstractNoOverwrite() throws Exception {
        mockDatatype(attribute, ABSTRACT_DATATYPE, true);

        assertFalse(xProductAttribute.isGenerateContentCode());
    }

    @Test
    public void testIsGenerateContentCode_notAbstractAndOverwriteNotAbstract() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(superAttribute.getDatatype()).thenReturn(DATATYPE);
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(ipsProject)).thenReturn(superAttribute);

        assertFalse(xProductAttribute.isGenerateContentCode());
    }

    @Test
    public void testIsGenerateContentCode_notAbstractButOverwriteAbstract() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        mockDatatype(superAttribute, ABSTRACT_DATATYPE, true);
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(ipsProject)).thenReturn(superAttribute);

        assertTrue(xProductAttribute.isGenerateContentCode());
    }

    @Test
    public void testIsCallSetDefaultValue_abstract() throws Exception {
        mockDatatype(attribute, ABSTRACT_DATATYPE, true);

        assertFalse(xProductAttribute.isCallSetDefaultValue());
    }

    @Test
    public void testIsCallSetDefaultValue_notAbstractDefaultNull() throws Exception {
        mockDatatype(attribute, DATATYPE, false);

        assertFalse(xProductAttribute.isCallSetDefaultValue());
    }

    @Test
    public void testIsCallSetDefaultValue_notAbstractDefaultNotNull() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(attribute.getDefaultValue()).thenReturn("anyDefault");

        assertTrue(xProductAttribute.isCallSetDefaultValue());
    }

    @Test
    public void testIsCallSetDefaultValue_notAbstractDefaultNullSuperAttributeAbstract() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(attribute.getDefaultValue()).thenReturn(null);
        mockDatatype(superAttribute, ABSTRACT_DATATYPE, true);
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(ipsProject)).thenReturn(superAttribute);

        assertFalse(xProductAttribute.isCallSetDefaultValue());
    }

    @Test
    public void testIsCallSetDefaultValue_notAbstractDefaultNotNullSuperAttributeAbstract() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(attribute.getDefaultValue()).thenReturn("asdsa");
        mockDatatype(superAttribute, ABSTRACT_DATATYPE, true);

        assertTrue(xProductAttribute.isCallSetDefaultValue());
    }

    @Test
    public void testIsCallSetDefaultValue_notAbstractDefaultNullSuperAttributeNotAbstract() throws Exception {
        mockDatatype(attribute, DATATYPE, false);
        when(attribute.getDefaultValue()).thenReturn(null);
        mockDatatype(superAttribute, DATATYPE, false);
        when(attribute.isOverwrite()).thenReturn(true);
        when(attribute.findOverwrittenAttribute(ipsProject)).thenReturn(superAttribute);

        assertTrue(xProductAttribute.isCallSetDefaultValue());
    }

}
