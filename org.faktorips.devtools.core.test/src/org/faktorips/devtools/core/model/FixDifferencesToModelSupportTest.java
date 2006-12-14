/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.ConfigElementType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesToModelSupportTest extends AbstractIpsPluginTest {

    private IProductCmpt product;
    private IProductCmpt product2;
    private IPolicyCmptType testType;

    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject prj = super.newIpsProject("TestProject");
        testType = newPolicyCmptType(prj, "TestType");
        IAttribute a1 = testType.newAttribute();
        a1.setName("A1");
        
        product = newProductCmpt(prj, "TestProduct");
        product.setPolicyCmptType(testType.getQualifiedName());
        IProductCmptGeneration gen = (IProductCmptGeneration)product.newGeneration();
        IConfigElement ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        
        testType.newAttribute().setName("A2");
        
        product2 = newProductCmpt(prj, "TestProduct2");
        product2.setPolicyCmptType(testType.getQualifiedName());
        gen = (IProductCmptGeneration)product2.newGeneration();
        ce1 = gen.newConfigElement();
        ce1.setPcTypeAttribute("A1");
        ce1.setType(ConfigElementType.POLICY_ATTRIBUTE);
        IConfigElement ce2 = gen.newConfigElement();
        ce2.setPcTypeAttribute("A2");
        ce2.setType(ConfigElementType.POLICY_ATTRIBUTE);
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#containsDifferenceToModel()}.
     * @throws CoreException 
     */
    public void testContainsDifferenceToModel() throws CoreException {
        assertEquals(true, product.containsDifferenceToModel());
        assertEquals(false, product2.containsDifferenceToModel());
        testType.getAttribute("A2").delete();
        assertEquals(false, product.containsDifferenceToModel());
        assertEquals(true, product2.containsDifferenceToModel());
       
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.model.IFixDifferencesToModelSupport#fixAllDifferencesToModel()}.
     * @throws CoreException 
     */
    public void testFixAllDifferencesToModel() throws CoreException {
        assertEquals(true, product.containsDifferenceToModel());
        product.fixAllDifferencesToModel();
        assertEquals(false, product.containsDifferenceToModel());
        
        assertEquals(false, product2.containsDifferenceToModel());
        product2.fixAllDifferencesToModel();
        assertEquals(false, product2.containsDifferenceToModel());
    }

}
