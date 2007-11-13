/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Arrays;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;

public class ConfigElementComparatorTest extends AbstractIpsPluginTest {

    private IProductCmptGeneration generation;
    private ConfigElementComparator comparator;
    private IIpsProject ipsProject;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IPolicyCmptType superSuperType = newPolicyAndProductCmptType(ipsProject, "SuperSuperPolicy", "SuperSuperProduct");
        IPolicyCmptType superType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        IPolicyCmptType childType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        
        childType.setSupertype(superType.getQualifiedName());
        superType.setSupertype(superSuperType.getQualifiedName());
        
        superSuperType.newPolicyCmptTypeAttribute().setName("SS");
        superType.newPolicyCmptTypeAttribute().setName("S1");
        superType.newPolicyCmptTypeAttribute().setName("S2");
        childType.newPolicyCmptTypeAttribute().setName("C");
        
        IProductCmpt product = newProductCmpt(childType.findProductCmptType(ipsProject), "Product");
        generation = product.getProductCmptGeneration(0);
        
        comparator = new ConfigElementComparator(ipsProject);
    }

    public void testCompare() {
        IConfigElement[] elements = new IConfigElement[4];
        elements[0] = generation.newConfigElement();
        elements[1] = generation.newConfigElement();
        elements[2] = generation.newConfigElement();
        elements[3] = generation.newConfigElement();
        
        elements[0].setPolicyCmptTypeAttribute("C");
        elements[1].setPolicyCmptTypeAttribute("S2");
        elements[2].setPolicyCmptTypeAttribute("S1");
        elements[3].setPolicyCmptTypeAttribute("SS");
        
        Arrays.sort(elements, comparator);
        
        assertEquals("SS", elements[0].getPolicyCmptTypeAttribute());
        assertEquals("S1", elements[1].getPolicyCmptTypeAttribute());
        assertEquals("S2", elements[2].getPolicyCmptTypeAttribute());
        assertEquals("C", elements[3].getPolicyCmptTypeAttribute());
    }
    
    
}
