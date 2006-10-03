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

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.RangeValueSet;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.ValueSetType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * 
 * @author Jan Ortmann
 */
public class EmptyProductCmptTypeNameTest extends AbstractIpsPluginTest {

    /**
     * 
     */
    public EmptyProductCmptTypeNameTest() {
        super();
    }

    /**
     * @param name
     */
    public EmptyProductCmptTypeNameTest(String name) {
        super(name);
    }
    
    /**
     * Tests if the build finishes without throwing an exception if the product component type name
     * is missing
     * 
     * @throws Exception
     */
    public void test() throws Exception {
        IIpsProject project = newIpsProject();
        IPolicyCmptType type = newPolicyCmptType(project, "Policy");
        type.setConfigurableByProductCmptType(true);
        type.setUnqualifiedProductCmptType(""); // missing product component type name!
        
        IAttribute a = type.newAttribute();
        a.setName("a");
        a.setDatatype("Integer");
        a.setProductRelevant(true);
        a.setValueSetType(ValueSetType.RANGE);
        a.setDefaultValue("0");
        RangeValueSet range = (RangeValueSet)a.getValueSet();
        range.setLowerBound("0");
        range.setUpperBound("10");
        assertTrue(a.isValid());
        
        IRelation r = type.newRelation();
        IPolicyCmptType target = newPolicyCmptType(project, "Target");
        target.setConfigurableByProductCmptType(true);
        target.setUnqualifiedProductCmptType("TargetType");
        target.getIpsSrcFile().save(true, null);
        r.setTarget(target.getQualifiedName());
        r.setMinCardinality(1);
        r.setMaxCardinality(1);
        r.setTargetRoleSingular("Target");
        r.setTargetRolePlural("Targets");
        r.setProductRelevant(true);
        r.setMinCardinalityProductSide(1);
        r.setMaxCardinalityProductSide(1);
        r.setTargetRoleSingularProductSide("TargetType");
        r.setTargetRolePluralProductSide("TargetTypes");
        assertTrue(r.isValid());
        
        type.getIpsSrcFile().save(true, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
    }

}
