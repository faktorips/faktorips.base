/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.abstracttest.builder.TestIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.util.LocalizedStringsSet;
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
        assertEquals(a, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).get(0));
        assertEquals(b, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).get(1));
        assertEquals(c, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).get(2));
        assertEquals(d, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).get(3));
        assertEquals(e, builderSet.getBuildersByClass(StubJavaSourceFileBuilder.class).get(4));
        assertEquals(1, builderSet.getBuildersByClass(A.class).size());
        assertEquals(a, builderSet.getBuildersByClass(A.class).get(0));
        assertEquals(1, builderSet.getBuildersByClass(B.class).size());
        assertEquals(b, builderSet.getBuildersByClass(B.class).get(0));
        assertEquals(3, builderSet.getBuildersByClass(C.class).size());
        assertEquals(c, builderSet.getBuildersByClass(C.class).get(0));
        assertEquals(d, builderSet.getBuildersByClass(C.class).get(1));
        assertEquals(e, builderSet.getBuildersByClass(C.class).get(2));
        assertEquals(1, builderSet.getBuildersByClass(DExtendsC.class).size());
        assertEquals(d, builderSet.getBuildersByClass(DExtendsC.class).get(0));
        assertEquals(1, builderSet.getBuildersByClass(EExtendsC.class).size());
        assertEquals(e, builderSet.getBuildersByClass(EExtendsC.class).get(0));
    }

    class A extends StubJavaSourceFileBuilder {

        public A() throws CoreException {
            super(new TestIpsArtefactBuilderSet(), "", new LocalizedStringsSet(StubJavaSourceFileBuilder.class));
        }

    }

    class B extends StubJavaSourceFileBuilder {

        public B() throws CoreException {
            super(new TestIpsArtefactBuilderSet(), "", new LocalizedStringsSet(StubJavaSourceFileBuilder.class));
        }

    }

    class C extends StubJavaSourceFileBuilder {

        public C() throws CoreException {
            super(new TestIpsArtefactBuilderSet(), "", new LocalizedStringsSet(StubJavaSourceFileBuilder.class));
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
            super(builderSet, "", new LocalizedStringsSet(StubJavaSourceFileBuilder.class));
        }

    }

    private static class StubJavaSourceFileBuilder extends JavaSourceFileBuilder {

        public boolean isBuilderFor = false;

        public StubJavaSourceFileBuilder(IIpsArtefactBuilderSet builderSet, String kindId,
                LocalizedStringsSet localizedStringsSet) {

            super(builderSet, kindId, localizedStringsSet);
        }

        @Override
        protected String generate() throws CoreException {
            return "";
        }

        @Override
        public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) {
            return isBuilderFor;
        }

        public void reset() {
            isBuilderFor = false;
        }

        @Override
        protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
                IIpsObjectPartContainer ipsObjectPartContainer) {

        }

        @Override
        public boolean isBuildingPublishedSourceFile() {
            return false;
        }

    }

}
