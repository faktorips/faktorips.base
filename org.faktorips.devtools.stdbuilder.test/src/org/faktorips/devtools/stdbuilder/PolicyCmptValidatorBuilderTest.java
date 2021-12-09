/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.xtend.policycmpt.PolicyCmptValidatorBuilder;
import org.junit.Before;
import org.junit.Test;

public class PolicyCmptValidatorBuilderTest extends AbstractStdBuilderTest {

    private PolicyCmptValidatorBuilder validatorBuilder;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        IIpsArtefactBuilder[] builders = ipsProject.getIpsArtefactBuilderSet().getArtefactBuilders();
        for (IIpsArtefactBuilder builder : builders) {
            if (builder instanceof PolicyCmptValidatorBuilder) {
                validatorBuilder = (PolicyCmptValidatorBuilder)builder;
            }
        }
        assertNotNull(validatorBuilder);
    }

    @Test
    public void testValidator_Generate_WithInterface() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java").exists());
    }

    @Test
    public void testValidator_Generate_WithoutInterface() throws CoreRuntimeException {
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
    public void testValidator_Generate_false() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(false);

        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertFalse(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());
    }

    @Test
    public void testValidator_Delete() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());

        sourceType.getIpsSrcFile().getCorrespondingFile().delete(null);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);

        assertFalse(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());
    }

    @Test
    public void testValidator_ExistingValidatorClassNotDeleted() throws CoreRuntimeException {
        IIpsProject project = newIpsProject();
        IPolicyCmptType sourceType = newPolicyCmptTypeWithoutProductCmptType(project, "Cmpt");
        sourceType.setGenerateValidatorClass(true);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
        assertTrue(project.getProject().getFile("src/org/faktorips/sample/model/internal/CmptValidator.java")
                .exists());

        sourceType.setGenerateValidatorClass(false);
        ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

        IFile validator = project.getProject()
                .getFile("src/org/faktorips/sample/model/internal/CmptValidator.java");
        assertThat(validator.exists(), is(true));
    }
}
