/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.xpand.policycmpt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Set;

import org.faktorips.devtools.core.builder.JavaNamingConvention;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.devtools.stdbuilder.xpand.model.XDerivedUnionAssociation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class XPolicyAssociationTest {

    @Mock
    private IPolicyCmptTypeAssociation typeAssoc;

    @Mock
    private GeneratorModelContext context;

    @Mock
    private ModelService modelService;

    @Mock
    private XPolicyAssociation inverseAssoc;
    private XPolicyAssociation assoc;

    @Before
    public void setUp() {
        assoc = spy(new XPolicyAssociation(typeAssoc, context, modelService));
        doReturn(new JavaNamingConvention()).when(assoc).getJavaNamingConvention();
    }

    @Test
    public void testGenerateSynchronizeForAdd() {
        doReturn(true).when(assoc).isMasterToDetail();
        doReturn(false).when(assoc).hasInverseAssociation();
        assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForAdd());

        doReturn(false).when(assoc).isMasterToDetail();
        doReturn(true).when(assoc).hasInverseAssociation();
        assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForAdd());

        doReturn(false).when(assoc).isMasterToDetail();
        doReturn(false).when(assoc).hasInverseAssociation();
        assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForAdd());

        doReturn(true).when(assoc).isMasterToDetail();
        doReturn(true).when(assoc).hasInverseAssociation();
        assertTrue(assoc.isGenerateCodeToSynchronizeInverseCompositionForAdd());
    }

    @Test
    public void testGenerateSynchronizeForRemove() {
        doReturn(true).when(assoc).isMasterToDetail();
        doReturn(false).when(assoc).hasInverseAssociation();
        assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForRemove());

        doReturn(false).when(assoc).isMasterToDetail();
        doReturn(true).when(assoc).hasInverseAssociation();
        // TODO fix with FIPS-1141
        // assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForRemove());

        doReturn(false).when(assoc).isMasterToDetail();
        doReturn(false).when(assoc).hasInverseAssociation();
        assertFalse(assoc.isGenerateCodeToSynchronizeInverseCompositionForRemove());

        doReturn(true).when(assoc).isMasterToDetail();
        doReturn(true).when(assoc).hasInverseAssociation();
        assertTrue(assoc.isGenerateCodeToSynchronizeInverseCompositionForRemove());
    }

    @Test
    public void testIsCompositionMasterToDetail() throws Exception {
    }

    public static <T> T myMock(Class<T> classToMock, String name, Answer<?> defaultAnswer) {
        return mock(classToMock, withSettings().defaultAnswer(defaultAnswer).name(name));
    }

    private XPolicyAssociation[] mockAssociations(String name,
            String inverseName,
            boolean isDerivedUnion,
            XDerivedUnionAssociation subsettedDU) {
        Answer<Object> myAnswer = new Answer<Object>() {

            @SuppressWarnings("unchecked")
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (invocation.getMethod().getName().equals("getModelNode")) {
                    return modelService.getModelNode((IIpsObjectPartContainer)invocation.getArguments()[0],
                            (Class<? extends AbstractGeneratorModelNode>)invocation.getArguments()[1], context);
                } else if (invocation.getMethod().getName().equals("getSubsettedDetailToMasterAssociations")) {
                    return invocation.callRealMethod();
                }
                return Mockito.RETURNS_DEFAULTS.answer(invocation);
            }
        };

        XPolicyAssociation xPolicyAssociation = myMock(XPolicyAssociation.class, name, myAnswer);
        XPolicyAssociation xInversePolicyAssociation = myMock(XPolicyAssociation.class, inverseName, myAnswer);
        IPolicyCmptTypeAssociation policyAssociation = mock(IPolicyCmptTypeAssociation.class, name);
        IPolicyCmptTypeAssociation inverseAssociation = mock(IPolicyCmptTypeAssociation.class, inverseName);

        when(xPolicyAssociation.getName()).thenReturn(name);
        when(xInversePolicyAssociation.getName()).thenReturn(inverseName);

        when(xPolicyAssociation.isComposition()).thenReturn(true);
        when(xPolicyAssociation.isMasterToDetail()).thenReturn(true);
        when(xInversePolicyAssociation.isCompositionDetailToMaster()).thenReturn(true);

        when(xPolicyAssociation.getInverseAssociation()).thenReturn(xInversePolicyAssociation);
        when(xInversePolicyAssociation.getInverseAssociation()).thenReturn(xPolicyAssociation);
        when(xPolicyAssociation.hasInverseAssociation()).thenReturn(true);
        when(xInversePolicyAssociation.hasInverseAssociation()).thenReturn(true);

        if (isDerivedUnion) {
            when(xPolicyAssociation.isDerived()).thenReturn(isDerivedUnion);
            when(xInversePolicyAssociation.isDerived()).thenReturn(isDerivedUnion);

            XDerivedUnionAssociation xDerivedUnionAssociation = myMock(XDerivedUnionAssociation.class, name, myAnswer);
            when(xDerivedUnionAssociation.getAssociation()).thenReturn(policyAssociation);
            when(modelService.getModelNode(policyAssociation, XDerivedUnionAssociation.class, context)).thenReturn(
                    xDerivedUnionAssociation);
            when(xDerivedUnionAssociation.getName()).thenReturn(name);

            XDetailToMasterDerivedUnionAssociation xDetailToMasterDerivedUnionAssociation = myMock(
                    XDetailToMasterDerivedUnionAssociation.class, inverseName, myAnswer);
            when(xDetailToMasterDerivedUnionAssociation.getAssociation()).thenReturn(inverseAssociation);
            when(modelService.getModelNode(inverseAssociation, XDetailToMasterDerivedUnionAssociation.class, context))
                    .thenReturn(xDetailToMasterDerivedUnionAssociation);
            when(xDetailToMasterDerivedUnionAssociation.getName()).thenReturn(inverseName);
        }

        if (subsettedDU != null) {
            when(xPolicyAssociation.getSubsettedDerivedUnion()).thenReturn(subsettedDU);
            when(xPolicyAssociation.isSubsetOfADerivedUnion()).thenReturn(true);
        }

        when(xPolicyAssociation.getAssociation()).thenReturn(policyAssociation);
        when(xInversePolicyAssociation.getAssociation()).thenReturn(inverseAssociation);
        when(modelService.getModelNode(policyAssociation, XPolicyAssociation.class, context)).thenReturn(
                xPolicyAssociation);
        when(modelService.getModelNode(inverseAssociation, XPolicyAssociation.class, context)).thenReturn(
                xInversePolicyAssociation);

        return new XPolicyAssociation[] { xPolicyAssociation, xInversePolicyAssociation };
    }

    private XDerivedUnionAssociation asDU(XPolicyAssociation policyAssociation) {
        return modelService.getModelNode(policyAssociation.getAssociation(), XDerivedUnionAssociation.class, context);
    }

    private XDetailToMasterDerivedUnionAssociation asDtoM_DU(XPolicyAssociation policyAssociation) {
        return modelService.getModelNode(policyAssociation.getAssociation(),
                XDetailToMasterDerivedUnionAssociation.class, context);
    }

    @Test
    public void testGetSubsettedDetailToMasterAssociations_easyCase() throws Exception {
        XPolicyAssociation[] derivedUnions1 = mockAssociations("derivedUnion", "inverseOfDerivedUnion1", true, null);
        XPolicyAssociation[] derivedUnions2 = mockAssociations("derivedUnion2", "inverseOfDerivedUnion2", true,
                asDU(derivedUnions1[0]));
        XPolicyAssociation[] subset = mockAssociations("subset", "other", true, asDU(derivedUnions2[0]));

        Set<XDetailToMasterDerivedUnionAssociation> detailToMasterAssociations = subset[1]
                .getSubsettedDetailToMasterAssociations();
        assertEquals(2, detailToMasterAssociations.size());
        assertThat(detailToMasterAssociations, hasItems(asDtoM_DU(derivedUnions1[1]), asDtoM_DU(derivedUnions2[1])));
    }

    @Test
    public void testGetSubsettedDetailToMasterAssociations_sameName() throws Exception {
        XPolicyAssociation[] derivedUnions1 = mockAssociations("derivedUnion1", "same", true, null);
        XPolicyAssociation[] derivedUnions2 = mockAssociations("derivedUnion2", "same", true, asDU(derivedUnions1[0]));
        XPolicyAssociation[] subset = mockAssociations("subset", "inverseOfDerivedUnion", false,
                asDU(derivedUnions1[0]));
        when(derivedUnions2[1].getSuperAssociationWithSameName()).thenReturn(derivedUnions1[1]);

        Set<XDetailToMasterDerivedUnionAssociation> detailToMasterAssociations = subset[1]
                .getSubsettedDetailToMasterAssociations();
        assertEquals(1, detailToMasterAssociations.size());
        assertThat(detailToMasterAssociations, hasItems(asDtoM_DU(derivedUnions1[1])));
    }

    @Test
    public void testGetSubsettedDetailToMasterAssociations_notDerivedSameName() throws Exception {
        XPolicyAssociation[] derivedUnions1 = mockAssociations("derivedUnion1", "inverseOfDerivedUnion1", true, null);
        XPolicyAssociation[] derivedUnions2 = mockAssociations("derivedUnion2", "same", true, asDU(derivedUnions1[0]));
        XPolicyAssociation[] subset = mockAssociations("subset", "same", false, asDU(derivedUnions1[0]));
        when(subset[1].getSuperAssociationWithSameName()).thenReturn(derivedUnions2[1]);

        Set<XDetailToMasterDerivedUnionAssociation> detailToMasterAssociations = subset[1]
                .getSubsettedDetailToMasterAssociations();
        assertEquals(2, detailToMasterAssociations.size());
        assertThat(detailToMasterAssociations, hasItems(asDtoM_DU(derivedUnions1[1]), asDtoM_DU(derivedUnions2[1])));
    }

    @Test
    public void testGetSubsettedDetailToMasterAssociations_shared() throws Exception {
        XPolicyAssociation[] derivedUnions1 = mockAssociations("derivedUnion1", "shared", true, null);
        XPolicyAssociation[] subset = mockAssociations("subset", "shared", false, asDU(derivedUnions1[0]));
        when(subset[1].isSharedAssociation()).thenReturn(true);
        IPolicyCmptTypeAssociation subsetInverse = subset[1].getAssociation();
        IPolicyCmptTypeAssociation derivedUnionInverse = derivedUnions1[1].getAssociation();
        when(subsetInverse.findSharedAssociationHost(any(IIpsProject.class))).thenReturn(derivedUnionInverse);

        Set<XDetailToMasterDerivedUnionAssociation> detailToMasterAssociations = subset[1]
                .getSubsettedDetailToMasterAssociations();
        assertEquals(1, detailToMasterAssociations.size());
        assertThat(detailToMasterAssociations, hasItems(asDtoM_DU(derivedUnions1[1])));
    }

}
