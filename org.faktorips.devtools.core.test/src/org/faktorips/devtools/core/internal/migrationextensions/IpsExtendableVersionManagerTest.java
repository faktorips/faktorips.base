/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.migrationextensions;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.Map;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsProjectMigrationOperation;
import org.faktorips.devtools.core.model.versionmanager.IIpsProjectMigrationOperationFactory;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Version;

public class IpsExtendableVersionManagerTest extends AbstractIpsPluginTest {

    private IpsExtendableVersionManager ipsExtendableVersionManager;
    private Version currentVersion;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation1;
    private AbstractIpsProjectMigrationOperation ipsProjectMigrationOperation2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        ipsExtendableVersionManager = new IpsExtendableVersionManager();
        currentVersion = Version.parseVersion(ipsExtendableVersionManager.getCurrentVersion());
    }

    @Test
    public void testCompareToCurrentVersion() throws Exception {

        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion(currentVersion + "zzz") > 0);
        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion("0.0.0") < 0);
        assertTrue(ipsExtendableVersionManager.compareToCurrentVersion(currentVersion.toString()) == 0);
    }

    @Test
    public void testGetMigrationOperations() throws Exception {
        mockMigrationOperations();

        IpsProject ipsProject = mock(IpsProject.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
        when(ipsProject.getProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn(
                currentVersion.toString());

        AbstractIpsProjectMigrationOperation[] migrationOperations = ipsExtendableVersionManager
                .getMigrationOperations(ipsProject);

        assertEquals(0, migrationOperations.length);

        when(ipsProject.getProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn("1.0.0");
        migrationOperations = ipsExtendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(0, migrationOperations.length);

        when(ipsProject.getProperties().getMinRequiredVersionNumber("org.faktorips.feature")).thenReturn("0.0.1");
        migrationOperations = ipsExtendableVersionManager.getMigrationOperations(ipsProject);
        assertEquals(1, migrationOperations.length);
        assertEquals(ipsProjectMigrationOperation1, migrationOperations[0]);
    }

    @Test
    public void testIsCurrentVersionCompatibleWith() throws Exception {
        mockMigrationOperations();

        assertTrue(ipsExtendableVersionManager.isCurrentVersionCompatibleWith(currentVersion.toString()));
        assertTrue(ipsExtendableVersionManager.isCurrentVersionCompatibleWith("1.0.0"));

        assertFalse(ipsExtendableVersionManager.isCurrentVersionCompatibleWith("0.0.1"));
        assertFalse(ipsExtendableVersionManager.isCurrentVersionCompatibleWith(currentVersion + "zzz"));
    }

    private void mockMigrationOperations() {
        ipsProjectMigrationOperation1 = mock(AbstractIpsProjectMigrationOperation.class);
        when(ipsProjectMigrationOperation1.getTargetVersion()).thenReturn("0.1.0");

        ipsProjectMigrationOperation2 = mock(AbstractIpsProjectMigrationOperation.class);
        when(ipsProjectMigrationOperation2.getTargetVersion()).thenReturn(currentVersion + "zzz");

        Map<Version, IIpsProjectMigrationOperationFactory> registeredMigrations = new HashMap<Version, IIpsProjectMigrationOperationFactory>();
        registeredMigrations.put(new Version(ipsProjectMigrationOperation1.getTargetVersion()),
                new IIpsProjectMigrationOperationFactory() {
                    @Override
                    public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                            String featureId) {
                        return ipsProjectMigrationOperation1;
                    }
                });
        registeredMigrations.put(new Version(ipsProjectMigrationOperation2.getTargetVersion()),
                new IIpsProjectMigrationOperationFactory() {

                    @Override
                    public AbstractIpsProjectMigrationOperation createIpsProjectMigrationOpertation(IIpsProject ipsProject,
                            String featureId) {
                        return ipsProjectMigrationOperation2;
                    }
                });
        ipsExtendableVersionManager.setRegisteredMigrations(registeredMigrations);
    }
}
