/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.JavaProjectUtil;
import org.faktorips.abstracttest.PlatformProjectBuilder;
import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.testsetup.AbstractAbstractionTestSetup;

public class EclipseAbstractionTestSetup extends AbstractAbstractionTestSetup {

    @Override
    public Path srcFolder() {
        return Path.of("src");
    }

    @Override
    public List<Path> additionalFolders() {
        return List.of(Path.of("bin"));
    }

    @Override
    public List<Path> files() {
        return List.of();
    }

    @Override
    public AProject newProjectImpl(String name) {
        return createEclipseProject(name);
    }

    @Override
    public void addDependencies(AProject project, AProject... dependencies) {
        try {
            IProjectDescription desc = ((IProject)project.unwrap()).getDescription();
            desc.setReferencedProjects(Arrays.stream(dependencies)
                    .map(p -> (IProject)p.unwrap())
                    .toArray(IProject[]::new));
            ((IProject)project.unwrap()).setDescription(desc, null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    @Override
    protected void toIpsProjectImpl(AProject project) {
        try {
            IProject eclipseProject = (IProject)project.unwrap();
            eclipseProject.setDescription(createIpsNature(eclipseProject), null);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    @Override
    public AJavaProject toJavaProject(AProject project) {
        try {
            return JavaProjectUtil.addJavaCapabilities(project);
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

    private AProject createEclipseProject(String name) {
        return new PlatformProjectBuilder().name(name).build();
    }

    private IProjectDescription createIpsNature(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 1, natures.length);
        newNatures[0] = "org.faktorips.devtools.model.ipsnature";
        description.setNatureIds(newNatures);
        return description;
    }

}
