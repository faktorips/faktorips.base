/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;

public abstract class ProductCmptTypeBuilderTest extends AbstractStdBuilderTest {

    protected final static String PRODUCT_NAME = "ProductCmptType";

    protected final static String POLICY_NAME = "PolicyCmptType";

    protected GenProductCmptType genProductCmptType;

    protected IProductCmptType productCmptType;

    protected IPolicyCmptType policyCmptType;

    protected IType javaClassConfiguredPolicy;

    protected IType javaInterfaceConfiguredPolicy;

    protected IType javaClassGeneration;

    protected IType javaInterfaceGeneration;

    protected IType javaClass;

    protected IType javaInterface;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        productCmptType = newProductCmptType(ipsProject, PRODUCT_NAME);
        policyCmptType = newPolicyCmptType(ipsProject, POLICY_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        genProductCmptType = new GenProductCmptType(productCmptType,
                (StandardBuilderSet)ipsProject.getIpsArtefactBuilderSet());

        javaClassConfiguredPolicy = getGeneratedJavaType(policyCmptType, false,
                StandardBuilderSet.KIND_POLICY_CMPT_TYPE_IMPL, POLICY_NAME);
        javaInterfaceConfiguredPolicy = getGeneratedJavaType(policyCmptType, false,
                StandardBuilderSet.KIND_POLICY_CMPT_TYPE_INTERFACE, POLICY_NAME);
        javaClassGeneration = getGeneratedJavaType(productCmptType, false,
                StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_GENERATION_IMPL, PRODUCT_NAME + "Gen");
        javaInterfaceGeneration = getGeneratedJavaType(productCmptType, false,
                StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_GENERATION_INTERFACE, "I" + PRODUCT_NAME + "Gen");
        javaClass = getGeneratedJavaType(productCmptType, false, StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_IMPL,
                PRODUCT_NAME);
        javaInterface = getGeneratedJavaType(productCmptType, false,
                StandardBuilderSet.KIND_PRODUCT_CMPT_TYPE_INTERFACE, "I" + PRODUCT_NAME);
    }

}
