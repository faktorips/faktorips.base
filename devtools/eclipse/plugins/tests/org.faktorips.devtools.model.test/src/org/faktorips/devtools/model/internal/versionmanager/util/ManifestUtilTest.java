/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.versionmanager.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.model.internal.versionmanager.util.ManifestUtil.ManifestFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ManifestUtilTest {

    @Mock
    private Manifest manifest;

    @Mock
    private Attributes attributes;

    @Mock
    private AFile file;

    @Mock
    private ManifestFactory manifestFactory;

    private static final String MY_REQUIRE_BUNDLE = "my.Require.Bundle";
    private static final String MY_REQUIRE_BUNDLE_VERSION = MY_REQUIRE_BUNDLE + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE;

    private static final String MY_REQUIRE_BUNDLE_WITH_VERSION = MY_REQUIRE_BUNDLE_VERSION + "=\"[3.9.0,3.10.0)\"";
    private static final String OTHER_BUNDLE1_WITH_VERSION = "other.Require.Bundle1" + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"3.4.0\"";

    private static final String OTHER_BUNDLE2_WITH_VERSION = "other.Require.Bundle2" + ";"
            + Constants.BUNDLE_VERSION_ATTRIBUTE + "=\"3.4.0\"";
    private static final String START_OTHER_BUNDLE = OTHER_BUNDLE1_WITH_VERSION + ",";
    private static final String END_OTHER_BUNDLE = "," + OTHER_BUNDLE2_WITH_VERSION;
    private static final String VISIBILITY_REEXPORT = ";visibility:=reexport";

    private static final Version VERSION_3_10 = new Version("3.10.0");
    private static final Version VERSION_3_11 = new Version("3.11.0");
    private static final VersionRange RANGE1 = new VersionRange(VERSION_3_10, true, VERSION_3_11, true);
    private static final VersionRange RANGE2 = new VersionRange(VERSION_3_10, true, VERSION_3_11, false);

    private AutoCloseable openMocks;

    @Before
    public void mockManifest() throws Exception {
        openMocks = MockitoAnnotations.openMocks(this);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        when(manifestFactory.loadManifest(file)).thenReturn(manifest);
    }

    @After
    public void releaseMocks() throws Exception {
        openMocks.close();
    }

    @Test(expected = NullPointerException.class)
    public void testManifestNull() throws IOException {
        new ManifestUtil(null, manifestFactory);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPluginDependencyPluginNull() throws IOException {
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(null, RANGE1);
    }

    @Test(expected = NullPointerException.class)
    public void testSetPluginDependencyVersionRangeNull() throws IOException {
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, null);
    }

    @Test
    public void testSetPluginDependencyIncludeMaxVersion() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(MY_REQUIRE_BUNDLE_WITH_VERSION);
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE1);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE, MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0]\"");
    }

    @Test
    public void testSetPluginDependencyExcludeMaxVersion() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(
                MY_REQUIRE_BUNDLE_WITH_VERSION + VISIBILITY_REEXPORT);
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"" + VISIBILITY_REEXPORT);
    }

    @Test
    public void testSetPluginDependencyInside() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_WITH_VERSION + END_OTHER_BUNDLE);
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"" + END_OTHER_BUNDLE);
    }

    @Test
    public void testSetPluginDependencyNotfound() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(OTHER_BUNDLE1_WITH_VERSION);
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE,
                START_OTHER_BUNDLE + MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"");
    }

    @Test
    public void testSetPluginDependencyEmpty() throws IOException {
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(null);
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE2);
        verify(attributes).putValue(Constants.REQUIRE_BUNDLE, MY_REQUIRE_BUNDLE_VERSION + "=\"[3.10.0,3.11.0)\"");
    }

    @Test
    public void testWriteManifest() throws IOException {
        ManifestUtil migrationUtil = createMigrationManifestUtil();
        when(attributes.getValue(Constants.REQUIRE_BUNDLE)).thenReturn(MY_REQUIRE_BUNDLE_WITH_VERSION);
        migrationUtil.setPluginDependency(MY_REQUIRE_BUNDLE, RANGE1);
        migrationUtil.writeManifest();
        verify(manifest).write(any(ByteArrayOutputStream.class));
        verify(file).setContents(any(ByteArrayInputStream.class), eq(true), any(NullProgressMonitor.class));
    }

    private ManifestUtil createMigrationManifestUtil() throws IOException {
        return new ManifestUtil(file, manifestFactory);
    }
}
