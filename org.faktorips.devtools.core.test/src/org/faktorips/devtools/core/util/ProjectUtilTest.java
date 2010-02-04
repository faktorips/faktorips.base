/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;

public class ProjectUtilTest extends AbstractIpsPluginTest {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testAddIpsNature_And_hasIpsNature() throws CoreException {
        IProject project = newPlatformProject("PlatformProject/abc");
        assertFalse(ProjectUtil.hasIpsNature(project.getProject()));
        ProjectUtil.addIpsNature(project);
        assertTrue(ProjectUtil.hasIpsNature(project));
    }

}
