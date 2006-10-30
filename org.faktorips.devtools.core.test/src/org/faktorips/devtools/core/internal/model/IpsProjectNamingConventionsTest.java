/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende:  Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  
 **************************************************************************************************/

package org.faktorips.devtools.core.internal.model;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Daniel Hohenberger
 */
public class IpsProjectNamingConventionsTest extends AbstractIpsPluginTest {

    private IpsProject ipsProject;
    private IIpsProjectNamingConventions namingConventions;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = (IpsProject)this.newIpsProject("TestProject");
        namingConventions = ipsProject.getNamingConventions();
    }

    /**
     * Test method for {@link org.faktorips.devtools.core.internal.model.DefaultIpsProjectNamingConventions#validateIpsPackageName(java.lang.String)}.
     * @throws CoreException 
     */
    public void testValidateIpsPackageName() throws CoreException {
        MessageList ml = namingConventions.validateIpsPackageName("validName");
        assertFalse(ml.containsErrorMsg());
        assertEquals(0, ml.getNoOfMessages());
        
        ml = namingConventions.validateIpsPackageName("1_invalid");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("no blanks allowed");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("new");
        assertTrue(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.ERROR, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.INVALID_NAME, ml.getMessage(0).getCode());
        
        ml = namingConventions.validateIpsPackageName("BIG");
        assertFalse(ml.containsErrorMsg());
        assertEquals(1, ml.getNoOfMessages());
        assertEquals(Message.WARNING, ml.getSeverity());
        assertEquals(IIpsProjectNamingConventions.DISCOURAGED_NAME, ml.getMessage(0).getCode());
    }

}
