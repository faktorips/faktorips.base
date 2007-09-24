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

package org.faktorips.devtools.core.internal.model.type;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.Modifier;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.type.IMethod;

/**
 * 
 * @author Jan Ortmann
 */
public class MethodTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IMethod method;
    
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        IProductCmptType type = newProductCmptType(ipsProject, "Product");
        method = type.newMethod();
    }
    
    
    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setName(java.lang.String)}.
     */
    public void testSetName() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_NAME, method, "calcPremium");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setDatatype(java.lang.String)}.
     */
    public void testSetDatatype() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_DATATYPE, method, "Integer");
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setAbstract(boolean)}.
     */
    public void testSetAbstract() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_ABSTRACT, method, Boolean.TRUE);
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.type.Method#setModifier(org.faktorips.devtools.core.model.pctype.Modifier)}.
     */
    public void testSetModifier() {
        testPropertyAccessReadWrite(Method.class, IMethod.PROPERTY_MODIFIER, method, Modifier.PUBLIC);
    }

}
