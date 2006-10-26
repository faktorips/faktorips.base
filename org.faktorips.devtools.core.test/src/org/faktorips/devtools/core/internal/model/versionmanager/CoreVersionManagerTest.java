/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import java.io.IOException;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.IpsProjectProperties;
import org.faktorips.devtools.core.model.IIpsProject;
import org.osgi.framework.Bundle;


/**
 *
 */
public class CoreVersionManagerTest extends AbstractIpsPluginTest {
    private BundleGroupProvider bundleProvider;
    private CoreVersionManager cvm;
    
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = this.newIpsProject("TestProject");
        IpsProjectProperties props = (IpsProjectProperties)ipsProject.getProperties();
        props.setMinRequiredVersionNumber("test.feature", "1.0.0");
        ipsProject.setProperties(props);
        bundleProvider = new BundleGroupProvider();
        bundleProvider.setVersion("1.0.0");
        Platform.registerBundleGroupProvider(bundleProvider);
        cvm = new CoreVersionManager();
        cvm.setFeatureId("test.feature");
    }
    
    public void testGetCurrentVersion() {
        assertEquals("1.0.0", cvm.getCurrentVersion());
        
        cvm = new CoreVersionManager();
        cvm.setFeatureId("unknown-and-never-to-be-found");
        assertNull(cvm.getCurrentVersion());
    }
    
    public void testCompareToCurrentVersion() {
        assertEquals(0, cvm.compareToCurrentVersion("1.0.0"));
        assertTrue(cvm.compareToCurrentVersion("0.9.1") < 0);
        assertTrue(cvm.compareToCurrentVersion("1.0.2") > 0);
    }
    
    public void testIsCurrentVersionCompatibleWith() throws IOException {
        assertTrue(cvm.isCurrentVersionCompatibleWith("1.0.0"));
        assertFalse(cvm.isCurrentVersionCompatibleWith("2.0.0"));
        assertFalse(cvm.isCurrentVersionCompatibleWith("0.9.0"));
        
        // because the CoreVersionManager does not provide a public method to 
        // set compatibility-informations, it is not possible to test 
        // this case automatically.
    }
    
    private class BundleGroupProvider implements IBundleGroupProvider {
        private BundleGroup group;
        
        public BundleGroupProvider() {
            group = new BundleGroup();
        }
        
        /**
         * {@inheritDoc}
         */
        public String getName() {
            return "test FaktorIps bundle group provider";
        }

        /**
         * {@inheritDoc}
         */
        public IBundleGroup[] getBundleGroups() {
            return new IBundleGroup[] { group };
        }
        
        public void setVersion(String version) {
            group.setVersion(version);
        }

        private class BundleGroup implements IBundleGroup {
            String version;
            
            public String getProperty(String key) {
                return null;
            }

            public Bundle[] getBundles() {
                return null;
            }

            public String getProviderName() {
                return "Faktor Zehn GmbH";
            }

            public String getDescription() {
                return null;
            }

            public String getVersion() {
                return version;
            }
            
            public void setVersion(String version) {
                this.version = version;
            }

            public String getName() {
                return "Test Feature";
            }

            public String getIdentifier() {
                return "test.feature";
            }

        }

    }
}
