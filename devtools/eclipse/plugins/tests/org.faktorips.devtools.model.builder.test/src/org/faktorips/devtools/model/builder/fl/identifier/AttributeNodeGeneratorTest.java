/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl.identifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAttribute;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyCmptClass;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAttribute;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductCmptClass;
import org.faktorips.devtools.model.internal.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IExpression;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.util.TextRegion;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AttributeNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private JavaBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    @Mock
    private IExpression expression;

    @Mock
    private IFormula formula;

    private AttributeNodeGenerator attributeNodeGenerator;

    private AttributeNode attributeNode;

    private IAttribute attribute;

    @Before
    public void createAttributeNodeGenerator() {
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        attributeNodeGenerator = new AttributeNodeGenerator(factory, expression, builderSet);
    }

    private void createAttributeNode(boolean isDefault) {
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.STRING);
        attributeNode = createAttributeNode(isDefault, false);
        when(contextCompilationResult.getDatatype()).thenReturn(Datatype.INTEGER);
        when(contextCompilationResult.getCodeFragment()).thenReturn(new JavaCodeFragment("this"));
    }

    @Test
    public void testGetCompilationResult_PolicyCmptTypeAttribute() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        createAttributeNode(false);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_PolicyCmptTypeAttributeDefault() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        createAttributeNode(true);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XPolicyCmptClass xPolicyCmptClass = mock(XPolicyCmptClass.class);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        when(builderSet.getModelNode(type, XPolicyCmptClass.class)).thenReturn(xPolicyCmptClass);
        when(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()).thenReturn("getProductCmptGen");
        when(xPolicyAttribute.getMethodNameGetDefaultValue()).thenReturn("getAttribute");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getProductCmptGen().getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttributeChangingOverTime() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttribute_changingOverTimeFormula() throws Exception {
        attributeNodeGenerator = new AttributeNodeGenerator(factory, formula, builderSet);

        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(builderSet.getModelNode(type, XProductCmptClass.class)).thenReturn(xProductCmptClass);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(xProductCmptClass.getMethodNameGetProductCmpt()).thenReturn("getProductCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getProductCmpt().getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttribute_staticFormula() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);
        IProductCmptType type = mock(IProductCmptType.class);
        attributeNodeGenerator = new AttributeNodeGenerator(factory, formula, builderSet);
        IPropertyValueContainer container = mock(IPropertyValueContainer.class);
        when(formula.getPropertyValueContainer()).thenReturn(container);
        when(formula.findProductCmptType(ipsProject)).thenReturn(type);
        when(container.isChangingOverTimeContainer()).thenReturn(false);
        when(contextCompilationResult.getDatatype()).thenReturn(type);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ListOfTypeDatatype() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        IPolicyCmptType type = mock(IPolicyCmptType.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        attributeNode = createAttributeNode(false, true);

        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("hsVertrag.getDeckungen()");
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(listofTypeDatatype.getBasicDatatype()).thenReturn(type);
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getWohnflaeche");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        when(builderSet.getJavaClassName(listofTypeDatatype.getBasicDatatype(), true)).thenReturn("Integer");
        when(builderSet.getJavaClassName(Datatype.INTEGER, true)).thenReturn("Integer");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        assertEquals(
                "new AttributeAccessorHelper<Integer, Integer>(){\n@Override protected Integer getValueInternal(Integer sourceObject){return sourceObject.getWohnflaeche();}}.getAttributeValues(hsVertrag.getDeckungen())",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test
    public void testGetCompilationResult_ListOfTypeDatatypePrimitiveType() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        IPolicyCmptType type = mock(IPolicyCmptType.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.PRIMITIVE_INT);
        attributeNode = createAttributeNode(false, true);

        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("hsVertrag.getDeckungen()");
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(listofTypeDatatype.getBasicDatatype()).thenReturn(type);
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getWohnflaeche");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        when(builderSet.getJavaClassName(listofTypeDatatype.getBasicDatatype(), true)).thenReturn("Integer");
        when(builderSet.getJavaClassName(Datatype.INTEGER, true)).thenReturn("Integer");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        assertEquals(
                "new AttributeAccessorHelper<Integer, Integer>(){\n@Override protected Integer getValueInternal(Integer sourceObject){return sourceObject.getWohnflaeche();}}.getAttributeValues(hsVertrag.getDeckungen())",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test(expected = IpsException.class)
    public void testGetCompilationResult_NoListOfTypeDatatype() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        attributeNode = createAttributeNode(false, false);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        attributeNodeGenerator.getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);
    }

    private AttributeNode createAttributeNode(boolean defaultAccess, boolean listOfType) {
        return (AttributeNode)new IdentifierNodeFactory(new TextRegion(attribute.getName(), 0, 0), ipsProject)
                .createAttributeNode(attribute, defaultAccess, listOfType);
    }

    @Test
    public void testGetParameterAttributGetterNameNotChangingOverTime() {
        XPolicyCmptClass xPolicyCmptClass = initMocksForTestgetParameterAttributeGetterName(false);
        when(xPolicyCmptClass.getMethodNameGetProductCmpt()).thenReturn("getPolicyProdCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertTrue(compilationResult.getCodeFragment().getSourcecode()
                .contains(xPolicyCmptClass.getMethodNameGetProductCmpt()));
    }

    @Test
    public void testGetParameterAttributeGetterNameChangingOverTime() {
        XPolicyCmptClass xPolicyCmptClass = initMocksForTestgetParameterAttributeGetterName(true);
        when(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()).thenReturn("getMethodPolicyProdCmptGen");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertTrue(compilationResult.getCodeFragment().getSourcecode()
                .contains(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()));
    }

    private XPolicyCmptClass initMocksForTestgetParameterAttributeGetterName(boolean isChangingOverTime) {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false);

        IPolicyCmptType datatypePolicy = mock(IPolicyCmptType.class);
        XPolicyCmptClass xPolicyCmptClass = mock(XPolicyCmptClass.class);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(attribute.isChangingOverTime()).thenReturn(isChangingOverTime);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(contextCompilationResult.getDatatype()).thenReturn(datatypePolicy);
        when(builderSet.getModelNode(datatypePolicy, XPolicyCmptClass.class)).thenReturn(xPolicyCmptClass);

        return xPolicyCmptClass;
    }
}
