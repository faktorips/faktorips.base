/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.method;

import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IParameter;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenMethod;

/**
 * Tests concerning the generators for <tt>IMethod</tt>s for <tt>IPolicyCmptType</tt>s.
 * 
 * @author Alexander Weickmann
 */
public class GenPolicyCmptTypeMethodTest extends PolicyCmptTypeBuilderTest {

    /** A published <tt>IMethod</tt> that can be used for tests. */
    private IMethod publishedMethod;

    /** A public <tt>IMethod</tt> that can be used for tests. */
    private IMethod publicMethod;

    /** <tt>GenPolicyCmptTypeMethod</tt> generator for the published method. */
    private GenPolicyCmptTypeMethod genPublishedMethod;

    /** <tt>GenPolicyCmptTypeMethod</tt> generator for the public method. */
    private GenPolicyCmptTypeMethod genPublicMethod;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        publishedMethod = policyCmptType.newMethod();
        publishedMethod.setName("publishedMethod");
        publishedMethod.setDatatype(Datatype.STRING.getName());
        publishedMethod.setModifier(Modifier.PUBLISHED);
        IParameter parameter1 = publishedMethod.newParameter();
        parameter1.setName("param1");
        parameter1.setDatatype(Datatype.PRIMITIVE_INT.getName());
        IParameter parameter2 = publishedMethod.newParameter();
        parameter2.setName("param2");
        parameter2.setDatatype(Datatype.VOID.getName());
        IParameter parameter3 = publishedMethod.newParameter();
        parameter3.setName("param3");
        parameter3.setDatatype(Datatype.STRING.getName());

        publicMethod = policyCmptType.newMethod();
        publicMethod.setName("publicMethod");
        publicMethod.setDatatype(Datatype.STRING.getName());
        publicMethod.setModifier(Modifier.PUBLIC);
        parameter1 = publicMethod.newParameter();
        parameter1.setName("param1");
        parameter1.setDatatype(Datatype.PRIMITIVE_INT.getName());
        parameter2 = publicMethod.newParameter();
        parameter2.setName("param2");
        parameter2.setDatatype(Datatype.VOID.getName());
        parameter3 = publicMethod.newParameter();
        parameter3.setName("param3");
        parameter3.setDatatype(Datatype.STRING.getName());

        genPublishedMethod = new GenPolicyCmptTypeMethod(genPolicyCmptType, publishedMethod);
        genPublicMethod = new GenPolicyCmptTypeMethod(genPolicyCmptType, publicMethod);
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genPublishedMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publishedMethod);
        expectMethod(javaInterface, genPublishedMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicMethod.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                publicMethod);
        assertTrue(generatedJavaElements.isEmpty());
    }

    public void testGetGeneratedJavaElementsForImplementation() {
        genPublishedMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publishedMethod);
        expectMethod(javaClass, genPublishedMethod);
        assertEquals(1, generatedJavaElements.size());

        generatedJavaElements.clear();
        genPublicMethod.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, publicMethod);
        expectMethod(javaClass, genPublicMethod);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectMethod(IType javaType, GenMethod genMethod) {
        String[] parameterTypeSignatures = new String[] { "I", "V", "QString;" };
        String methodName = genMethod.getMethod().getName();
        org.eclipse.jdt.core.IMethod expectedMethod = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
