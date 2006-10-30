/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.versionmanager;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.ITestAnswerProvider;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.versionmanager.AbstractMigrationOperation;

/**
 * 
 * @author Joerg Ortmann
 */
public class CoreVersionManagerTest  extends AbstractIpsPluginTest {

    private IIpsProject project;
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        project = newIpsProject("TestProject");
        setMinVersion("0.0.0");
        
        IpsPlugin.getDefault().setTestMode(true);
        IpsPlugin.getDefault().setTestAnswerProvider(new ITestAnswerProvider() {
        
            public Object getAnswer() {
                return getClass().getClassLoader();
            }
        
            public String getStringAnswer() {
                return null;
            }
        
            public int getIntAnswer() {
                return 0;
            }
        
            public boolean getBooleanAnswer() {
                return false;
            }
        });
    }

    public void testGetMigrationOperations() throws Exception {
        CoreVersionManager cvm = new CoreVersionManager();
        AbstractMigrationOperation[] operations = cvm.getMigrationOperations(project);
        
        // This test will fail if a migration class for the current version of the
        // plugin allready exists - which will be the case during development often.
        // As this is no essential test we don't test this.
        // assertEquals(1, operations.length);
        
        assertTrue(operations[0] instanceof Migration_0_0_0);
        assertTrue(operations[1] instanceof Migration_0_0_1);
        
        setMinVersion("0.0.2");
        try {
            cvm.getMigrationOperations(project);
            fail();
        }
        catch (CoreException e) {
            // success
        }
        
    }
    
    public void testIsCurrentVersionCompatibleWith() {
        CoreVersionManager cvm = new CoreVersionManager();
        assertFalse(cvm.isCurrentVersionCompatibleWith("0.0.0"));
        assertTrue(cvm.isCurrentVersionCompatibleWith("0.0.1"));        
    }
    
    private void setMinVersion(String version) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        project.setProperties(props);
    }
    
}
