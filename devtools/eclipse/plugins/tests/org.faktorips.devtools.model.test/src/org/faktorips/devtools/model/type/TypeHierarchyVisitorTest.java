/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class TypeHierarchyVisitorTest extends AbstractIpsPluginTest {

    private IType type;
    private IType supertype;
    private IType superSupertype;
    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        type = newProductCmptType(ipsProject, "Type");
        supertype = newProductCmptType(ipsProject, "Supertype");
        superSupertype = newProductCmptType(ipsProject, "SuperSupertype");

        type.setSupertype("Supertype");
        supertype.setSupertype("SuperSupertype");
    }

    @Test
    public void test_NoCycle() {
        MyVisitor visitor = new MyVisitor(ipsProject);
        visitor.start(type);
        IType[] types = visitor.getVisitedTypes();
        assertEquals(3, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertEquals(superSupertype, types[2]);
        assertFalse(visitor.cycleDetected());

        visitor.stopVisitingAfterThisType = supertype;
        visitor.start(type);
        types = visitor.getVisitedTypes();
        assertEquals(2, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertFalse(visitor.cycleDetected());

        visitor.stopVisitingAfterThisType = null;
        visitor.start(superSupertype);
        types = visitor.getVisitedTypes();
        assertEquals(1, types.length);
        assertEquals(superSupertype, types[0]);
        assertFalse(visitor.cycleDetected());

        visitor.start(null);
        types = visitor.getVisitedTypes();
        assertEquals(0, types.length);
        assertFalse(visitor.cycleDetected());
    }

    @Test
    public void test_WithCycle() {
        superSupertype.setSupertype("Type");
        MyVisitor visitor = new MyVisitor(ipsProject);
        visitor.start(type);
        IType[] types = visitor.getVisitedTypes();
        assertEquals(3, types.length);
        assertEquals(type, types[0]);
        assertEquals(supertype, types[1]);
        assertEquals(superSupertype, types[2]);
        assertTrue(visitor.cycleDetected());

        visitor.start(null);
        types = visitor.getVisitedTypes();
        assertEquals(0, types.length);
        assertFalse(visitor.cycleDetected());
    }

    public class MyVisitor extends TypeHierarchyVisitor<IType> {

        private IType stopVisitingAfterThisType = null;

        public MyVisitor(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IType currentType) {
            if (stopVisitingAfterThisType == null) {
                return true;
            }
            return currentType != stopVisitingAfterThisType;
        }

    }

}
