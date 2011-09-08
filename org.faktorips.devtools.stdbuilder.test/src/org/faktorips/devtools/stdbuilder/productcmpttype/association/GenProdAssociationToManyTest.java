/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.resolvedParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
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
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptLinks(javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(genAssociationToMany, javaInterfaceGeneration);
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConstrainsPolicyCmptTypeAssociation()
            throws CoreException {

        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationToMany.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements,
                javaInterfaceGeneration, association);
        expectMethodGetManyRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetNumOfRelatedCmpts(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptGens(javaInterfaceGeneration);
        expectMethodGetRelatedCmptAtIndex(javaInterfaceGeneration);
        expectMethodGetManyRelatedCmptLinks(javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(genAssociationToMany, javaInterfaceGeneration);
        expectMethodGetCardinalityForAssociation(genAssociationToMany, javaInterfaceGeneration);
        assertEquals(7, generatedJavaElements.size());
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
        expectFieldToManyAssociation(0, javaClassGeneration);
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
        expectFieldToManyAssociation(0, javaClassGeneration);
        expectMethodGetManyRelatedCmpts(javaClassGeneration);
        expectMethodGetManyRelatedCmptGens(javaClassGeneration);
        expectMethodGetRelatedCmptAtIndex(javaClassGeneration);
        expectMethodGetManyRelatedCmptLinks(javaClassGeneration);
        expectMethodGetRelatedCmptLink(genAssociationToMany, javaClassGeneration);
        expectMethodAddRelatedCmpt(javaClassGeneration);
        expectMethodAddRelatedCmptWithCardinality(javaClassGeneration);
        expectMethodGetNumOfRelatedCmpts(javaClassGeneration);
        expectMethodGetCardinalityForAssociation(genAssociationToMany, javaClassGeneration);
        assertEquals(10, generatedJavaElements.size());
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

    private void expectMethodGetManyRelatedCmpts(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetManyRelatedCmpts());
    }

    private void expectMethodGetManyRelatedCmptGens(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetManyRelatedCmpts(),
                resolvedParam(Calendar.class.getName()));
    }

    private void expectMethodGetRelatedCmptAtIndex(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetRelatedCmptAtIndex(), intParam());
    }

    private void expectMethodGetManyRelatedCmptLinks(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetManyRelatedCmptLinks());
    }

    private void expectMethodGetNumOfRelatedCmpts(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameGetNumOfRelatedCmpts());
    }

    private void expectMethodAddRelatedCmpt(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddRelatedCmpt(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }

    private void expectMethodAddRelatedCmptWithCardinality(IType javaType) {
        expectMethod(javaType, genAssociationToMany.getMethodNameAddRelatedCmpt(),
                unresolvedParam(javaInterfaceTargetType.getElementName()), unresolvedParam(IntegerRange.class));
    }

}
