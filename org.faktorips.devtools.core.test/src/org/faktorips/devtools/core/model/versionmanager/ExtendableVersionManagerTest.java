/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.versionmanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.junit.Before;
import org.junit.Test;

public class ExtendableVersionManagerTest {

    private static final String MY_FEATURE_ID = "myFeatureId";
    private ExtendableVersionManager extendableVersionManager;
    private AVersion currentVersion;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation1;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation2;

    @Before
    public void setUp() throws Exception {
        extendableVersionManager = new ExtendableVersionManager() {

            @Override
            protected AVersion getVersion() {
                return AVersion.parse("1.0.0.test");
            }

            @Override
            public String getFeatureId() {
                return MY_FEATURE_ID;
            }
        };
        currentVersion = AVersion.parse(extendableVersionManager.getCurrentVersion());
    }

    @Test
    public void testCompareToCurrentVersion() {
        assertTrue(extendableVersionManager.compareToCurrentVersion(currentVersion + "zzz") > 0);
        assertTrue(extendableVersionManager.compareToCurrentVersion("0.0.0") < 0);
        assertTrue(extendableVersionManager.compareToCurrentVersion(currentVersion.toString()) == 0);
    }

    @Test
    public void testGetMigrationOperations() throws Exception {
        mockMigrationOperations();

        IIpsProject ipsProject = mock(IIpsProject.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber(MY_FEATURE_ID)).thenReturn(
                currentVersion.toString());

        AbstractIpsProjectMigrationOperation[] migrationOperations = extendableVersionManager
                .getMigrationOperations(ipsProject);

        assertEquals(0, migrationOperations.length);

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber(MY_FEATURE_ID)).thenReturn("0.0.0");
        migrationOperations = extendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(0, migrationOperations.length);

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber(MY_FEATURE_ID)).thenReturn("1.0.0");
        migrationOperations = extendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(0, migrationOperations.length);

        when(ipsProject.getReadOnlyProperties().getMinRequiredVersionNumber(MY_FEATURE_ID)).thenReturn("0.0.1");
        migrationOperations = extendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(1, migrationOperations.length);
        assertEquals(ipsProjectMigrationOperation1, migrationOperations[0]);
    }

    @Test
    public void testIsCurrentVersionCompatibleWith() {
        mockMigrationOperations();

        assertTrue(extendableVersionManager.isCurrentVersionCompatibleWith(currentVersion.toString()));
        assertTrue(extendableVersionManager.isCurrentVersionCompatibleWith("1.0.0"));

        assertFalse(extendableVersionManager.isCurrentVersionCompatibleWith("0.0.1"));
        assertFalse(extendableVersionManager.isCurrentVersionCompatibleWith(currentVersion + "zzz"));
    }

    private void mockMigrationOperations() {
        ipsProjectMigrationOperation1 = mock(AbstractIpsProjectMigrationOperation.class);
        when(ipsProjectMigrationOperation1.getTargetVersion()).thenReturn("0.1.0");

        ipsProjectMigrationOperation2 = mock(AbstractIpsProjectMigrationOperation.class);
        when(ipsProjectMigrationOperation2.getTargetVersion()).thenReturn(currentVersion + "zzz");

        Map<AVersion, IIpsProjectMigrationOperationFactory> registeredMigrations = new HashMap<>();
        registeredMigrations.put(AVersion.parse(ipsProjectMigrationOperation1.getTargetVersion()),
                (ipsProject, featureId) -> ipsProjectMigrationOperation1);
        registeredMigrations.put(AVersion.parse(ipsProjectMigrationOperation2.getTargetVersion()),
                (ipsProject, featureId) -> ipsProjectMigrationOperation2);
        extendableVersionManager.setRegisteredMigrations(registeredMigrations);
    }

}
