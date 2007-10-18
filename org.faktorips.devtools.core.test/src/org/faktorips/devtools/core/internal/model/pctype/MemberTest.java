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

package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.pctype.IMember;


/**
 *
 */
public class MemberTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private PolicyCmptType pcType;
    private IMember member;

    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject("TestProject");
        pcType = newPolicyCmptType(project, "Policy");
        ipsSrcFile = pcType.getIpsSrcFile();
        member = pcType.newPolicyCmptTypeAttribute();
        ipsSrcFile.save(true, null);
    }
    
    public void testSetName() {
        member.setName("premium");
        assertEquals("premium", member.getName());
        assertTrue(ipsSrcFile.isDirty());
    }

    public void testSetDescription() {
        member.setDescription("blabla");
        assertEquals("blabla", member.getDescription());
        assertTrue(ipsSrcFile.isDirty());
    }
    
}
