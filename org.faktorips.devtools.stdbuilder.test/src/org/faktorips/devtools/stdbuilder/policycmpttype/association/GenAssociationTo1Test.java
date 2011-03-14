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
        expectMethodGetRefObject(2, javaInterface);
        expectMethodSetObject(3, javaInterface);
        expectMethodNewChild(4, genAssociationTo1, javaInterface);
        assertEquals(5, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceMasterToDetailTargetConfigurable()
            throws CoreException {

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectMethodNewChildConfigured(
                5,
                genAssociationTo1,
                javaInterface,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetail() {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(1, javaClass);
        expectMethodSetObject(2, javaClass);
        expectMethodSetObjectInternal(3, javaClass);
        expectMethodNewChild(4, genAssociationTo1, javaClass);
        assertEquals(5, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationMasterToDetailTargetConfigurable() throws CoreException {
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IProductCmptType configurationForTarget = setUpTargetConfigurable();

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectMethodNewChildConfigured(
                5,
                genAssociationTo1,
                javaClass,
                getGeneratedJavaType(configurationForTarget, false, false,
                        getPublishedInterfaceName(configurationForTarget.getName())));
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceDetailToMaster() {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(1, javaInterface);
        assertEquals(2, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationDetailToMaster() {
        association.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(1, javaClass);
        expectMethodSetObjectInternal(2, javaClass);
        assertEquals(3, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForPublishedInterfaceAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterface,
                association);
        expectFieldAssociationName(0, genAssociationTo1, javaInterface);
        expectFieldGetMaxCardinalityFor(1, genAssociationTo1, javaInterface);
        expectMethodGetRefObject(2, javaInterface);
        expectMethodSetObject(3, javaInterface);
        assertEquals(4, generatedJavaElements.size());
    }

    @Test
    public void testGetGeneratedJavaElementsForImplementationAssociation() {
        association.setAssociationType(AssociationType.ASSOCIATION);

        genAssociationTo1.getGeneratedJavaElementsForImplementation(generatedJavaElements, javaClass, association);
        expectFieldAssociation(0, genAssociationTo1, javaClass);
        expectMethodGetRefObject(1, javaClass);
        expectMethodSetObject(2, javaClass);
        expectMethodSetObjectInternal(3, javaClass);
        assertEquals(4, generatedJavaElements.size());
    }

    private void expectMethodGetRefObject(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationTo1.getMethodNameGetRefObject());
    }

    private void expectMethodSetObject(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationTo1.getMethodNameAddOrSetObject(),
                "Q" + javaInterfaceTargetType.getElementName() + ";");
    }

    private void expectMethodSetObjectInternal(int index, IType javaType) {
        expectMethod(index, javaType, genAssociationTo1.getMethodNameAddOrSetObjectInternal(), "Q"
                + javaInterfaceTargetType.getElementName() + ";");
    }

    private String getPublishedInterfaceName(String name) {
        return genAssociationTo1.getJavaNamingConvention().getPublishedInterfaceName(name);
    }

}
