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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public class PolicyCmptImplClassBuilderTest extends PolicyCmptTypeBuilderTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProjectProperties props = ipsProject.getProperties();
        ipsProject.setProperties(props);
    }

    public void testBuild() throws CoreException {
        /*
         * The supertype of this subtype is missing on purpose for this testcase. This subtype
         * overrides an attribute that doesn't exist. There was a bug that caused a NPE for this
         * scenario. This testcase makes sure that this bug will be found fast if it comes up again.
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

}
