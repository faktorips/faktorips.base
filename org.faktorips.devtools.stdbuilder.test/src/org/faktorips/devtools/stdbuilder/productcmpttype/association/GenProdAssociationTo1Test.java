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

import java.util.Calendar;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.stdbuilder.ProjectConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

public class GenProdAssociationTo1Test extends GenProdAssociationTest {

    private GenProdAssociationTo1 genAssociationTo1;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        association.setAssociationType(AssociationType.AGGREGATION);
        association.setMaxCardinality(1);

        genAssociationTo1 = new GenProdAssociationTo1(genProductCmptType, association);

        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, true);
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterfaceGeneration,
                association);
        expectMethodGet1RelatedCmpt(javaInterfaceGeneration);
        expectMethodGet1RelatedCmptGen(javaInterfaceGeneration);
        expectMethodGet1RelatedCmptLink(genAssociationTo1, javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(genAssociationTo1, javaInterfaceGeneration);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceConstrainsPolicyCmptTypeAssociation()
            throws CoreException {

        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterfaceGeneration,
                association);
        expectMethodGet1RelatedCmpt(javaInterfaceGeneration);
        expectMethodGet1RelatedCmptGen(javaInterfaceGeneration);
        expectMethodGet1RelatedCmptLink(genAssociationTo1, javaInterfaceGeneration);
        expectMethodGetRelatedCmptLink(genAssociationTo1, javaInterfaceGeneration);
        expectMethodGetCardinalityForAssociation(genAssociationTo1, javaInterfaceGeneration);
        assertEquals(5, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterfaceGeneration,
                association);
        expectMethodGet1RelatedCmpt(javaInterfaceGeneration);
        expectMethodGet1RelatedCmptGen(javaInterfaceGeneration);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementation() {
        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldTo1Association(0, javaClassGeneration);
        expectMethodGet1RelatedCmpt(javaClassGeneration);
        expectMethodGet1RelatedCmptGen(javaClassGeneration);
        expectMethodSet1RelatedCmpt(javaClassGeneration);
        expectMethodGet1RelatedCmptLink(genAssociationTo1, javaClassGeneration);
        expectMethodGetRelatedCmptLink(genAssociationTo1, javaClassGeneration);
        assertEquals(6, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationConstrainsPolicyCmptTypeAssociation() throws CoreException {
        setUpConstrainsPolicyCmptTypeAssociation();
        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectMethodGet1RelatedCmpt(javaClassGeneration);
        expectMethodGet1RelatedCmptGen(javaClassGeneration);
        expectMethodSet1RelatedCmpt(javaClassGeneration);
        expectMethodGet1RelatedCmptLink(genAssociationTo1, javaClassGeneration);
        expectMethodGetRelatedCmptLink(genAssociationTo1, javaClassGeneration);
        expectMethodGetCardinalityForAssociation(genAssociationTo1, javaClassGeneration);
        assertEquals(7, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDoNotUseTypesafeCollections() throws CoreException {
        ProjectConfigurationUtil.setUpUseTypesafeCollections(ipsProject, false);
        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClassGeneration,
                association);
        expectFieldTo1Association(0, javaClassGeneration);
        expectMethodGet1RelatedCmpt(javaClassGeneration);
        expectMethodGet1RelatedCmptGen(javaClassGeneration);
        expectMethodSet1RelatedCmpt(javaClassGeneration);
        assertEquals(4, generatedJavaElements.size());
    }

    private void expectFieldTo1Association(int index, IType javaType) {
        expectField(index, javaType, genAssociationTo1.getFieldNameTo1Association());
    }

    private void expectMethodGet1RelatedCmpt(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameGet1RelatedCmpt());
    }

    private void expectMethodGet1RelatedCmptGen(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameGet1RelatedCmpt(),
                resolvedParam(Calendar.class.getName()));
    }

    private void expectMethodGet1RelatedCmptLink(GenProdAssociation genProdAssociation, IType javaType) {
        expectMethod(javaType, genProdAssociation.getMethodNameGet1RelatedCmptLink());
    }

    private void expectMethodSet1RelatedCmpt(IType javaType) {
        expectMethod(javaType, genAssociationTo1.getMethodNameSet1RelatedCmpt(),
                unresolvedParam(javaInterfaceTargetType.getElementName()));
    }
}
