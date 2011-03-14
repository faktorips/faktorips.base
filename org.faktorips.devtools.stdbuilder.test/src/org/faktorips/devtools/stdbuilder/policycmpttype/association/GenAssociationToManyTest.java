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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

public class GenAssociationToManyTest extends GenAssociationTest {

    private GenAssociationToMany genAssociationToMany;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        association.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        genAssociationToMany = new GenAssociationToMany(genPolicyCmptType, association);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationToMany, javaInterface);
        expectMethodAddObject(2, javaInterface);
        expectMethodRemoveObject(3, javaInterface);
        expectMethodNewChild(4, genAssociationToMany, javaInterface);
        expectMethodGetRefObjectAtIndex(5, javaInterface);
        expectMethodGetNumOfRefObjects(6, javaInterface);
        expectMethodContainsObject(7, javaInterface);
        expectMethodGetAllRefObjects(8, javaInterface);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailTargetConfigurable()
            throws CoreException {

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectMethodNewChildConfigured(
                6,
                genAssociationToMany,
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectMethodGetRefObjectByQualifier(
                7,
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(1, javaInterface);
        expectMethodContainsObject(2, javaInterface);
        expectMethodGetAllRefObjects(3, javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(1, javaClass);
        expectMethodGetAllRefObjects(2, javaClass);
        expectMethodGetRefObjectAtIndex(3, javaClass);
        expectMethodNewChild(4, genAssociationToMany, javaClass);
        expectMethodAddObject(5, javaClass);
        expectMethodAddObjectInternal(6, javaClass);
        expectMethodRemoveObject(7, javaClass);
        expectMethodContainsObject(8, javaClass);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailTargetConfigurable() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodNewChildConfigured(
                8,
                genAssociationToMany,
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodGetRefObjectByQualifier(
                9,
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodContainsObject(0, javaClass);
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationToMany, javaInterface);
        expectMethodAddObject(2, javaInterface);
        expectMethodRemoveObject(3, javaInterface);
        expectMethodGetRefObjectAtIndex(4, javaInterface);
        expectMethodGetNumOfRefObjects(5, javaInterface);
        expectMethodContainsObject(6, javaInterface);
        expectMethodGetAllRefObjects(7, javaInterface);
        assertEquals(8, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociationDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(1, javaInterface);
        expectMethodContainsObject(2, javaInterface);
        expectMethodGetAllRefObjects(3, javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(1, javaClass);
        expectMethodGetAllRefObjects(2, javaClass);
        expectMethodGetRefObjectAtIndex(3, javaClass);
        expectMethodAddObject(4, javaClass);
        expectMethodAddObjectInternal(5, javaClass);
        expectMethodRemoveObject(6, javaClass);
        expectMethodContainsObject(7, javaClass);
        assertEquals(8, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociationDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodContainsObject(0, javaClass);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectMethodGetNumOfRefObjects(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetNumOfRefObjects());
    }

    private void expectMethodContainsObject(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameContainsObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodGetAllRefObjects(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetAllRefObjects());
    }

    private void expectMethodAddObject(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameAddObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodAddObjectInternal(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameAddObjectInternal(), "Q"
                + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodRemoveObject(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameRemoveObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodGetRefObjectAtIndex(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetRefObjectAtIndex(), "I");
    }

    private void expectMethodGetRefObjectByQualifier(int index,
            IType javaType,
            IType javaInterfaceTargetConfiguringProductCmptType) {

        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetRefObject(), "Q"
                + javaInterfaceTargetConfiguringProductCmptType.getElementName() + ";");
    }

    private String getPublishedInterfaceName(String name) {
        return genAssociationToMany.getJavaNamingConvention().getPublishedInterfaceName(name);
    }

}
