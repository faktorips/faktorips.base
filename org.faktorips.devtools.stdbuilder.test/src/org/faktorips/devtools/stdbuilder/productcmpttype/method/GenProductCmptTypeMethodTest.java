/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype.method;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.voidParam;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenMethod;
import org.junit.Before;
import org.junit.Test;

public class GenProductCmptTypeMethodTest extends ProductCmptTypeBuilderTest {

    private IProductCmptTypeMethod publishedMethod;

    private IProductCmptTypeMethod publicMethod;

    private IProductCmptTypeMethod publishedFormulaMethod;

    private IProductCmptTypeMethod publicFormulaMethod;

    private GenProductCmptTypeMethod genPublishedMethod;

    private GenProductCmptTypeMethod genPublicMethod;

    private GenProductCmptTypeMethod genPublishedFormulaMethod;

    private GenProductCmptTypeMethod genPublicFormulaMethod;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        publishedMethod = createTestMethod("publishedMethod", Modifier.PUBLISHED, false);
        publicMethod = createTestMethod("publicMethod", Modifier.PUBLIC, false);
        publishedFormulaMethod = createTestMethod("publishedFormulaMethod", Modifier.PUBLISHED, true);
        publicFormulaMethod = createTestMethod("publicFormulaMethod", Modifier.PUBLIC, true);

        genPublishedMethod = new GenProductCmptTypeMethod(genProductCmptType, publishedMethod);
        genPublicMethod = new GenProductCmptTypeMethod(genProductCmptType, publicMethod);
        genPublishedFormulaMethod = new GenProductCmptTypeMethod(genProductCmptType, publishedFormulaMethod);
        genPublicFormulaMethod = new GenProductCmptTypeMethod(genProductCmptType, publicFormulaMethod);
    }

    private IProductCmptTypeMethod createTestMethod(String name, Modifier modifier, boolean formula) {
        IProductCmptTypeMethod method = (IProductCmptTypeMethod)productCmptType.newMethod();
        method.setName(name);
        method.setDatatype(Datatype.STRING.getName());
        method.setModifier(modifier);
        method.setFormulaSignatureDefinition(formula);
        IParameter parameter1 = method.newParameter();
        parameter1.setName("param1");
        parameter1.setDatatype(Datatype.PRIMITIVE_INT.getName());
        IParameter parameter2 = method.newParameter();
        parameter2.setName("param2");
        parameter2.setDatatype(Datatype.VOID.getName());
        IParameter parameter3 = method.newParameter();
        parameter3.setName("param3");
        parameter3.setDatatype(Datatype.STRING.getName());

        return method;
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genPublishedMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publishedMethod);
        expectMethod(javaInterfaceGeneration, genPublishedMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterfaceGeneration,
                publicMethod);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceFormula() {
        genPublishedFormulaMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publishedFormulaMethod);
        expectMethod(javaInterfaceGeneration, genPublishedFormulaMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicFormulaMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, publicFormulaMethod);
        assertTrue(generatedJavaElements.isEmpty());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genPublishedMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedMethod);
        expectMethod(javaClassGeneration, genPublishedMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicMethod);
        expectMethod(javaClassGeneration, genPublicMethod);
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationFormula() {
        genPublishedFormulaMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedFormulaMethod);
        expectMethod(javaClassGeneration, genPublishedFormulaMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicFormulaMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicFormulaMethod);
        expectMethod(javaClassGeneration, genPublicFormulaMethod);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectMethod(IType javaType, GenMethod genMethod) {
        String methodName = genMethod.getMethod().getName();
        expectMethod(javaType, methodName, intParam(), voidParam(), stringParam());
    }

}
