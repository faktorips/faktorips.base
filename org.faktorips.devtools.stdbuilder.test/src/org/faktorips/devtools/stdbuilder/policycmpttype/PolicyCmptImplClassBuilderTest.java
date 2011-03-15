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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptImplClassBuilderTest extends PolicyCmptTypeBuilderTest {

    private PolicyCmptImplClassBuilder builder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsProjectProperties props = ipsProject.getProperties();
        ipsProject.setProperties(props);

        builder = new PolicyCmptImplClassBuilder(builderSet, DefaultBuilderSet.KIND_POLICY_CMPT_TYPE_IMPL);
    }

    @Test
    public void testBuild() throws CoreException {
        /*
         * The super type of this sub type is missing on purpose for this test case. This sub type
         * overrides an attribute that doesn't exist. There was a bug that caused a NPE for this
         * scenario. This test case makes sure that this bug will be found fast if it comes up
         * again.
         */
        PolicyCmptType b = newPolicyCmptType(ipsProject, "SubType");
        IPolicyCmptTypeAttribute bAttr = b.newPolicyCmptTypeAttribute();
        bAttr.setAttributeType(AttributeType.CHANGEABLE);
        bAttr.setDatatype(Datatype.INTEGER.getQualifiedName());
        bAttr.setOverwrite(true);
        bAttr.setName("age");

        try {
            ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (CoreException e) {
            printOriginalStatus(e.getStatus());
            fail();
        }
    }

    @Test
    public void testGetGeneratedJavaElements() {
        generatedJavaElements = builder.getGeneratedJavaElements(policyCmptType);
        assertTrue(generatedJavaElements.contains(javaClass));
    }

}
