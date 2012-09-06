/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.builder;

import static org.junit.Assert.assertEquals;

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
        assertEquals(0, builderSet.getBuilderByClass(NotInBuilderSer.class).size());
        assertEquals(5, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).size());
        assertEquals(a, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).get(0));
        assertEquals(b, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).get(1));
        assertEquals(c, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).get(2));
        assertEquals(d, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).get(3));
        assertEquals(e, builderSet.getBuilderByClass(StubJavaSourceFileBuilder.class).get(4));
        assertEquals(1, builderSet.getBuilderByClass(A.class).size());
        assertEquals(a, builderSet.getBuilderByClass(A.class).get(0));
        assertEquals(1, builderSet.getBuilderByClass(B.class).size());
        assertEquals(b, builderSet.getBuilderByClass(B.class).get(0));
        assertEquals(3, builderSet.getBuilderByClass(C.class).size());
        assertEquals(c, builderSet.getBuilderByClass(C.class).get(0));
        assertEquals(d, builderSet.getBuilderByClass(C.class).get(1));
        assertEquals(e, builderSet.getBuilderByClass(C.class).get(2));
        assertEquals(1, builderSet.getBuilderByClass(DExtendsC.class).size());
        assertEquals(d, builderSet.getBuilderByClass(DExtendsC.class).get(0));
        assertEquals(1, builderSet.getBuilderByClass(EExtendsC.class).size());
        assertEquals(e, builderSet.getBuilderByClass(EExtendsC.class).get(0));
    }

    class A extends StubJavaSourceFileBuilder {

        public A() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    class B extends StubJavaSourceFileBuilder {

        public B() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    class C extends StubJavaSourceFileBuilder {

        public C() throws CoreException {
            super(new TestIpsArtefactBuilderSet());
        }

    }

    class DExtendsC extends C {

        public DExtendsC() throws CoreException {
            super();
        }

    }

    class EExtendsC extends C {

        public EExtendsC() throws CoreException {
            super();
        }

    }

    class NotInBuilderSer extends StubJavaSourceFileBuilder {

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
        public boolean isBuildingInternalArtefacts() {
            // Auto-generated method stub
            return false;
        }

    }

}
