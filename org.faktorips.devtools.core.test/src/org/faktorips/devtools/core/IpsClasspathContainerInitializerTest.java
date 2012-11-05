/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.core.IpsClasspathContainerInitializer.IpsClasspathContainer;
import org.junit.Test;

public class IpsClasspathContainerInitializerTest {

    @Test
    public void testGetAdditionalBundleIds_NullBehaviour_for_ClasspathEntry() {
        String[] ids = IpsClasspathContainerInitializer.getAdditionalBundleIds((IClasspathEntry)null);
        assertEquals(0, ids.length);
    }

    @Test
    public void testGetAdditionalBundleIds_NullBehaviour_for_Path() {
        String[] ids = IpsClasspathContainerInitializer.getAdditionalBundleIds((IPath)null);
        assertEquals(0, ids.length);
    }

    @Test
    public void testGetAdditionalBundleIds() {
        IPath path = new Path(IpsClasspathContainerInitializer.CONTAINER_ID);
        String[] bundles = IpsClasspathContainerInitializer.getAdditionalBundleIds(path);
        assertEquals(0, bundles.length);

        path = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/bundle1");
        bundles = IpsClasspathContainerInitializer.getAdditionalBundleIds(path);
        assertEquals(1, bundles.length);
        assertEquals("bundle1", bundles[0]);

        path = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/bundle1,bundle2");
        bundles = IpsClasspathContainerInitializer.getAdditionalBundleIds(path);
        assertEquals(2, bundles.length);
        assertEquals("bundle1", bundles[0]);
        assertEquals("bundle2", bundles[1]);
    }

    @Test
    public void testNewEntryPath() {
        IPath expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(false, false));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(true, false));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.GROOVY_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(false, true));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE + "," + IpsClasspathContainerInitializer.GROOVY_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(true, true));
    }

    @Test
    public void testGetSourceBundlePath() throws Exception {
        IpsClasspathContainer ipsClasspathContainer = new IpsClasspathContainerInitializer.IpsClasspathContainer(
                new Path("faktorips"));
        String sourceBundlePath = ipsClasspathContainer.getSourceBundlePath(
                "/home/any-folder/eclipse_test/eclipse/plugins/org.faktorips.runtime.java5_1.2.3.test_vla-jztd",
                "org.faktorips.runtime.java5");
        assertEquals(
                "/home/any-folder/eclipse_test/eclipse/plugins/org.faktorips.runtime.java5.source_1.2.3.test_vla-jztd",
                sourceBundlePath);
    }

}
