/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IType;
import org.junit.Test;

public class PullUpAttributeProcessorIntegrationTest extends AbstractIpsRefactoringTest {

    @Test
    public void testFinalCheckConditionsInvalidModel() throws CoreRuntimeException {
        policyCmptTypeAttribute.setDatatype(null);

        RefactoringStatus status = performPullUpRefactoring(policyCmptTypeAttribute, superPolicyCmptType);

        assertTrue(status.hasError());
    }

    @Test
    public void testPullUpPolicyCmptTypeAttribute() throws CoreRuntimeException {
        performPullUpRefactoring(policyCmptTypeAttribute, superPolicyCmptType);

        checkExpectationsForPullUpAttribute(policyCmptTypeAttribute, policyCmptType, superPolicyCmptType);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeFurtherUpInHierarchy() throws CoreRuntimeException {
        IPolicyCmptType superSuperPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject,
                "SuperSuperPolicy");
        IProductCmptType superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superSuperPolicyCmptType.setConfigurableByProductCmptType(true);
        superSuperPolicyCmptType.setProductCmptType(superSuperProductCmptType.getQualifiedName());
        superSuperProductCmptType.setConfigurationForPolicyCmptType(true);
        superSuperProductCmptType.setPolicyCmptType(superSuperPolicyCmptType.getQualifiedName());
        superSuperPolicyCmptType.setAbstract(true);
        superSuperProductCmptType.setAbstract(true);
        superPolicyCmptType.setSupertype(superSuperPolicyCmptType.getQualifiedName());
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());

        performPullUpRefactoring(policyCmptTypeAttribute, superSuperPolicyCmptType);

        checkExpectationsForPullUpAttribute(policyCmptTypeAttribute, policyCmptType, superSuperPolicyCmptType);
    }

    @Test
    public void testPullUpProductCmptTypeAttribute() throws CoreRuntimeException {
        performPullUpRefactoring(productCmptTypeAttribute, superProductCmptType);

        checkExpectationsForPullUpAttribute(productCmptTypeAttribute, productCmptType, superProductCmptType);
    }

    @Test
    public void testPullUpProductCmptTypeAttributeFurtherUpInHierarchy() throws CoreRuntimeException {
        IPolicyCmptType superSuperPolicyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject,
                "SuperSuperPolicy");
        IProductCmptType superSuperProductCmptType = newProductCmptType(ipsProject, "SuperSuperProduct");
        superSuperPolicyCmptType.setConfigurableByProductCmptType(true);
        superSuperPolicyCmptType.setProductCmptType(superSuperProductCmptType.getQualifiedName());
        superSuperProductCmptType.setConfigurationForPolicyCmptType(true);
        superSuperProductCmptType.setPolicyCmptType(superSuperPolicyCmptType.getQualifiedName());
        superSuperPolicyCmptType.setAbstract(true);
        superSuperProductCmptType.setAbstract(true);
        superPolicyCmptType.setSupertype(superSuperPolicyCmptType.getQualifiedName());
        superProductCmptType.setSupertype(superSuperProductCmptType.getQualifiedName());

        performPullUpRefactoring(productCmptTypeAttribute, superSuperProductCmptType);

        checkExpectationsForPullUpAttribute(productCmptTypeAttribute, productCmptType, superSuperProductCmptType);
    }

    private void checkExpectationsForPullUpAttribute(IAttribute attribute, IType originalType, IType targetType) {
        // Check that attribute no longer exists in original type
        assertNull(originalType.getAttribute(attribute.getName()));

        // Check that attribute exists in target type
        assertNotNull(targetType.getAttribute(attribute.getName()));
    }

}
