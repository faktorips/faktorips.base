/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.faktorips.devtools.abstraction.AJavaProject;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.Abstractions;
import org.faktorips.devtools.abstraction.Wrappers;
import org.faktorips.devtools.abstraction.testsetup.AbstractAbstractionTestSetup;

public class PlainJavaAbstractionTestSetup extends AbstractAbstractionTestSetup {

    @Override
    public Path srcFolder() {
        return Path.of("src", "main", "java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public List<Path> additionalFolders() {
        return List.of(
                Path.of("src", "main", "resources"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Path.of("src", "test", "java"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                Path.of("src", "test", "resources")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public List<Path> files() {
        return List.of(Path.of("pom.xml")); //$NON-NLS-1$
    }

    @Override
    public void addDependencies(AProject project, AProject... dependencies) {
        // TODO: FIPS-8693: Dependencies in POM?
    }

    @Override
    protected void toIpsProjectImpl(AProject project) {
        // TODO: FIPS-8693: IPS-Dependencies in POM?
    }

    @Override
    public AProject newProjectImpl(String name) {
        return Wrappers.wrap(createPlainJavaProject(name)).as(AProject.class);
    }

    @Override
    public AJavaProject toJavaProject(AProject project) {
        return Wrappers.wrap(project.unwrap()).as(AJavaProject.class);
    }

    private File createPlainJavaProject(String name) {
        File path = Abstractions.getWorkspace().getRoot().unwrap();
        File file = new File(path, name);
        file.mkdir();
        return file;
    }
}
