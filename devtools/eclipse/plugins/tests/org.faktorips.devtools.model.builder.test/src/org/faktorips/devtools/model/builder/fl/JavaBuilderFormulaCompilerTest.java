/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.fl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.List;

import org.faktorips.abstracttest.test.XmlAbstractTestCase;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet;
import org.faktorips.devtools.model.builder.java.JavaBuilderSet.FormulaCompiling;
import org.faktorips.devtools.model.builder.xmodel.GeneratorConfig;
import org.faktorips.devtools.model.builder.xmodel.GeneratorModelContext;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.method.IParameter;
import org.faktorips.devtools.model.productcmpt.IFormula;
import org.faktorips.devtools.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.util.XmlUtil;
import org.faktorips.fl.CompilationResultImpl;
import org.faktorips.fl.JavaExprCompiler;
import org.faktorips.runtime.formula.AbstractFormulaEvaluator;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JavaBuilderFormulaCompilerTest extends XmlAbstractTestCase {
    private JavaBuilderSet builderSet;
    private IIpsProject ipsProject;
    private GeneratorModelContext generatorModelContext;
    private Document document;
    private Element element;
    private JavaBuilderFormulaCompiler compiler;

    @Before
    public void setUp() throws Exception {
        builderSet = mock(JavaBuilderSet.class);
        ipsProject = mock(IIpsProject.class);
        when(builderSet.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        generatorModelContext = mock(GeneratorModelContext.class);
        when(builderSet.getGeneratorModelContext()).thenReturn(generatorModelContext);
        document = getTestDocument();
        element = document.getDocumentElement();
        compiler = new JavaBuilderFormulaCompiler();
    }

    @Test
    public void testCompileFormulas() {
        IIpsObject ipsObject = mockIpsObject();
        mockGeneratorConfig(ipsObject, FormulaCompiling.XML);
        IFormula formulaA = mockFormula("formulaA", "methodA", "foo", "\"bar\"", Datatype.STRING,
                "my.special.MyDatatype");
        IPropertyValueContainer propertyValueContainer = mockPropertyValueContainer(ipsObject, List.of(formulaA));

        compiler.compileFormulas(propertyValueContainer, document, element);

        Element compiledExpressionA = XmlUtil.getFirstElement(XmlUtil.getElement(element, IFormula.TAG_NAME, 0),
                AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
        assertThat(compiledExpressionA, is(notNullValue()));
        assertThat(XmlUtil.getCDATAorTextContent(compiledExpressionA),
                is("""
                        import my.special.MyDatatype;


                        public MyDatatype methodA()
                        {
                        return \"bar\";
                        }

                        """));
        Element compiledExpressionB = XmlUtil.getFirstElement(XmlUtil.getElement(element, IFormula.TAG_NAME, 1),
                AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
        assertThat(compiledExpressionB, is(nullValue()));
    }

    @Test
    public void testCompileFormulas_Disabled() {
        IIpsObject ipsObject = mockIpsObject();
        mockGeneratorConfig(ipsObject, FormulaCompiling.Subclass);
        IFormula formulaA = mockFormula("formulaA", "methodA", "foo", "\"bar\"", Datatype.STRING,
                "my.special.MyDatatype");
        IPropertyValueContainer propertyValueContainer = mockPropertyValueContainer(ipsObject, List.of(formulaA));

        compiler.compileFormulas(propertyValueContainer, document, element);

        Element compiledExpressionA = XmlUtil.getFirstElement(XmlUtil.getElement(element, IFormula.TAG_NAME, 0),
                AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
        assertThat(compiledExpressionA, is(nullValue()));
        Element compiledExpressionB = XmlUtil.getFirstElement(XmlUtil.getElement(element, IFormula.TAG_NAME, 1),
                AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
        assertThat(compiledExpressionB, is(nullValue()));
    }

    private IPropertyValueContainer mockPropertyValueContainer(IIpsObject ipsObject, List<IFormula> formulas) {
        IPropertyValueContainer propertyValueContainer1 = mock(IPropertyValueContainer.class);
        when(propertyValueContainer1.getIpsProject()).thenReturn(ipsProject);
        when(propertyValueContainer1.getIpsObject()).thenReturn(ipsObject);
        IPropertyValueContainer propertyValueContainer = propertyValueContainer1;
        when(propertyValueContainer.getPropertyValues(IFormula.class)).thenReturn(formulas);
        return propertyValueContainer;
    }

    private void mockGeneratorConfig(IIpsObject ipsObject, FormulaCompiling formulaCompiling) {
        GeneratorConfig genertorConfig = mock(GeneratorConfig.class);
        when(generatorModelContext.getGeneratorConfig(ipsObject)).thenReturn(genertorConfig);
        when(genertorConfig.getFormulaCompiling()).thenReturn(formulaCompiling);
    }

    private IIpsObject mockIpsObject() {
        IIpsObject ipsObject = mock(IIpsObject.class);
        when(ipsObject.getIpsProject()).thenReturn(ipsProject);
        return ipsObject;
    }

    private IFormula mockFormula(String signature,
            String methodName,
            String expression,
            String sourcecode,
            Datatype datatype,
            String javaClassName) {
        IFormula formulaA = mock(IFormula.class);
        when(formulaA.getFormulaSignature()).thenReturn(signature);
        IProductCmptTypeMethod method = mock(IProductCmptTypeMethod.class);
        when(formulaA.getIpsProject()).thenReturn(ipsProject);
        when(formulaA.findFormulaSignature(ipsProject)).thenReturn(method);
        when(formulaA.getExpression()).thenReturn(expression);
        JavaExprCompiler expressionCompiler = mock(JavaExprCompiler.class);
        when(expressionCompiler.compile(expression)).thenReturn(new CompilationResultImpl(sourcecode, datatype));
        when(formulaA.newExprCompiler(ipsProject)).thenReturn(expressionCompiler);
        when(method.getParameters()).thenReturn(new IParameter[0]);
        when(method.getDatatype()).thenReturn(datatype.getQualifiedName());
        when(method.findDatatype(ipsProject)).thenReturn(datatype);
        when(ipsProject.findDatatype(datatype.getQualifiedName())).thenReturn(datatype);
        when(builderSet.getJavaClassName(datatype, false)).thenReturn(javaClassName);
        when(method.getName()).thenReturn(methodName);
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC);
        return formulaA;
    }

}
