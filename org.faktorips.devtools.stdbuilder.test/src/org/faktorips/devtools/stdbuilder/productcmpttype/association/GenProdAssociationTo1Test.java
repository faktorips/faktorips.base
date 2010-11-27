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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.AssociationType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.stdbuilder.productcmpttype.ProductCmptTypeBuilderTest;

public class GenProdAssociationTo1Test extends ProductCmptTypeBuilderTest {

    private static final String TARGET_PRODUCT_NAME = "Product2";

    private static final String TARGET_ROLE_SINGULAR = "Product2";

    private static final String TARGET_ROLE_PLURAL = "Product2s";

    private IType javaInterfaceTargetType;

    private IProductCmptTypeAssociation association;

    private GenProdAssociationTo1 genAssociationTo1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IProductCmptType targetProductCmptType = newProductCmptType(ipsProject, TARGET_PRODUCT_NAME);

        javaInterfaceTargetType = getGeneratedJavaType(targetProductCmptType, false, false, "I" + TARGET_PRODUCT_NAME);

        association = productCmptType.newProductCmptTypeAssociation();
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setMinCardinality(0);
        association.setMaxCardinality(1);
        association.setTarget(targetProductCmptType.getQualifiedName());
        association.setTargetRoleSingular(TARGET_ROLE_SINGULAR);
        association.setTargetRolePlural(TARGET_ROLE_PLURAL);

        genAssociationTo1 = new GenProdAssociationTo1(genProductCmptType, association);
    }

    public void testGetGeneratedJavaElementsForPublishedInterface() {
        genAssociationTo1.getGeneratedJavaElementsForPublishedInterface(generatedJavaElements, javaInterfaceGeneration,
                association);
        expectGet1RelatedCmptMethod(javaInterfaceGeneration);
        expectGet1RelatedCmptGenMethod(javaInterfaceGeneration);
        expectGet1RelatedCmptLinkMethod(javaInterfaceGeneration);
        expectGetRelatedCmptLinkMethod(javaInterfaceGeneration);
        assertEquals(4, generatedJavaElements.size());
    }

    private void expectGet1RelatedCmptMethod(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationTo1.getMethodNameGet1RelatedCmpt(), new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectGet1RelatedCmptGenMethod(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationTo1.getMethodNameGet1RelatedCmpt(),
                new String[] { "Ljava.util.Calendar;" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectGet1RelatedCmptLinkMethod(IType javaType) {
        IMethod expectedMethod = javaType
                .getMethod(genAssociationTo1.getMethodNameGet1RelatedCmptLink(), new String[0]);
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

    private void expectGetRelatedCmptLinkMethod(IType javaType) {
        IMethod expectedMethod = javaType.getMethod(genAssociationTo1.getMethodNameGet1RelatedCmptLink(),
                new String[] { "Q" + javaInterfaceTargetType.getElementName() + ";" });
        assertTrue(generatedJavaElements.contains(expectedMethod));
    }

}
