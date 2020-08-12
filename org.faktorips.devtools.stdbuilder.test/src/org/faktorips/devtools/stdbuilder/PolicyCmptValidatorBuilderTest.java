/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.junit.Test;

public class PolicyCmptValidatorBuilderTest extends AbstractStdBuilderTest {

    @Test
    public void testValidator_Generate_WithInterface() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java").exists());
    }

    @Test
    public void testValidator_Generate_WithoutInterface() throws CoreException {
        IIpsProject project = newIpsProject();
        IIpsProjectProperties properties = project.getProperties();
        properties.getBuilderSetConfig()
                .setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_PUBLISHED_INTERFACES, "false", "");
        project.setProperties(properties);
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/CmptValidator.java")
                .exists());
    }

    @Test
    public void testValidator_Generate_false() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(false);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertFalse(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());
    }

    @Test
    public void testValidator_Delete() throws CoreException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());

        sourceType.getIpsSrcFile().getCorrespondingFile().delete(true, false, null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

        assertFalse(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());
    }
}
