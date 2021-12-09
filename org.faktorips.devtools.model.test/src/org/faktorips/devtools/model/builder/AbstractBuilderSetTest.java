/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
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

        public A() throws CoreRuntimeException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class B extends StubJavaSourceFileBuilder {

        public B() throws CoreRuntimeException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class C extends StubJavaSourceFileBuilder {

        public C() throws CoreRuntimeException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    static class DExtendsC extends C {

        public DExtendsC() throws CoreRuntimeException {
            super();
        }

    }

    static class EExtendsC extends C {

        public EExtendsC() throws CoreRuntimeException {
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
        public void build(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
            // Auto-generated method stub

        }

        @Override
        public void delete(IIpsSrcFile ipsSrcFile) throws CoreRuntimeException {
            // Auto-generated method stub

        }

        @Override
        public boolean isBuildingInternalArtifacts() {
            // Auto-generated method stub
            return false;
        }

    }

}
