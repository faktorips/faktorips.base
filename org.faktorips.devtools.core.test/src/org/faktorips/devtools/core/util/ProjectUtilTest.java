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

    @Test
    public void testGetValidProjectName_NotValidCharacter() {
        String projectName = ProjectUtil.getValidProjectName("&23&& 78");
        assertEquals("_23___78", projectName);
    }

    @Test
    public void testGetValidProjectName_NotValidCharacter_NummberAsFirsCharacter() {
        String projectName = ProjectUtil.getValidProjectName("123");
        assertEquals("_23", projectName);
    }

    @Test
    public void testGetValidProjectName() {
        String projectName = ProjectUtil.getValidProjectName("$123");
        assertEquals("$123", projectName);
    }

    @Test
    public void testGetValidProjectName_NotValidHyphen() {
        String projectName = ProjectUtil.getValidProjectName("$-123");
        assertEquals("$_123", projectName);
    }

}
