/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.wizards.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

public class CreateConstrainingAssociationOperationTest extends AbstractIpsPluginTest {
    private IPolicyCmptType targetPolicy;
    private IPolicyCmptTypeAssociation policyAssociation;
    private IPolicyCmptType subTargetPolicy;
    private IPolicyCmptType subSourcePolicy;
    private IProductCmptType targetProduct;
    private IProductCmptTypeAssociation productAssociation;
    private IProductCmptType subSourceProduct;
    private IProductCmptType subTargetProduct;
    private IPolicyCmptType sourcePolicy;
    private IProductCmptType sourceProduct;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject();
        sourcePolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "A");
        targetPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "B");
        policyAssociation = (IPolicyCmptTypeAssociation)sourcePolicy.newAssociation();
        policyAssociation.setTarget(targetPolicy.getQualifiedName());
        policyAssociation.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        policyAssociation.setMaxCardinality(Integer.MAX_VALUE);
        policyAssociation.setTargetRoleSingular(targetPolicy.getQualifiedName());
        policyAssociation.setTargetRolePlural(targetPolicy.getQualifiedName() + "s");

        subSourcePolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "ASubtype");
        subSourcePolicy.setSupertype(sourcePolicy.getQualifiedName());
        subTargetPolicy = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "BSubtype");
        subTargetPolicy.setSupertype(targetPolicy.getQualifiedName());

        sourceProduct = newProductCmptType(ipsProject, "ProductA");
        targetProduct = newProductCmptType(ipsProject, "ProductB");
        productAssociation = (IProductCmptTypeAssociation)sourceProduct.newAssociation();
        productAssociation.setAssociationType(AssociationType.AGGREGATION);
        productAssociation.setTarget(targetProduct.getQualifiedName());
        productAssociation.setTargetRoleSingular(targetProduct.getQualifiedName());
        productAssociation.setTargetRolePlural(targetProduct.getQualifiedName() + "s");

        subSourceProduct = newProductCmptType(ipsProject, "SubProductA");
        subSourceProduct.setSupertype(sourceProduct.getQualifiedName());
        subTargetProduct = newProductCmptType(ipsProject, "SubProductB");
        subTargetProduct.setSupertype(targetProduct.getQualifiedName());

        removeUnneccessaryCategories(subSourceProduct);
        removeUnneccessaryCategories(subTargetProduct);
    }

    private void removeUnneccessaryCategories(IProductCmptType type) {
        List<IProductCmptCategory> categories = type.getCategories();
        for (IProductCmptCategory category : categories) {
            category.delete();
        }
    }

    private void setUpMatchingAssociations() {
        setUpConfiguration(sourcePolicy, sourceProduct);
        setUpConfiguration(targetPolicy, targetProduct);
        setUpConfiguration(subSourcePolicy, subSourceProduct);
        setUpConfiguration(subTargetPolicy, subTargetProduct);
    }

    private void setUpConfiguration(IPolicyCmptType policyType, IProductCmptType productType) {
        policyType.setProductCmptType(productType.getQualifiedName());
        productType.setPolicyCmptType(policyType.getQualifiedName());
        policyType.setConfigurableByProductCmptType(true);
        productType.setConfigurationForPolicyCmptType(true);
    }

    private void setUpInverseAssociation() {
        IPolicyCmptTypeAssociation inverseAssoc = targetPolicy.newPolicyCmptTypeAssociation();
        inverseAssoc.setAssociationType(AssociationType.COMPOSITION_DETAIL_TO_MASTER);
        inverseAssoc.setTarget(sourcePolicy.getQualifiedName());
        inverseAssoc.setTargetRoleSingular(sourcePolicy.getName());
        inverseAssoc.setMinCardinality(0);
        inverseAssoc.setMaxCardinality(1);
        inverseAssoc.setInverseAssociation(policyAssociation.getName());
        policyAssociation.setInverseAssociation(inverseAssoc.getName());
    }

    @Test
    public void testCreateConstrainingAssociation_policy() {
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourcePolicy,
                policyAssociation, subTargetPolicy);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourcePolicy, policyAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_product() {
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourceProduct,
                productAssociation, subTargetProduct);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourceProduct, productAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_createMatchingAssociationPolicy() {
        setUpMatchingAssociations();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourcePolicy,
                policyAssociation, subTargetPolicy);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourcePolicy, policyAssociation);
        assertMatchingAssociationExistsFor(subSourcePolicy, policyAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_createMatchingAssociationProduct() {
        setUpMatchingAssociations();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourceProduct,
                productAssociation, subTargetProduct);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourceProduct, productAssociation);
        assertMatchingAssociationExistsFor(subSourceProduct, productAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_createInverseAssociationPolicy() {
        setUpInverseAssociation();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourcePolicy,
                policyAssociation, subTargetPolicy);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourcePolicy, policyAssociation);
        assertInverseAssociationExistsFor(subSourcePolicy, policyAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_createMatchingAndInverseAssociationPolicy() {
        setUpMatchingAssociations();
        setUpInverseAssociation();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourcePolicy,
                policyAssociation, subTargetPolicy);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourcePolicy, policyAssociation);
        assertMatchingAssociationExistsFor(subSourcePolicy, policyAssociation);
        assertInverseAssociationExistsFor(subSourcePolicy, policyAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_createMatchingAndInverseAssociationProduct() {
        setUpMatchingAssociations();
        setUpInverseAssociation();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourceProduct,
                productAssociation, subTargetProduct);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourceProduct, productAssociation);
        assertMatchingAssociationExistsFor(subSourceProduct, productAssociation);
        assertInverseAssociationExistsFor(subSourcePolicy, policyAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_doNotCreateMatchingAssociationForErroneousModel() {
        setUpConfiguration(sourcePolicy, sourceProduct);
        setUpConfiguration(targetPolicy, targetProduct);
        setUpConfiguration(subSourcePolicy, subSourceProduct);
        // targetType is not configured (error!)
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourceProduct,
                productAssociation, subTargetProduct);
        operation.execute();

        assertConstrainingAssociationExistsFor(subSourceProduct, productAssociation);
        assertMatchingAssociationIsMissing(subSourceProduct, productAssociation);
    }

    @Test
    public void testCreateConstrainingAssociation_multipleCallsDoNotCreateErrors() throws CoreException {
        setUpMatchingAssociations();
        setUpInverseAssociation();
        CreateConstrainingAssociationOperation operation = new CreateConstrainingAssociationOperation(subSourceProduct,
                productAssociation, subTargetProduct);
        assertErrorFreeness();

        operation.execute();
        assertErrorFreeness();

        operation.execute();
        assertErrorFreeness();

        operation.execute();
        assertErrorFreeness();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateConstrainingAssociation_illegalArguments_typeDifferent() {
        setUpMatchingAssociations();
        setUpInverseAssociation();

        new CreateConstrainingAssociationOperation(subSourceProduct, productAssociation, subTargetPolicy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateConstrainingAssociation_illegalArgument_associationWrongClass() {
        setUpMatchingAssociations();
        setUpInverseAssociation();

        new CreateConstrainingAssociationOperation(subSourceProduct, policyAssociation, subTargetProduct);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateConstrainingAssociation_illegalArgument_targetTypeMismatch() throws CoreException {
        IProductCmptType otherProductClass = newProductCmptType(ipsProject, "OtherProduct");

        setUpMatchingAssociations();
        setUpInverseAssociation();

        new CreateConstrainingAssociationOperation(subSourceProduct, productAssociation, otherProductClass);
    }

    private void assertErrorFreeness() throws CoreException {
        assertNoErrorsIn(subSourcePolicy);
        assertNoErrorsIn(subTargetPolicy);
        assertNoErrorsIn(subSourceProduct);
        assertNoErrorsIn(subTargetProduct);
    }

    private void assertNoErrorsIn(IType type) throws CoreException {
        MessageList messageList = type.validate(ipsProject);
        assertEquals(0, messageList.size());
    }

    private void assertMatchingAssociationIsMissing(IType type, IAssociation constrainedAssociation) {
        IAssociation constrainedMatchingAssociation = getConstrainedMatchingAssociation(type, constrainedAssociation);
        assertNull(constrainedMatchingAssociation);
    }

    private IAssociation getConstrainedMatchingAssociation(IType type, IAssociation constrainedAssociation) {
        try {
            IAssociation association = getConstrainingAssociation(type, constrainedAssociation);
            IAssociation matchingAssociation = association.findMatchingAssociation();
            return matchingAssociation;
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private void assertMatchingAssociationExistsFor(IType type, IAssociation constrainedAssociation) {
        IAssociation constrainedMatchingAssociation = getConstrainedMatchingAssociation(type, constrainedAssociation);
        assertExistsAndIsConstrain(constrainedMatchingAssociation);
    }

    private void assertInverseAssociationExistsFor(IPolicyCmptType type,
            IPolicyCmptTypeAssociation constrainedAssociation) {
        try {
            IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)getConstrainingAssociation(type,
                    constrainedAssociation);
            IPolicyCmptTypeAssociation inverseAssociation = association.findInverseAssociation(ipsProject);
            assertNotNull(inverseAssociation);
        } catch (CoreException e) {
            fail();
        }
    }

    private void assertConstrainingAssociationExistsFor(IType type, IAssociation constrainedAssociation) {
        IAssociation association = getConstrainingAssociation(type, constrainedAssociation);
        assertExistsAndIsConstrain(association);
    }

    private IAssociation getConstrainingAssociation(IType type, IAssociation constrainedAssociation) {
        return type.getAssociation(constrainedAssociation.getTargetRoleSingular());
    }

    private void assertExistsAndIsConstrain(IAssociation association) {
        assertNotNull(association);
        assertTrue(association.isConstrain());
    }
}
