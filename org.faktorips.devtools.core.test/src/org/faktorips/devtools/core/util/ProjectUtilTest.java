/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
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
    public void testCreateIpsProjectSetsFunctionsLanguageLocaleToEnglishIfDefaultIsNotGerman() throws CoreException {
        IProject platformProject = newPlatformProject("TestProjectItalian");
        IJavaProject javaProject = addJavaCapabilities(platformProject);
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, "runtimeIdPrefix", true, true, false,
                Arrays.asList(Locale.ITALIAN, Locale.GERMAN));
        assertEquals(Locale.ENGLISH, ipsProject.getFunctionsLanguageLocale());
    }

    @Test
    public void testCreateIpsProjectSetsFunctionsLanguageLocaleToGermanIfDefaultIsGerman() throws CoreException {
        IProject platformProject = newPlatformProject("TestProjectGerman");
        IJavaProject javaProject = addJavaCapabilities(platformProject);
        IIpsProject ipsProject = ProjectUtil.createIpsProject(javaProject, "runtimeIdPrefix", true, true, false,
                Arrays.asList(Locale.GERMAN, Locale.ITALIAN, Locale.ENGLISH));
        assertEquals(Locale.GERMAN, ipsProject.getFunctionsLanguageLocale());
    }

}
