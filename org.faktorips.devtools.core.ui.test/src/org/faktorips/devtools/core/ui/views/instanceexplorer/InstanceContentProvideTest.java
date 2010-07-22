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
package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * @author dirmeier
 * 
 */
public class InstanceContentProvideTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    private IIpsProject referencingProject;

    private IIpsProject leaveProject1;

    private IIpsProject leaveProject2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        referencingProject = newIpsProject("ReferencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. ipsProject have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);
    }

    public void testEmtptyTest() {
        assertTrue(true);
    }

}
