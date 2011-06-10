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

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Test;

public class PullUpAttributeProcessorIntegrationTest extends AbstractIpsRefactoringTest {

    @Test
    public void testPullUpPolicyCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(policyCmptTypeAttribute, superPolicyCmptType);

        checkExpectationsForPullUpAttribute(policyCmptTypeAttribute, policyCmptType, superPolicyCmptType);
    }

    @Test
    public void testPullUpPolicyCmptTypeAttributeFurtherUpInHierarchy() throws CoreException {
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
    public void testPullUpProductCmptTypeAttribute() throws CoreException {
        performPullUpRefactoring(productCmptTypeAttribute, superProductCmptType);

        checkExpectationsForPullUpAttribute(productCmptTypeAttribute, productCmptType, superProductCmptType);
    }

    @Test
    public void testPullUpProductCmptTypeAttributeFurtherUpInHierarchy() throws CoreException {
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
