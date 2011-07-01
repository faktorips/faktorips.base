/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.junit.Assert.assertEquals;

import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.ObjectProperty;
import org.junit.Before;
import org.junit.Test;

public class GenValidationRuleTest extends PolicyCmptTypeBuilderTest {

    private IValidationRule validationRule;

    private GenValidationRule genValidationRule;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        validationRule = new ValidationRule(policyCmptType, "blub");
        validationRule.setName("blub");
        validationRule.setMessageCode("blubCode");
        validationRule.setValidatedAttrSpecifiedInSrc(false);
        genValidationRule = new GenValidationRule(genPolicyCmptType, validationRule);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceValidatedAttributesNotDefinedInSourcecode() {
        genValidationRule.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                validationRule);

        expectField(0, javaInterface, genValidationRule.getFieldNameForMsgCode());
        expectField(1, javaInterface, genValidationRule.getFieldNameForRuleName());
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceValidatedAttributesDefinedInSourcecode() {
        validationRule.setValidatedAttrSpecifiedInSrc(true);

        genValidationRule.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                validationRule);

        expectField(0, javaInterface, genValidationRule.getFieldNameForMsgCode());
        expectField(1, javaInterface, genValidationRule.getFieldNameForRuleName());
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationValidatedAttributesNotDefinedInSourcecode() {
        genValidationRule.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, validationRule);

        expectExecRuleMethod();
        expectCreateMessageForRuleMethod(false);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationValidatedAttributesDefinedInSourcecode() {
        validationRule.setValidatedAttrSpecifiedInSrc(true);

        genValidationRule.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, validationRule);

        expectExecRuleMethod();
        expectCreateMessageForRuleMethod(true);
    }

    private void expectExecRuleMethod() {
        expectMethod(javaClass, genValidationRule.getMethodNameExecRule(), unresolvedParam(MessageList.class),
                unresolvedParam(IValidationContext.class));
    }

    private void expectCreateMessageForRuleMethod(boolean attributesDefinedInSourceCode) {
        String[] parameters = new String[] { unresolvedParam(IValidationContext.class) };
        if (attributesDefinedInSourceCode) {
            parameters = new String[] { unresolvedParam(IValidationContext.class),
                    unresolvedParam(ObjectProperty.class.getSimpleName() + "[]") };
        }
        expectMethod(javaClass, genValidationRule.getMethodNameCreateMessageForRule(), parameters);
    }

}
