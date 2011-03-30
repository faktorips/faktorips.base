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

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.internal.model.type.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
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
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodNewChild(genAssociationToMany, javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailTargetConfigurable()
            throws CoreException {

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationToMany, javaInterface);
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodNewChild(genAssociationToMany, javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaInterface,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        assertEquals(10, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationToMany, javaInterface);
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodNewChild(genAssociationToMany, javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaInterface,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        expectMethodGetRefObjectByQualifier(
                javaInterface,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        assertEquals(11, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodNewChild(genAssociationToMany, javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        expectMethodContainsObject(javaClass);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailTargetConfigurable() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodNewChild(genAssociationToMany, javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        expectMethodContainsObject(javaClass);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaClass,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        assertEquals(10, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodNewChild(genAssociationToMany, javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        expectMethodContainsObject(javaClass);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaClass,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        expectMethodGetRefObjectByQualifier(
                javaClass,
                getGeneratedJavaInterface(configurationForTarget, false,
                        StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, configurationForTarget.getName()));
        assertEquals(11, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodContainsObject(javaClass);
        assertEquals(1, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationToMany, javaInterface);
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(8, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociationDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        expectMethodContainsObject(javaClass);
        assertEquals(8, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociationDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodContainsObject(javaClass);
        assertEquals(1, generatedJavaElements.size());
    }

    private void expectMethodGetNumOfRefObjects(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetNumOfRefObjects());
    }

    private void expectMethodContainsObject(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameContainsObject(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }

    private void expectMethodGetAllRefObjects(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetAllRefObjects());
    }

    private void expectMethodAddObject(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddObject(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }

    private void expectMethodAddObjectInternal(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddObjectInternal(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }

    private void expectMethodRemoveObject(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameRemoveObject(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }

    private void expectMethodGetRefObjectAtIndex(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetRefObjectAtIndex(), intParam());
    }

    private void expectMethodGetRefObjectByQualifier(IType javaType, IType javaInterfaceTargetConfiguringProductCmptType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetRefObject(),
                unresolvedParam(javaInterfaceTargetConfiguringProductCmptType.getElementName()));
    }

}
