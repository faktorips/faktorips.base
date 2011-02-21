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

package org.faktorips.devtools.core.model.pctype;

import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

public class AssociationTypeTest extends AbstractIpsPluginTest {

    private IAssociation association;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(project, "mycompany.motor.MotorPolicy",
                "mycompany.motor.MotorProduct");
        association = policyCmptType.newAssociation();
    }

    @Test
    public void testGetCorrespondingAssociationType() throws Exception {
        association.setAssociationType(AssociationType.ASSOCIATION);
        assertEquals(AssociationType.ASSOCIATION, association.getAssociationType().getCorrespondingAssociationType());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        assertEquals(AssociationType.COMPOSITION_DETAIL_TO_MASTER, association.getAssociationType()
                .getCorrespondingAssociationType());

        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        assertEquals(AssociationType.COMPOSITION_MASTER_TO_DETAIL, association.getAssociationType()
                .getCorrespondingAssociationType());
    }
}
