/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.junit.Test;

public class ProjectUtilTest extends AbstractIpsPluginTest {

    @Test
    public void testAddIpsNature_And_hasIpsNature() throws CoreException {
        IProject project = newPlatformProject("PlatformProject");
        assertFalse(ProjectUtil.hasIpsNature(project.getProject()));
        ProjectUtil.addIpsNature(project);
        assertTrue(ProjectUtil.hasIpsNature(project));
    }

    @Test
    public void testCreateIpsProjectSetsFormulaLanguageLocaleToEnglishIfDefaultIsNotGerman() throws CoreException {
        IProject platformProject = newPlatformProject("TestProjectItalian");
        IJavaProject javaProject = addJavaCapabilities(platformProject);
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, "runtimeIdPrefix", true, true, false,
                Arrays.asList(Locale.ITALIAN, Locale.GERMAN));
        assertEquals(Locale.ENGLISH, ipsProject.getFormulaLanguageLocale());
    }

    @Test
    public void testCreateIpsProjectSetsFormulaLanguageLocaleToGermanIfDefaultIsGerman() throws CoreException {
        IProject platformProject = newPlatformProject("TestProjectGerman");
        IJavaProject javaProject = addJavaCapabilities(platformProject);
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, "runtimeIdPrefix", true, true, false,
                Arrays.asList(Locale.GERMAN, Locale.ITALIAN, Locale.ENGLISH));
        assertEquals(Locale.GERMAN, ipsProject.getFormulaLanguageLocale());
    }

}
