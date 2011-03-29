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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import static org.junit.Assert.assertEquals;

import org.faktorips.devtools.core.internal.model.pctype.ValidationRule;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
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
    public void testGetGeneratedJavaElementsForPublishedInterfaceValidatedAttributesNotDefinedInSourceCode() {
        genValidationRule.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                validationRule);

        expectField(0, javaInterface, genValidationRule.getFieldNameForMsgCode());
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceValidatedAttributesDefinedInSourceCode() {
        validationRule.setValidatedAttrSpecifiedInSrc(true);

        genValidationRule.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                validationRule);

        expectField(0, javaInterface, genValidationRule.getFieldNameForMsgCode());
        assertEquals(1, generatedJavaElements.size());
    }

}
