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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAssociation;

public class GenAssociationToManyTest extends GenAssociationTest {

    private GenAssociationToMany genAssociationToMany;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        association.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        genAssociationToMany = new GenAssociationToMany(genPolicyCmptType, association);
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldGetMaxCardinalityFor(genAssociationToMany, javaInterface);
        expectFieldAssociationName(genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodNewChild(genAssociationToMany, javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        assertEquals(9, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailTargetConfigurable()
            throws CoreException {

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectMethodGetRefObjectByQualifier(
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodContainsObject(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodNewChild(genAssociationToMany, javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        assertEquals(9, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationMasterToDetailTargetConfigurable() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodNewChildConfigured(
                genAssociationToMany,
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    public void testGetGeneratedJavaElementsForImplementationMasterToDetailQualified() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();
        association.setQualified(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodGetRefObjectByQualifier(
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    public void testGetGeneratedJavaElementsForImplementationMasterToDetailDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodContainsObject(javaClass);
        assertEquals(1, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldGetMaxCardinalityFor(genAssociationToMany, javaInterface);
        expectFieldAssociationName(genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        expectMethodAddObject(javaInterface);
        expectMethodRemoveObject(javaInterface);
        expectMethodGetRefObjectAtIndex(javaInterface);
        assertEquals(8, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociationDefinesDerivedUnion() {
        association.setAssociationType(AssociationType.ASSOCIATION);
        association.setDerivedUnion(true);

        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(genAssociationToMany, javaInterface);
        expectMethodGetNumOfRefObjects(javaInterface);
        expectMethodContainsObject(javaInterface);
        expectMethodGetAllRefObjects(javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    public void testGetGeneratedJavaElementsForImplementationAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(genAssociationToMany, javaClass);
        expectMethodGetNumOfRefObjects(javaClass);
        expectMethodContainsObject(javaClass);
        expectMethodGetAllRefObjects(javaClass);
        expectMethodGetRefObjectAtIndex(javaClass);
        expectMethodAddObject(javaClass);
        expectMethodAddObjectInternal(javaClass);
        expectMethodRemoveObject(javaClass);
        assertEquals(8, generatedJavaElements.size());
    }

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
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodGetAllRefObjects(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetAllRefObjects());
    }

    private void expectMethodAddObject(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodAddObjectInternal(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddObjectInternal(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodRemoveObject(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameRemoveObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodGetRefObjectAtIndex(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetRefObjectAtIndex(), "I");
    }

    private void expectMethodGetRefObjectByQualifier(IType javaType, IType javaInterfaceTargetConfiguringProductCmptType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetRefObject(), "Q"
                + javaInterfaceTargetConfiguringProductCmptType.getElementName() + ";");
    }

    private String getPublishedInterfaceName(String name) {
        return genAssociationToMany.getJavaNamingConvention().getPublishedInterfaceName(name);
    }

}
