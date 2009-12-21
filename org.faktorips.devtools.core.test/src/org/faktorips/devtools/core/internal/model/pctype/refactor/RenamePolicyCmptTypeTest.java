/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.pctype.refactor;

import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;

public class RenamePolicyCmptTypeTest extends AbstractIpsRefactoringTest {

    private static final String POLICY_NAME = "Policy";

    private static final String PRODUCT_NAME = "Product";

    private IPolicyCmptType superPolicyCmptType;

    private IProductCmptType superProductCmptType;

    private IPolicyCmptType policyCmptType;

    private IProductCmptType productCmptType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create super policy component type.
        superPolicyCmptType = newPolicyCmptType(ipsProject, "SuperPolicy");
        superPolicyCmptType.setAbstract(true);
        superPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create super product component type.
        superProductCmptType = newProductCmptType(ipsProject, "SuperProduct");
        superProductCmptType.setAbstract(true);
        superProductCmptType.setConfigurationForPolicyCmptType(true);
        superProductCmptType.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
        superPolicyCmptType.setProductCmptType(superProductCmptType.getQualifiedName());

        // Create concrete policy component type.
        policyCmptType = newPolicyCmptType(ipsProject, POLICY_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        // Create concrete product component type.
        productCmptType = newProductCmptType(ipsProject, PRODUCT_NAME);
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        productCmptType.setSupertype(superProductCmptType.getQualifiedName());
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
    }

    public void testRenamePolicyCmptType() {
        // TODO AW: Implement test.
    }

}
