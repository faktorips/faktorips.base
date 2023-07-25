/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.junit.Before;
import org.junit.Test;

public class IpsExtendableVersionManagerTest extends AbstractIpsPluginTest {

    private IpsExtendableVersionManager ipsExtendableVersionManager;
    private AVersion currentVersion;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation1;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        currentVersion = AVersion.parse(IpsPlugin.getInstalledFaktorIpsVersion()).majorMinorPatch();
        ipsExtendableVersionManager = new IpsExtendableVersionManager() {
            @Override
            public String getCurrentVersion() {
                return currentVersion.toString();
            }
        };
    }

    @Test
    public void testCompareToCurrentVersion() throws Exception {
        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion(currentVersion.toString() + ".zzz") > 0);
        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion("0.0.0") < 0);
        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion(currentVersion.toString()) == 0);
    }

    @Test
    public void testGetMigrationOperations() throws Exception {
        mockMigrationOperations();

        IIpsProject ipsProject = mock(IIpsProject.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn(
                currentVersion.toString());

        AbstractIpsProjectMigrationOperation[] migrationOperations = ipsExtendableVersionManager
                .getMigrationOperations(ipsProject);
        // same major.minor.patch but not release (zzz) therefore 1
        assertEquals(1, migrationOperations.length);

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn(
                "1.0.0");
        migrationOperations = ipsExtendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(0, migrationOperations.length);

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn(
                "0.0.1");
        migrationOperations = ipsExtendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(1, migrationOperations.length);
        assertEquals(ipsProjectMigrationOperation1, migrationOperations[0]);
    }

    @Test
    public void testGetMigrationOperationsOnlyShowsLatest() throws Exception {
        currentVersion = AVersion.parse("24.1.0.qualifier");
        mockMigrationOperations("22.12", "23.6", "24.1");

        IIpsProject ipsProject = mock(IIpsProject.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber("org.faktorips.feature"))
                .thenReturn("23.6");

        AbstractIpsProjectMigrationOperation[] migrationOperations = ipsExtendableVersionManager
                .getMigrationOperations(ipsProject);
        assertEquals(1, migrationOperations.length);
        assertEquals("24.1", migrationOperations[0].getTargetVersion());
    }

    @Test
    public void testIsCurrentVersionCompatibleWith() throws Exception {
        mockMigrationOperations();

        assertTrue(ipsExtendableVersionManager.isCurrentVersionCompatibleWith(currentVersion.toString()));
        assertTrue(ipsExtendableVersionManager.isCurrentVersionCompatibleWith("1.0.0"));

        assertFalse(ipsExtendableVersionManager.isCurrentVersionCompatibleWith("0.0.1"));
        assertFalse(ipsExtendableVersionManager.isCurrentVersionCompatibleWith(currentVersion + ".zzz"));
    }

    private void mockMigrationOperations() {
        ipsProjectMigrationOperation1 = mockMigrationOperation("0.1.0");
        ipsProjectMigrationOperation2 = mockMigrationOperation(currentVersion + ".zzz");
        mockMigrationOperations(Stream.of(ipsProjectMigrationOperation1, ipsProjectMigrationOperation2));
    }

    private void mockMigrationOperations(String... versions) {
        mockMigrationOperations(Arrays.stream(versions).map(this::mockMigrationOperation));
    }

    private AbstractIpsProjectMigrationOperation mockMigrationOperation(String version) {
        AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation = mock(
                AbstractIpsProjectMigrationOperation.class);
        when(ipsProjectMigrationOperation.getTargetVersion()).thenReturn(version);
        return ipsProjectMigrationOperation;
    }

    private void mockMigrationOperations(Stream<AbstractIpsProjectMigrationOperation> operations) {
        Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations = operations.collect(Collectors.toMap(
                (Function<AbstractIpsProjectMigrationOperation, AVersion>)o -> AVersion.parse(o.getTargetVersion()),
                (Function<AbstractIpsProjectMigrationOperation, IIpsProjectMigrationOperationFactory>)o -> (_1,
                        _2) -> o));
        ipsExtendableVersionManager.setRegisteredMigrations(registeredMigrations);
    }
}
