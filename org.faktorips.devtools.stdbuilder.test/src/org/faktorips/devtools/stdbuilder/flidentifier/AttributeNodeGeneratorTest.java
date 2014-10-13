/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.flidentifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ListOfTypeDatatype;
import org.faktorips.devtools.core.builder.flidentifier.IdentifierNodeGeneratorFactory;
import org.faktorips.devtools.core.builder.flidentifier.ast.AttributeNode;
import org.faktorips.devtools.core.builder.flidentifier.ast.IdentifierNodeFactory;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.util.TextRegion;
import org.faktorips.devtools.stdbuilder.GeneratorRuntimeException;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyAttribute;
import org.faktorips.devtools.stdbuilder.xpand.policycmpt.model.XPolicyCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductAttribute;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.fl.CompilationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AttributeNodeGeneratorTest {

    @Mock
    private IdentifierNodeGeneratorFactory<JavaCodeFragment> factory;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private CompilationResult<JavaCodeFragment> contextCompilationResult;

    private AttributeNodeGenerator attributeNodeGenerator;

    private AttributeNode attributeNode;

    private IAttribute attribute;

    @Before
    public void createAttributeNodeGenerator() {
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        attributeNodeGenerator = new AttributeNodeGenerator(factory, builderSet);
    }

    private void createAttributeNode(boolean isDefault, boolean isStaticContext) throws CoreException {
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.STRING);
        attributeNode = createAttributeNode(isDefault, isStaticContext, false);
        when(contextCompilationResult.getDatatype()).thenReturn(Datatype.INTEGER);
        when(contextCompilationResult.getCodeFragment()).thenReturn(new JavaCodeFragment("this"));
    }

    @Test
    public void testGetCompilationResult_PolicyCmptTypeAttribute() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        createAttributeNode(false, false);
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
        createAttributeNode(true, false);
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
        createAttributeNode(false, false);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(xProductAttribute.isChangingOverTime()).thenReturn(true);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttribute() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false, false);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(builderSet.getModelNode(type, XProductCmptClass.class)).thenReturn(xProductCmptClass);
        when(xProductAttribute.isChangingOverTime()).thenReturn(false);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(xProductCmptClass.getMethodNameGetProductCmpt()).thenReturn("getProductCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertEquals("this.getProductCmpt().getAttribute()", compilationResult.getCodeFragment().getSourcecode());
        assertEquals(Datatype.STRING, compilationResult.getDatatype());
    }

    @Test
    public void testGetCompilationResult_ProductCmptTypeAttribute_StaticContext() throws Exception {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false, true);
        IType type = mock(IType.class);
        when(attribute.getType()).thenReturn(type);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);

        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(builderSet.getModelNode(type, XProductCmptClass.class)).thenReturn(xProductCmptClass);
        when(xProductAttribute.isChangingOverTime()).thenReturn(false);
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
        attributeNode = createAttributeNode(false, false, true);

        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("hsVertrag.getDeckungen()");
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(listofTypeDatatype.getBasicDatatype()).thenReturn(type);
        when(type.getJavaClassName()).thenReturn("HausratVertrag");
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
        attributeNode = createAttributeNode(false, false, true);

        JavaCodeFragment javaCodeFragment = new JavaCodeFragment("hsVertrag.getDeckungen()");
        when(contextCompilationResult.getCodeFragment()).thenReturn(javaCodeFragment);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(listofTypeDatatype.getBasicDatatype()).thenReturn(type);
        when(type.getJavaClassName()).thenReturn("HausratVertrag");
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        XPolicyAttribute xPolicyAttribute = mock(XPolicyAttribute.class);
        when(xPolicyAttribute.getMethodNameGetter()).thenReturn("getWohnflaeche");
        when(builderSet.getModelNode(attribute, XPolicyAttribute.class)).thenReturn(xPolicyAttribute);
        when(builderSet.getJavaClassName(listofTypeDatatype.getBasicDatatype(), true)).thenReturn("Integer");
        when(builderSet.getJavaClassName(Datatype.PRIMITIVE_INT, true)).thenReturn("int");
        when(builderSet.getJavaClassName(Datatype.INTEGER, true)).thenReturn("Integer");
        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);
        assertFalse(compilationResult.failed());
        assertEquals(
                "new AttributeAccessorHelper<Integer, Integer>(){\n@Override protected Integer getValueInternal(Integer sourceObject){return sourceObject.getWohnflaeche();}}.getAttributeValues(hsVertrag.getDeckungen())",
                compilationResult.getCodeFragment().getSourcecode());
    }

    @Test(expected = GeneratorRuntimeException.class)
    public void testGetCompilationResult_NoListOfTypeDatatype() throws Exception {
        attribute = mock(IPolicyCmptTypeAttribute.class);
        when(attribute.findDatatype(ipsProject)).thenReturn(Datatype.INTEGER);
        when(attribute.getDatatype()).thenReturn("Integer");
        attributeNode = createAttributeNode(false, false, false);
        ListOfTypeDatatype listofTypeDatatype = mock(ListOfTypeDatatype.class);
        when(contextCompilationResult.getDatatype()).thenReturn(listofTypeDatatype);
        attributeNodeGenerator.getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);
    }

    private AttributeNode createAttributeNode(boolean defaultAccess, boolean isStaticContext, boolean listOfType) {
        return (AttributeNode)new IdentifierNodeFactory(new TextRegion(attribute.getName(), 0, 0), ipsProject)
                .createAttributeNode(attribute, defaultAccess, isStaticContext, listOfType);
    }

    @Test
    public void testGetParameterAttributGetterNameNotChangingOverTime() throws CoreException {
        XPolicyCmptClass xPolicyCmptClass = initMocksForTestgetParameterAttributeGetterName(false);
        when(xPolicyCmptClass.getMethodNameGetProductCmpt()).thenReturn("getPolicyProdCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertTrue(compilationResult.getCodeFragment().getSourcecode()
                .contains(xPolicyCmptClass.getMethodNameGetProductCmpt()));
    }

    @Test
    public void testGetParameterAttributeGetterNameChangingOverTime() throws CoreException {
        XPolicyCmptClass xPolicyCmptClass = initMocksForTestgetParameterAttributeGetterName(true);
        when(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()).thenReturn("getMethodPolicyProdCmpt");

        CompilationResult<JavaCodeFragment> compilationResult = attributeNodeGenerator
                .getCompilationResultForCurrentNode(attributeNode, contextCompilationResult);

        assertFalse(compilationResult.failed());
        assertTrue(compilationResult.getCodeFragment().getSourcecode()
                .contains(xPolicyCmptClass.getMethodNameGetProductCmptGeneration()));
    }

    private XPolicyCmptClass initMocksForTestgetParameterAttributeGetterName(boolean isChangingOverTime)
            throws CoreException {
        attribute = mock(IProductCmptTypeAttribute.class);
        createAttributeNode(false, false);

        IPolicyCmptType datatypePolicy = mock(IPolicyCmptType.class);
        IType type = mock(IType.class);
        XPolicyCmptClass xPolicyCmptClass = mock(XPolicyCmptClass.class);
        XProductAttribute xProductAttribute = mock(XProductAttribute.class);
        XProductCmptClass xProductCmptClass = mock(XProductCmptClass.class);

        when(attribute.getType()).thenReturn(type);
        when(builderSet.getModelNode(attribute, XProductAttribute.class)).thenReturn(xProductAttribute);
        when(builderSet.getModelNode(type, XProductCmptClass.class)).thenReturn(xProductCmptClass);
        when(xProductAttribute.isChangingOverTime()).thenReturn(isChangingOverTime);
        when(xProductAttribute.getMethodNameGetter()).thenReturn("getAttribute");
        when(xProductCmptClass.getMethodNameGetProductCmpt()).thenReturn("getProductCmpt");
        when(contextCompilationResult.getDatatype()).thenReturn(datatypePolicy);
        when(builderSet.getModelNode(datatypePolicy, XPolicyCmptClass.class)).thenReturn(xPolicyCmptClass);

        return xPolicyCmptClass;
    }
}
