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

package org.faktorips.devtools.stdbuilder.policycmpttype.association;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

public class GenAssociationTo1Test extends GenAssociationTest {

    private GenAssociationTo1 genAssociationTo1;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        association.setMaxCardinality(1);

        genAssociationTo1 = new GenAssociationTo1(genPolicyCmptType, association);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(javaInterface);
        expectMethodSetObject(javaInterface);
        expectMethodNewChild(genAssociationTo1, javaInterface);
        assertEquals(5, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailTargetConfigurable()
            throws CoreException {

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(javaInterface);
        expectMethodSetObject(javaInterface);
        expectMethodNewChild(genAssociationTo1, javaInterface);
        expectMethodNewChildConfigured(
                genAssociationTo1,
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(javaClass);
        expectMethodSetObject(javaClass);
        expectMethodSetObjectInternal(javaClass);
        expectMethodNewChild(genAssociationTo1, javaClass);
        assertEquals(5, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailTargetConfigurable() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(javaClass);
        expectMethodSetObject(javaClass);
        expectMethodSetObjectInternal(javaClass);
        expectMethodNewChild(genAssociationTo1, javaClass);
        expectMethodNewChildConfigured(
                genAssociationTo1,
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDetailToMaster() {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(javaInterface);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDetailToMaster() {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(javaClass);
        expectMethodSetObjectInternal(javaClass);
        assertEquals(3, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(javaInterface);
        expectMethodSetObject(javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(javaClass);
        expectMethodSetObject(javaClass);
        expectMethodSetObjectInternal(javaClass);
        assertEquals(4, generatedJavaElements.size());
    }

    private void expectMethodGetRefObject(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameGetRefObject());
    }

    private void expectMethodSetObject(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameAddOrSetObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodSetObjectInternal(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameAddOrSetObjectInternal(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private String getPublishedInterfaceName(String name) {
        return genAssociationTo1.getJavaNamingConvention().getPublishedInterfaceName(name);
    }

    @Test
    public void testIsInverseOfDerivedUnion() throws Exception {
        setOptionalConstraintSharedAssociation(true);
        assertFalse(genAssociationTo1.isInverseOfDerivedUnionAssociation());

        IPolicyCmptTypeAssociation derivedUnion = (IPolicyCmptTypeAssociation)targetPolicyCmptType.newAssociation();
        derivedUnion.setDerivedUnion(true);
        derivedUnion.setTargetRoleSingular("derivedUnion");
        association.setInverseAssociation("derivedUnion");

        genAssociationTo1 = new GenAssociationTo1(genPolicyCmptType, association);
        assertTrue(genAssociationTo1.isInverseOfDerivedUnionAssociation());

        association.setInverseAssociation("");
        IPolicyCmptType superType = newPolicyCmptType(ipsProject, "superType");
        policyCmptType.setSupertype(superType.getQualifiedName());
        IAssociation sharedHost = superType.newAssociation();

    }

    @Test
    public void testGetGeneratorForInverseOfDerivedUnion() throws Exception {
        // TODO FIPS-85
    }

    private void setOptionalConstraintSharedAssociation(boolean enabled) throws CoreException {
        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setSharedDetailToMasterAssociations(enabled);
        ipsProject.setProperties(properties);
    }

}
