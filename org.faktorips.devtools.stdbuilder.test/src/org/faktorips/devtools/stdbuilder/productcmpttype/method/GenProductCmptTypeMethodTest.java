/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.productcmpttype.method;

import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenMethod;

/**
 * Tests concerning the generators for <tt>IProductCmptTypeMethod</tt>s.
 * 
 * @author Alexander Weickmann
 */
public class GenProductCmptTypeMethodTest extends ProductCmptTypeBuilderTest {

    /** A published <tt>IProductCmptTypeMethod</tt> that can be used for tests. */
    private IProductCmptTypeMethod publishedMethod;

    /** A public <tt>IProductCmptTypeMethod</tt> that can be used for tests. */
    private IProductCmptTypeMethod publicMethod;

    /** A published formula <tt>IProductCmptTypeMethod</tt> that can be used for tests. */
    private IProductCmptTypeMethod publishedFormulaMethod;

    /** A public formula <tt>IProductCmptTypeMethod</tt> that can be used for tests. */
    private IProductCmptTypeMethod publicFormulaMethod;

    /** <tt>GenProductCmptTypeMethod</tt> generator for the published method. */
    private GenProductCmptTypeMethod genPublishedMethod;

    /** <tt>GenProductCmptTypeMethod</tt> generator for the public method. */
    private GenProductCmptTypeMethod genPublicMethod;

    /** <tt>GenProductCmptTypeMethod</tt> generator for the published formula method. */
    private GenProductCmptTypeMethod genPublishedFormulaMethod;

    /** <tt>GenProductCmptTypeMethod</tt> generator for the public formula method. */
    private GenProductCmptTypeMethod genPublicFormulaMethod;

    @Override
    protected void setUp() throws Exception {
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

    public void testGetGeneratedJavaElementsForImplementationFormula() {
        genPublishedFormulaMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publishedFormulaMethod);
        assertTrue(generatedJavaElements.isEmpty());

        generatedJavaElements.clear();
        genPublicFormulaMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                publicFormulaMethod);
        expectMethod(javaClassGeneration, genPublicFormulaMethod);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectMethod(IType javaType, GenMethod genMethod) {
        String[] parameterTypeSignatures = new String[] { "I", "V", "QString;" };
        String methodName = genMethod.getMethod().getName();
        org.eclipse.jdt.core.IMethod expectedMethod = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
