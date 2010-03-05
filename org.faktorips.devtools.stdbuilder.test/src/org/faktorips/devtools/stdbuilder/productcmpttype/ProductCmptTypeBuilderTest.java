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

package org.faktorips.devtools.stdbuilder.productcmpttype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public abstract class ProductCmptTypeBuilderTest extends AbstractStdBuilderTest {

    protected final static String PRODUCT_NAME = "ProductCmptType";

    protected final static String POLICY_NAME = "PolicyCmptType";

    protected IProductCmptType productCmptType;

    protected GenProductCmptType genProductCmptType;

    private IPolicyCmptType policyCmptType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        productCmptType = newProductCmptType(ipsProject, PRODUCT_NAME);
        policyCmptType = newPolicyCmptType(ipsProject, POLICY_NAME);
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        genProductCmptType = new GenProductCmptType(productCmptType, (StandardBuilderSet)ipsProject
                .getIpsArtefactBuilderSet());
    }

    protected final IType getGeneratedJavaType(boolean publishedSource) {
        return (publishedSource) ? getGeneratedJavaInterface() : getGeneratedJavaClass();
    }

    private IType getGeneratedJavaClass() {
        return getGeneratedJavaType(productCmptType, false, true, PRODUCT_NAME);
    }

    private IType getGeneratedJavaInterface() {
        return getGeneratedJavaType(productCmptType, false, false, "I" + PRODUCT_NAME);
    }

}
