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

package org.faktorips.devtools.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.AbstractIpsPluginTest;

/**
 * 
 * @author Jan Ortmann
 */
public class CreateIpsArchiveOperationTest extends AbstractIpsPluginTest {


    /*
     * Test method for 'org.faktorips.devtools.core.model.CreateIpsArchiveOperation.run(IProgressMonitor)'
     */
    public void testRun() throws CoreException {
        IIpsProject project = newIpsProject();
        newPolicyCmptType(project, "mycompany.motor.MotorPolicy");
        newPolicyCmptType(project, "mycompany.motor.MotorCoverage");
        newPolicyCmptType(project, "mycompany.home.HomeCoverage");
        
        IFile archive = project.getProject().getFile("test.ipsar");
        CreateIpsArchiveOperation operation = new CreateIpsArchiveOperation(project.getIpsPackageFragmentRoots(), archive);
        operation.run(null);
        
        assertTrue(archive.exists());
    }

}
