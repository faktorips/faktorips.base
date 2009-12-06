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

    protected IProductCmptType productCmptType;

    protected IPolicyCmptType policyCmptType;

    protected GenProductCmptType genProductCmptType;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        productCmptType = newProductCmptType(ipsProject, "PolicyCmptType");
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
        genProductCmptType = new GenProductCmptType(productCmptType, (StandardBuilderSet)ipsProject
                .getIpsArtefactBuilderSet());
    }

}
