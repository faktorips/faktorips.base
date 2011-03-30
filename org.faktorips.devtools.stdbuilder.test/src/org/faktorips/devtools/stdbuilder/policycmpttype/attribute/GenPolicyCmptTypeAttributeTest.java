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

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;

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
 * Abstract base class for tests concerning the generators for {@link IPolicyCmptTypeAttribute}s.
 * <p>
 * Provides convenient methods.
 * 
 * @author Alexander Weickmann
 */
public abstract class GenPolicyCmptTypeAttributeTest extends PolicyCmptTypeBuilderTest {

    protected IPolicyCmptTypeAttribute publishedAttribute;

    protected IPolicyCmptTypeAttribute publicAttribute;

    /** The {@link AttributeType} set by subclasses for the test attributes. */
    private AttributeType attributeType;

    /**
     * @param attributeType The {@link AttributeType} to apply to the
     *            {@link IPolicyCmptTypeAttribute}s provided for testing.
     * 
     * @throws NullPointerException If the parameter is null.
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

    protected final void expectGetterMethod(IType javaType, GenAttribute genAttribute) {
        expectMethod(javaType, genAttribute.getGetterMethodName());
    }

    protected final void expectSetterMethod(IType javaType, GenAttribute genAttribute) {
        expectMethod(javaType, genAttribute.getSetterMethodName(),
                unresolvedParam(genAttribute.getDatatype().getName()));
    }

}
