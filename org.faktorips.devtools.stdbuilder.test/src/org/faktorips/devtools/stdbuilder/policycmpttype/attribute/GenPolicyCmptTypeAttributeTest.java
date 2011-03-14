/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype.attribute;

import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.stdbuilder.policycmpttype.PolicyCmptTypeBuilderTest;
import org.faktorips.devtools.stdbuilder.type.GenAttribute;
import org.faktorips.util.ArgumentCheck;
import org.junit.Before;

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
    @Before
    public void setUp() throws Exception {
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

    protected final void expectPropertyConstant(int index, IType javaType, GenAttribute genAttribute) {
        expectField(index, javaType, genAttribute.getStaticConstantPropertyName());
    }

    protected final void expectMemberVar(int index, IType javaType, GenAttribute genAttribute) {
        expectField(index, javaType, genAttribute.getMemberVarName());
    }

    protected final void expectGetterMethod(int index, IType javaType, GenAttribute genAttribute) {
        expectMethod(index, javaType, genAttribute.getGetterMethodName(), new String[0]);
    }

    protected final void expectSetterMethod(int index, IType javaType, GenAttribute genAttribute) {
        expectMethod(index, javaType, genAttribute.getSetterMethodName(), new String[] { "Q"
                + genAttribute.getDatatype().getName() + ";" });
    }

}
