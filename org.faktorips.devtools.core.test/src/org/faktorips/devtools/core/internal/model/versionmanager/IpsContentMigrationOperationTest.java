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
import org.faktorips.devtools.core.model.versionmanager.AbstractIpsContentMigrationOperation;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Joerg Ortmann
 */
public class IpsContentMigrationOperationTest  extends AbstractIpsPluginTest {

    private IIpsProject project;
    private AbstractIpsContentMigrationOperation operation;
    
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

        operation = IpsPlugin.getDefault().getMigrationOperation(project);
    }

    public void testGetDescription() throws Exception {
        String descr = operation.getDescription();
        assertTrue(descr.indexOf("cool") > 0);
        assertTrue(descr.indexOf("crazy") > 0);
    }
    
    public void testIsEmpty() throws Exception {
        assertFalse(operation.isEmpty());
    }
    
    public void testExecute() throws Exception {
        operation.run(null);
        MessageList ml = operation.getMessageList();
        assertNotNull(ml.getMessageByCode("first"));
        assertNotNull(ml.getMessageByCode("second"));
    }
    
    private void setMinVersion(String version) throws CoreException {
        IIpsProjectProperties props = project.getProperties();
        props.setMinRequiredVersionNumber("org.faktorips.feature", version);
        project.setProperties(props);
    }
    
}
