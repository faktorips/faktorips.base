/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.eclipse.builder;

import static org.faktorips.devtools.abstraction.Wrappers.unwrap;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;
import static org.faktorips.devtools.abstraction.eclipse.mapping.BuildKindMapping.buildKind;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.ABuilder;
import org.faktorips.devtools.abstraction.AProject;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.builder.IpsBuilder;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class EclipseIpsBuilder extends IncrementalProjectBuilder {

    private final IpsBuilder ipsBuilder = new IpsBuilder(new EclipseBuilder());

    public IpsBuilder getIpsBuilder() {
        return ipsBuilder;
    }

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
        try {
            return unwrap(ipsBuilder.build(buildKind(kind), monitor)).asArrayOf(IProject.class);
        } catch (IpsException e) {
            switch (e.getCause()) {
                case CoreException ce when ce.getCause() instanceof Error error -> throw error;
                case CoreException ce -> throw ce;
                case Error error -> throw error;
                default -> throw new CoreException(new IpsStatus(e));
            }
        }
    }

    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        ipsBuilder.clean(monitor);
    }

    public class EclipseBuilder implements ABuilder {

        @Override
        public AResourceDelta getDelta() {
            return wrap(EclipseIpsBuilder.this.getDelta(EclipseIpsBuilder.this.getProject()))
                    .as(AResourceDelta.class);
        }

        @Override
        public AProject getProject() {
            return wrap(EclipseIpsBuilder.this.getProject()).as(AProject.class);
        }

    }

}