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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.util.ArgumentCheck;

/**
 * Abstract base class for tests concerning the generators for <tt>IPolicyCmptTypeAttribute</tt>s.
 * <p>
 * Provides convenient methods.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenPolicyCmptTypeAttributeTest extends PolicyCmptTypeBuilderTest {

    /** A published <tt>IPolicyCmptTypeAttribute</tt> that can be used for tests. */
    protected IPolicyCmptTypeAttribute publishedAttribute;

    /** A public <tt>IPolicyCmptTypeAttribute</tt> that can be used for tests. */
    protected IPolicyCmptTypeAttribute publicAttribute;

    /** The <tt>AttributeType</tt> set by subclasses for the test attributes. */
    private AttributeType attributeType;

    /**
     * Creates a <tt>GenPolicyCmptTypeAttributeTest</tt>.
     * 
     * @param attributeType The <tt>AttributeType</tt> to apply to the
     *            <tt>IPolicyCmptTypeAttribute</tt>s provided for testing.
     * 
     * @throws NullPointerException If <tt>attributeType</tt> is <tt>null</tt>.
     */
    protected GenPolicyCmptTypeAttributeTest(AttributeType attributeType) {
        ArgumentCheck.notNull(attributeType);
        this.attributeType = attributeType;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        publishedAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        publishedAttribute.setName("publishedAttribute");
        publishedAttribute.setDatatype(Datatype.MONEY.getName());
        publishedAttribute.setModifier(Modifier.PUBLISHED);
        publishedAttribute.setAttributeType(attributeType);

        publicAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        publicAttribute.setName("publicAttribute");
        publicAttribute.setDatatype(Datatype.MONEY.getName());
        publicAttribute.setModifier(Modifier.PUBLIC);
        publicAttribute.setAttributeType(attributeType);
    }

    protected final void expectPropertyConstant(IType javaType, GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute) {
        IField expectedPropertyConstant = javaType.getField(genPolicyCmptTypeAttribute.getStaticConstantPropertyName());
        assertTrue(generatedJavaElements.contains(expectedPropertyConstant));
    }

    protected final void expectGetterMethod(IType javaType, GenAttribute genAttribute) {
        String methodName = genAttribute.getGetterMethodName();
        IMethod expectedGetterMethod = javaType.getMethod(methodName, new String[] {});
        assertTrue(generatedJavaElements.contains(expectedGetterMethod));
    }

    protected final void expectSetterMethod(IType javaType, GenAttribute genAttribute) {
        String methodName = genAttribute.getSetterMethodName();
        String[] parameterTypeSignatures = new String[] { "Q" + genAttribute.getDatatype().getName() + ";" };
        IMethod expectedSetterMethod = javaType.getMethod(methodName, parameterTypeSignatures);
        assertTrue(generatedJavaElements.contains(expectedSetterMethod));
    }

    protected final void expectMemberVar(IType javaType, GenAttribute genAttribute) {
        String fieldName = genAttribute.getMemberVarName();
        IField expectedMemberVar = javaType.getField(fieldName);
        assertTrue(generatedJavaElements.contains(expectedMemberVar));
    }

}
