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

import static org.faktorips.devtools.abstraction.Wrappers.get;
import static org.faktorips.devtools.abstraction.Wrappers.run;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.Wrappers.wrapSupplier;
import static org.faktorips.devtools.abstraction.mapping.BuildKindMapping.buildKind;

import java.nio.charset.Charset;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuildKind;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AProject;

public class EclipseProject extends EclipseContainer implements AProject {
    private static final String NATURE_ID = "org.faktorips.devtools.model.ipsnature"; //$NON-NLS-1$
    private static final String OLD_NATURE_ID = "org.faktorips.devtools.core.ipsnature"; //$NON-NLS-1$

    EclipseProject(IProject project) {
        super(project);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IProject unwrap() {
        return (IProject)super.unwrap();
    }

    public IProject project() {
        return unwrap();
    }

    @Override
    public AFile getFile(String name) {
        return wrap(project().getFile(name)).as(AFile.class);
    }

    @Override
    public AFolder getFolder(String name) {
        return wrap(project().getFolder(name)).as(AFolder.class);
    }

    @Override
    public Set<AProject> getReferencedProjects() {
        return wrapSupplier(project()::getReferencedProjects).asSetOf(AProject.class);
    }

    @Override
    public void delete(IProgressMonitor monitor) {
        run(() -> project().delete(true, true, monitor));
    }

    @Override
    public boolean isIpsProject() {
        return get(() -> project().isOpen()
                && (project().hasNature(NATURE_ID) || project().hasNature(OLD_NATURE_ID)));
    }

    @Override
    public void build(ABuildKind kind, IProgressMonitor monitor) {
        run(() -> project().build(buildKind(kind), monitor));
    }

    @Override
    public Charset getDefaultCharset() {
        return get(() -> Charset.forName(project().getDefaultCharset()));
    }
}
