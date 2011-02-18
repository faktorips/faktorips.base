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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class PolicyCmptInterfaceBuilderTest extends PolicyCmptTypeBuilderTest {

    private PolicyCmptInterfaceBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        builder = new PolicyCmptInterfaceBuilder(builderSet, DefaultBuilderSet.KIND_POLICY_CMPT_TYPE_INTERFACE);
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(policyCmptType);
        assertTrue(generatedJavaElements.contains(javaInterface));
    }

}
