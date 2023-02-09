/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.eclipse.internal;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.model.builder.JaxbSupportVariant;
import org.faktorips.devtools.model.eclipse.internal.IpsClasspathContainerInitializer.IpsClasspathContainer;
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
    public void testNewDefaultEntryPath() {
        IPath expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(false, false, JaxbSupportVariant.None));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(true, false, JaxbSupportVariant.None));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.GROOVY_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(false, true, JaxbSupportVariant.None));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE + "," + IpsClasspathContainerInitializer.GROOVY_BUNDLE);
        assertEquals(expected, IpsClasspathContainerInitializer.newEntryPath(true, true, JaxbSupportVariant.None));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE + "," + IpsClasspathContainerInitializer.GROOVY_BUNDLE
                + "," + IpsClasspathContainerInitializer.CLASSIC_JAXB_BUNDLE);
        assertEquals(expected,
                IpsClasspathContainerInitializer.newEntryPath(true, true, JaxbSupportVariant.ClassicJAXB));

        expected = new Path(IpsClasspathContainerInitializer.CONTAINER_ID + "/"
                + IpsClasspathContainerInitializer.JODA_BUNDLE + "," + IpsClasspathContainerInitializer.GROOVY_BUNDLE
                + "," + IpsClasspathContainerInitializer.JAKARTA_BUNDLE);
        assertEquals(expected,
                IpsClasspathContainerInitializer.newEntryPath(true, true, JaxbSupportVariant.JakartaXmlBinding3));
    }

    @Test
    public void testGetSourceBundlePath() throws Exception {
        if (Abstractions.isEclipseRunning()) {
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

    @Test
    public void testGetSourceBundlePath_2() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            IpsClasspathContainer ipsClasspathContainer = new IpsClasspathContainerInitializer.IpsClasspathContainer(
                    new Path("faktorips"));
            String sourceBundlePath = ipsClasspathContainer
                    .getSourceBundlePath(
                            "/home/any-folder/eclipse_test/eclipse/plugins/org/faktorips/runtime/java5/org.faktorips.runtime.java5_1.2.3.test_vla-jztd",
                            "org.faktorips.runtime.java5");
            assertEquals(
                    "/home/any-folder/eclipse_test/eclipse/plugins/org/faktorips/runtime/java5/org.faktorips.runtime.java5.source_1.2.3.test_vla-jztd",
                    sourceBundlePath);
        }
    }

    @Test
    public void testGetSourceBundlePath_3() throws Exception {
        if (Abstractions.isEclipseRunning()) {
            IpsClasspathContainer ipsClasspathContainer = new IpsClasspathContainerInitializer.IpsClasspathContainer(
                    new Path("faktorips"));
            String sourceBundlePath = ipsClasspathContainer
                    .getSourceBundlePath(
                            "/home/any-folder/eclipse_test/eclipse/plugins/org.faktorips.runtime.java5/org.faktorips+.runtime.java5_1.2.3.test_vla-jztd",
                            "org.faktorips+.runtime.java5");
            assertEquals(
                    "/home/any-folder/eclipse_test/eclipse/plugins/org.faktorips.runtime.java5/org.faktorips+.runtime.java5.source_1.2.3.test_vla-jztd",
                    sourceBundlePath);
        }
    }

}
