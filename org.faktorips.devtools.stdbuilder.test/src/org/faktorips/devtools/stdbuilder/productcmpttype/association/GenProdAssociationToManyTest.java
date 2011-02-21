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
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
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
        expectMethodGetManyRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptLinks(javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(genAssociationToMany, javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(javaInterfaceGeneration);
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConstrainsPolicyCmptTypeAssociation()
            throws CoreException {

        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetCardinalityForAssociation(genAssociationToMany, javaInterfaceGeneration);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(javaInterfaceGeneration);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDefinesDerivedUnion() {
        association.setDerivedUnion(true);
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(javaInterfaceGeneration);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldToManyAssociation(javaClassGeneration);
        expectMethodGetManyRelatedCmpts(javaClassGeneration);
        expectMethodGetManyRelatedCmptGens(javaClassGeneration);
        expectMethodGetRelatedCmptAtIndex(javaClassGeneration);
        expectMethodGetManyRelatedCmptLinks(javaClassGeneration);
        expectMethodGetRelatedCmptLink(genAssociationToMany, javaClassGeneration);
        expectMethodAddRelatedCmpt(javaClassGeneration);
        expectMethodAddRelatedCmptWithCardinality(javaClassGeneration);
        expectMethodGetNumOfRelatedCmpts(javaClassGeneration);
        assertEquals(9, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationConstrainsPolicyCmptTypeAssociation() throws CoreException {
        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectMethodGetCardinalityForAssociation(genAssociationToMany, javaClassGeneration);
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldToManyAssociation(javaClassGeneration);
        expectMethodGetManyRelatedCmpts(javaClassGeneration);
        expectMethodGetManyRelatedCmptGens(javaClassGeneration);
        expectMethodGetRelatedCmptAtIndex(javaClassGeneration);
        expectMethodAddRelatedCmpt(javaClassGeneration);
        expectMethodAddRelatedCmptWithCardinality(javaClassGeneration);
        expectMethodGetNumOfRelatedCmpts(javaClassGeneration);
        assertEquals(7, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDefinesDerivedUnion() {
        association.setDerivedUnion(true);
        genAssociationToMany.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        assertEquals(0, generatedJavaElements.size());
    }

    private void expectFieldToManyAssociation(IType javaType) {
        IField expectedField = javaType.getField(genAssociationToMany.getFieldNameToManyAssociation());
        assertTrue(generatedJavaElements.contains(expectedField));
    }

    private void expectMethodGetManyRelatedCmpts(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameGetManyRelatedCmpts(),
                new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodGetManyRelatedCmptGens(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameGetManyRelatedCmpts(),
                new String[] { "Ljava.util.Calendar;" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodGetRelatedCmptAtIndex(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameGetRelatedCmptAtIndex(),
                new String[] { "I" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodGetManyRelatedCmptLinks(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameGetManyRelatedCmptLinks(),
                new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodGetNumOfRelatedCmpts(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameGetNumOfRelatedCmpts(),
                new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodAddRelatedCmpt(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameAddRelatedCmpt(),
                new String[] { "Q" + javaInterfaceTargetType.getElementName() + ";" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectMethodAddRelatedCmptWithCardinality(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationToMany.getMethodNameAddRelatedCmpt(), new String[] {
                "Q" + javaInterfaceTargetType.getElementName() + ";", "Q" + IntegerRange.class.getSimpleName() + ";" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
