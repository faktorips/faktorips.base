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

import java.util.List;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
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

    /** Expects, that the property constant field is contained in the given list. */
    protected final void expectPropertyConstant(List<IJavaElement> javaElements,
            GenPolicyCmptTypeAttribute genPolicyCmptTypeAttribute,
            boolean forPublishedInterface) {

        IField expectedPropertyConstant = getGeneratedJavaType(forPublishedInterface).getField(
                genPolicyCmptTypeAttribute.getStaticConstantPropertyName());
        assertTrue(javaElements.contains(expectedPropertyConstant));
    }

    /** Expects, that the getter method is contained in the given list. */
    protected final void expectGetterMethod(List<IJavaElement> javaElements,
            GenAttribute genAttribute,
            boolean forPublishedInterface) {

        IMethod expectedGetterMethod = getGeneratedJavaType(forPublishedInterface).getMethod(
                genAttribute.getGetterMethodName(), new String[] {});
        assertTrue(javaElements.contains(expectedGetterMethod));
    }

    /** Expects, that the setter method is contained in the given list. */
    protected final void expectSetterMethod(List<IJavaElement> javaElements,
            GenAttribute genAttribute,
            boolean forPublishedInterface) {

        IMethod expectedSetterMethod = getGeneratedJavaType(forPublishedInterface).getMethod(
                genAttribute.getSetterMethodName(), new String[] { "Q" + genAttribute.getDatatype().getName() + ";" });
        assertTrue(javaElements.contains(expectedSetterMethod));
    }

    /** Expects, that the member variable is contained in the given list. */
    protected final void expectMemberVar(List<IJavaElement> javaElements,
            GenAttribute genAttribute,
            boolean forPublishedInterface) {

        IField expectedMemberVar = getGeneratedJavaType(forPublishedInterface)
                .getField(genAttribute.getMemberVarName());
        assertTrue(javaElements.contains(expectedMemberVar));
    }

}
