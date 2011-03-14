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

package org.faktorips.devtools.stdbuilder.productcmpttype.association;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.stdbuilder.ProjectConfigurationUtil;
import org.faktorips.valueset.IntegerRange;
import org.junit.Before;
import org.junit.Test;

public class GenProdAssociationToManyTest extends GenProdAssociationTest {

    private GenProdAssociationToMany genAssociationToMany;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        association.setAssociationType(AssociationType.AGGREGATION);
        association.setMaxCardinality(IAssociation.CARDINALITY_MANY);

        genAssociationToMany = new GenProdAssociationToMany(genProductCmptType, association);

        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, true);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(0, javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(1, javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(2, javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(3, javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptLinks(4, javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(5, genAssociationToMany, javaInterfaceGeneration);
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConstrainsPolicyCmptTypeAssociation()
            throws CoreException {

        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetCardinalityForAssociation(6, genAssociationToMany, javaInterfaceGeneration);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(0, javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(1, javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(2, javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(3, javaInterfaceGeneration);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDefinesDerivedUnion() {
        association.setDerivedUnion(true);
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(0, javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(1, javaInterfaceGeneration);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldToManyAssociation(0, javaClassGeneration);
        expectMethodGetManyRelatedCmpts(1, javaClassGeneration);
        expectMethodGetManyRelatedCmptGens(2, javaClassGeneration);
        expectMethodGetRelatedCmptAtIndex(3, javaClassGeneration);
        expectMethodGetManyRelatedCmptLinks(4, javaClassGeneration);
        expectMethodGetRelatedCmptLink(5, genAssociationToMany, javaClassGeneration);
        expectMethodAddRelatedCmpt(6, javaClassGeneration);
        expectMethodAddRelatedCmptWithCardinality(7, javaClassGeneration);
        expectMethodGetNumOfRelatedCmpts(8, javaClassGeneration);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationConstrainsPolicyCmptTypeAssociation() throws CoreException {
        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectMethodGetCardinalityForAssociation(9, genAssociationToMany, javaClassGeneration);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldToManyAssociation(0, javaClassGeneration);
        expectMethodGetManyRelatedCmpts(1, javaClassGeneration);
        expectMethodGetManyRelatedCmptGens(2, javaClassGeneration);
        expectMethodGetRelatedCmptAtIndex(3, javaClassGeneration);
        expectMethodAddRelatedCmpt(4, javaClassGeneration);
        expectMethodAddRelatedCmptWithCardinality(5, javaClassGeneration);
        expectMethodGetNumOfRelatedCmpts(6, javaClassGeneration);
        assertEquals(7, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDefinesDerivedUnion() {
        association.setDerivedUnion(true);
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        assertEquals(0, generatedJavaElements.size());
    }

    private void expectFieldToManyAssociation(int index, IType javaType) {
        expectField(index, javaType, genAssociationToMany.getFieldNameToManyAssociation());
    }

    private void expectMethodGetManyRelatedCmpts(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetManyRelatedCmpts(), new String[0]);
    }

    private void expectMethodGetManyRelatedCmptGens(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetManyRelatedCmpts(),
                new String[] { "Ljava.util.Calendar;" });
    }

    private void expectMethodGetRelatedCmptAtIndex(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetRelatedCmptAtIndex(), new String[] { "I" });
    }

    private void expectMethodGetManyRelatedCmptLinks(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetManyRelatedCmptLinks(), new String[0]);
    }

    private void expectMethodGetNumOfRelatedCmpts(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameGetNumOfRelatedCmpts(), new String[0]);
    }

    private void expectMethodAddRelatedCmpt(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameAddRelatedCmpt(), new String[] { "Q"
                + javaInterfaceTargetType.getElementName() + ";" });
    }

    private void expectMethodAddRelatedCmptWithCardinality(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationToMany.getMethodNameAddRelatedCmpt(), new String[] {
                "Q" + javaInterfaceTargetType.getElementName() + ";", "Q" + IntegerRange.class.getSimpleName() + ";" });
    }

}
