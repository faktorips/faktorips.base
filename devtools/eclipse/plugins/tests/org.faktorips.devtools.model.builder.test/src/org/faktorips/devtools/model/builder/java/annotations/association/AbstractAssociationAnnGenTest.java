/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.association;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.xmodel.XAssociation;
import org.faktorips.devtools.model.builder.xmodel.XDerivedUnionAssociation;
import org.faktorips.devtools.model.builder.xmodel.XType;
import org.faktorips.devtools.model.builder.xmodel.policycmpt.XPolicyAssociation;
import org.faktorips.devtools.model.builder.xmodel.productcmpt.XProductAssociation;
import org.faktorips.runtime.model.type.AssociationKind;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This class is merely a String test. Functionality has to be additionally tested in integration
 * tests.
 */
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractAssociationAnnGenTest {

    private static final String ASSOCIATION = "association";
    private static final String ASSOCIATION_PLURAL = "associations";
    private static final String ASSOCIATION_TARGET_UNQUALIFIED = "AssociationTarget";
    private static final String ASSOCIATION_TARGET = "some.package." + ASSOCIATION_TARGET_UNQUALIFIED;
    private static final String DERIVED_UNION = "derivedUnion";
    private static final String MATCHING_SOURCE_TYPE = "MatchingSourceType";
    private static final String MATCHING_ASSOCIATION = "matchingAssociation";
    private static final int MIN_CARD = 0;
    private static final int MAX_CARD = 10;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractAssociationAnnGen annGen;

    // expected outcomes
    private String annAssociation = "@IpsAssociation(name = \"" + ASSOCIATION + "\", pluralName = \""
            + ASSOCIATION_PLURAL + "\", kind = AssociationKind.Composition, targetClass = "
            + ASSOCIATION_TARGET_UNQUALIFIED
            + ".class, min = " + MIN_CARD + ", max = " + MAX_CARD + ")" + System.lineSeparator();
    private String annAssociationQualified = "@IpsAssociation(name = \"" + ASSOCIATION + "\", pluralName = \""
            + ASSOCIATION_PLURAL + "\", kind = AssociationKind.Composition, targetClass = "
            + ASSOCIATION_TARGET
            + ".class, min = " + MIN_CARD + ", max = " + MAX_CARD + ")" + System.lineSeparator();
    private String annQualifiedAssociation = "@IpsAssociation(name = \"" + ASSOCIATION + "\", pluralName = \""
            + ASSOCIATION_PLURAL + "\", kind = AssociationKind.Composition, targetClass = "
            + ASSOCIATION_TARGET_UNQUALIFIED
            + ".class, min = " + MIN_CARD + ", max = " + MAX_CARD + ", qualified = true)" + System.lineSeparator();
    private String annDerivedUnion = "@IpsDerivedUnion" + System.lineSeparator();
    private String annSubsetOfDerivedUnion = "@IpsSubsetOfDerivedUnion(\"" + DERIVED_UNION + "\")"
            + System.lineSeparator();
    private String annMatchingAssociation = "@IpsMatchingAssociation(source = " + MATCHING_SOURCE_TYPE
            + ".class, name = \"" + MATCHING_ASSOCIATION + "\")" + System.lineSeparator();

    @Test
    public void testCreateAnnAssociation() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET_UNQUALIFIED);
        assertThat(annGen.createAnnAssociation(association).getSourcecode(), is(annAssociation));
    }

    @Test
    public void testCreateAnnAssociation_ConflictingImport() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET);

        assertThat(annGen.createAnnAssociation(association).getSourcecode(), is(annAssociationQualified));
    }

    @Test
    public void testCreateAnnQualifiedAssociation() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET_UNQUALIFIED);
        when(association.isQualified()).thenReturn(true);
        assertThat(annGen.createAnnAssociation(association).getSourcecode(), is(annQualifiedAssociation));
    }

    @Test
    public void testCreateAnnDerivedUnion() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET_UNQUALIFIED);
        assertThat(annGen.createAnnDerivedUnion(association).getSourcecode(), is(""));

        mockDerivedUnion(association);
        assertThat(annGen.createAnnDerivedUnion(association).getSourcecode(), is(annDerivedUnion));
    }

    @Test
    public void testCreateAnnSubsetOfDerivedUnion() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET_UNQUALIFIED);
        assertThat(annGen.createAnnSubsetOfDerivedUnion(association).getSourcecode(), is(""));

        mockSubsetOfDerivedUnion(association);
        assertThat(annGen.createAnnSubsetOfDerivedUnion(association).getSourcecode(), is(annSubsetOfDerivedUnion));
    }

    @Test
    public void testMatchingAssociation() {
        XAssociation association = mockBasicAssociation(ASSOCIATION_TARGET_UNQUALIFIED);
        mockMatchingAssociation(association);
        assertThat(annGen.createAnnMatchingAssociation(association).getSourcecode(), is(annMatchingAssociation));
    }

    @Test
    public void testCreateAnnAssociationNotRelevant() {
        XProductAssociation association = mock(XProductAssociation.class);
        when(association.getName(false)).thenReturn(ASSOCIATION);
        when(association.getName(true)).thenReturn(ASSOCIATION_PLURAL);
        when(association.getAssociationKind()).thenReturn(AssociationKind.Composition);
        when(association.getTargetQualifiedClassName()).thenReturn(ASSOCIATION_TARGET);
        when(association.getMinCardinality()).thenReturn(MIN_CARD);
        when(association.getMaxCardinality()).thenReturn(MAX_CARD);
        when(association.addImport(ASSOCIATION_TARGET)).thenReturn(ASSOCIATION_TARGET_UNQUALIFIED);
        when(association.isVisible()).thenReturn(false);

        assertThat(annGen.createAnnAssociation(association).getSourcecode(),
                is("@IpsAssociation(name = \"association\", pluralName = \"associations\", kind = AssociationKind.Composition, targetClass = AssociationTarget.class, min = 0, max = 10, hide = true)"
                        + System.lineSeparator()));
    }

    @Test
    public void testCreateAnnAssociationCardinalityConfigurable() {
        XPolicyAssociation association = mock(XPolicyAssociation.class);
        when(association.getName(false)).thenReturn(ASSOCIATION);
        when(association.getName(true)).thenReturn(ASSOCIATION_PLURAL);
        when(association.getAssociationKind()).thenReturn(AssociationKind.Composition);
        when(association.getTargetQualifiedClassName()).thenReturn(ASSOCIATION_TARGET);
        when(association.getMinCardinality()).thenReturn(MIN_CARD);
        when(association.getMaxCardinality()).thenReturn(MAX_CARD);
        when(association.addImport(ASSOCIATION_TARGET)).thenReturn(ASSOCIATION_TARGET_UNQUALIFIED);
        when(association.isCardinalityConfigurable()).thenReturn(true);

        assertThat(annGen.createAnnAssociation(association).getSourcecode(),
                is("@IpsAssociation(name = \"association\", pluralName = \"associations\", kind = AssociationKind.Composition, targetClass = AssociationTarget.class, min = 0, max = 10, cardinalityConfigurable = true)"
                        + System.lineSeparator()));
    }

    private void mockDerivedUnion(XAssociation association) {
        when(association.isDerivedUnion()).thenReturn(true);
    }

    private void mockSubsetOfDerivedUnion(XAssociation association) {
        when(association.isSubsetOfADerivedUnion()).thenReturn(true);
        XDerivedUnionAssociation derivedUnionAssociation = mock(XDerivedUnionAssociation.class);
        when(derivedUnionAssociation.getName(false)).thenReturn(DERIVED_UNION);
        when(association.getSubsettedDerivedUnion()).thenReturn(derivedUnionAssociation);
    }

    private void mockMatchingAssociation(XAssociation association) {
        XAssociation matchingAssociation = mock(XAssociation.class);
        when(matchingAssociation.getName(false)).thenReturn(MATCHING_ASSOCIATION);
        XType matchingSourceType = mock(XType.class);
        when(matchingSourceType.getPublishedInterfaceName()).thenReturn(MATCHING_SOURCE_TYPE);
        when(matchingAssociation.getSourceModelNodeNotConsiderChangingOverTime()).thenReturn(matchingSourceType);
        when(association.getMatchingAssociation()).thenReturn(matchingAssociation);
    }

    /**
     * This method mocks a XAssociation representing {@value AssociationKind#Composition} with
     * singular name {@value #ASSOCIATION}, plural name {@value #ASSOCIATION_PLURAL}, target class
     * name {@value #ASSOCIATION_TARGET}. The minimal cardinality is {@value #MIN_CARD}, max
     * cardinality is {@value #MAX_CARD}.
     */
    private XAssociation mockBasicAssociation(String nameToUseForTargetClass) {
        XAssociation association = mock(XAssociation.class);
        when(association.getName(false)).thenReturn(ASSOCIATION);
        when(association.getName(true)).thenReturn(ASSOCIATION_PLURAL);
        when(association.getAssociationKind()).thenReturn(AssociationKind.Composition);
        when(association.getTargetQualifiedClassName()).thenReturn(ASSOCIATION_TARGET);
        when(association.getMinCardinality()).thenReturn(MIN_CARD);
        when(association.getMaxCardinality()).thenReturn(MAX_CARD);
        when(association.addImport(ASSOCIATION_TARGET)).thenReturn(nameToUseForTargetClass);
        return association;
    }
}
