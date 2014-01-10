/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Cornelius.Dirmeier
 */
public class AbstractBuilderSetTest extends AbstractIpsPluginTest {

    private TestIpsArtefactBuilderSet builderSet;
    private A a;
    private B b;
    private C c;
    private DExtendsC d;
    private EExtendsC e;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        a = new A();
        b = new B();
        c = new C();
        d = new DExtendsC();
        e = new EExtendsC();
        builderSet = new TestIpsArtefactBuilderSet(new IIpsArtefactBuilder[] { a, b, c, d, e });
    }

    @Test
    public void testGetBuilderByClass() {
        assertEquals(0, builderSet.getBuildersByClass(NotInBuilderSer.class).size());
        assertEquals(5, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).size());
        assertThat(builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class),
                hasItem((StubJavaSourceFileBuilder)a));
        assertThat(builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class),
                hasItem((StubJavaSourceFileBuilder)b));
        assertThat(builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class),
                hasItem((StubJavaSourceFileBuilder)c));
        assertThat(builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class),
                hasItem((StubJavaSourceFileBuilder)d));
        assertThat(builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class),
                hasItem((StubJavaSourceFileBuilder)e));
        assertEquals(1, builderSet.getBuildersByClass(A.class).size());
        assertEquals(a, builderSet.getBuildersByClass(A.class).get(0));
        assertEquals(1, builderSet.getBuildersByClass(B.class).size());
        assertEquals(b, builderSet.getBuildersByClass(B.class).get(0));
        assertEquals(3, builderSet.getBuildersByClass(C.class).size());
        assertThat(builderSet.getBuildersByClass(C.class), hasItem(c));
        assertThat(builderSet.getBuildersByClass(C.class), hasItem((C)d));
        assertThat(builderSet.getBuildersByClass(C.class), hasItem((C)e));
        assertEquals(1, builderSet.getBuildersByClass(DExtendsC.class).size());
        assertEquals(d, builderSet.getBuildersByClass(DExtendsC.class).get(0));
        assertEquals(1, builderSet.getBuildersByClass(EExtendsC.class).size());
        assertEquals(e, builderSet.getBuildersByClass(EExtendsC.class).get(0));
    }

    static class A extends StubJavaSourceFileBuilder {

        public A() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class B extends StubJavaSourceFileBuilder {

        public B() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class C extends StubJavaSourceFileBuilder {

        public C() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class DExtendsC extends C {

        public DExtendsC() throws CoreException {
            super();
        }

    }

    static class EExtendsC extends C {

        public EExtendsC() throws CoreException {
            super();
        }

    }

    static class NotInBuilderSer extends StubJavaSourceFileBuilder {

        public NotInBuilderSer(IIpsArtefactBuilderSet builderSet) {
            super(builderSet);
        }

    }

    private static class StubJavaSourceFileBuilder extends AbstractArtefactBuilder {

        public boolean isBuilderFor = false;

        public StubJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet) {
            super(builderSet);
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return isBuilderFor;
        }

        public void reset() {
            isBuilderFor = false;
        }

        @Override
        public String getName() {
            // Auto-generated method stub
            return null;
        }

        @Override
        public void build(IIpsSrcFile ipsSrcFile) throws CoreException {
            // Auto-generated method stub

        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreException {
            // Auto-generated method stub

        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            // Auto-generated method stub
            return false;
        }

    }

}
