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

package org.faktorips.devtools.formulalibrary.builder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.ConversionCodeGenerator;
import org.faktorips.codegen.ImportDeclaration;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.naming.BuilderAspect;
import org.faktorips.devtools.core.internal.model.method.Parameter;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunctionExpression;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptClass;
import org.faktorips.devtools.stdbuilder.xpand.productcmpt.model.XProductCmptGenerationClass;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XFormulaMethodTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private Datatype datatype;

    @Mock
    private IBaseMethod method;

    @Mock
    private XFormulaParameter xParameter1;

    @Mock
    private XFormulaParameter xParameter2;

    @Before
    public void initModelContext() {
        GeneratorModelCaches generatorModelCache = new GeneratorModelCaches();
        when(modelContext.getGeneratorModelCache()).thenReturn(generatorModelCache);
        when(modelContext.isGeneratePublishedInterfaces()).thenReturn(false);
        when(method.getIpsProject()).thenReturn(ipsProject);
    }

    @Test
    public void testGetJavaModifier() {
        when(method.getJavaModifier()).thenReturn(Modifier.PUBLIC | Modifier.STATIC);

        XFormulaMethod xFormulaMethod = new XFormulaMethod(method, modelContext, modelService);

        assertEquals("public static", xFormulaMethod.getJavaModifier());
    }

    @Test
    public void testGetParameters() {
        setUpParameters();

        XFormulaMethod xFormulaMethod = new XFormulaMethod(method, modelContext, modelService);
        Set<XFormulaParameter> parameters = xFormulaMethod.getFormulaParameters();
        assertEquals(2, parameters.size());
        assertThat(parameters, hasItems(xParameter1, xParameter2));
    }

    @Test
    public void testClassNameOfProductComponentParameter() {

        class TestType extends ProductCmptType {

            public TestType(IIpsSrcFile file) {
                super(file);
            }

        }

        IProductCmptType type = new TestType(null);

        XProductCmptGenerationClass value = mock(XProductCmptGenerationClass.class);
        when(value.getSimpleName(BuilderAspect.IMPLEMENTATION)).thenReturn(TestType.class.getName() + "AnpStufe");
        when(modelService.getModelNode(type, XProductCmptGenerationClass.class, modelContext)).thenReturn(value);

        XFormulaMethod xFormulaMethod = new XFormulaMethod(method, modelContext, modelService);

        String javaClassName = xFormulaMethod.getJavaClassName(type);

        verify(modelService).getModelNode(type, XProductCmptGenerationClass.class, modelContext);
        verify(modelService, never()).getModelNode(type, XProductCmptClass.class, modelContext);

        assertEquals(TestType.class.getName() + "AnpStufe", javaClassName);
    }

    @Test
    public void testGetFormula() throws CoreException {
        setUpParameters();
        IFormulaFunction formulaFunction = mock(IFormulaFunction.class);
        FormulaFunctionExpression functionExpression = mock(FormulaFunctionExpression.class);
        ExprCompiler exprCompiler = mock(ExprCompiler.class);
        CompilationResult compilationResult = mock(CompilationResult.class);
        JavaCodeFragment codeFragment = mock(JavaCodeFragment.class);
        ImportDeclaration importDeclaration = mock(ImportDeclaration.class);
        ConversionCodeGenerator conversion = mock(ConversionCodeGenerator.class);

        when(method.findDatatype(ipsProject)).thenReturn(datatype);
        when(method.getParent()).thenReturn(formulaFunction);
        when(formulaFunction.getExpression()).thenReturn(functionExpression);
        when(functionExpression.getExpression()).thenReturn("1 + 1");
        when(functionExpression.newExprCompiler((IIpsProject)Mockito.anyObject())).thenReturn(exprCompiler);
        when(exprCompiler.compile(Mockito.anyString())).thenReturn(compilationResult);
        when(compilationResult.successfull()).thenReturn(true);
        when(compilationResult.getDatatype()).thenReturn(datatype);
        when(compilationResult.getCodeFragment()).thenReturn(codeFragment);
        when(exprCompiler.getConversionCodeGenerator()).thenReturn(conversion);
        when(
                conversion.getConversionCode((Datatype)Mockito.anyObject(), (Datatype)Mockito.anyObject(),
                        (JavaCodeFragment)Mockito.anyObject())).thenReturn(codeFragment);
        when(codeFragment.getSourcecode()).thenReturn("test");
        when(codeFragment.getImportDeclaration()).thenReturn(importDeclaration);
        when(importDeclaration.getImports()).thenReturn(new HashSet<String>());

        XFormulaMethod xFormulaMethod = new XFormulaMethod(method, modelContext, modelService);
        String formula = xFormulaMethod.getCompiledExpression();

        assertNotNull(formula);
        assertTrue(formula, formula.contains("test"));
    }

    private void setUpParameters() {
        Parameter parameter1 = mock(Parameter.class);
        Parameter parameter2 = mock(Parameter.class);
        IParameter[] listParameters = new IParameter[] { parameter1, parameter2 };
        when(method.getParameters()).thenReturn(listParameters);
        doReturn(xParameter1).when(modelService).getModelNode(parameter1, XFormulaParameter.class, modelContext);
        doReturn(xParameter2).when(modelService).getModelNode(parameter2, XFormulaParameter.class, modelContext);
        when(parameter1.getName()).thenReturn("param1");
        when(parameter2.getName()).thenReturn("param2");
        when(parameter1.getDatatype()).thenReturn("String");
        when(parameter2.getDatatype()).thenReturn("Boolean");
    }
}
