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

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AResourceDeltaVisitor;
import org.faktorips.devtools.abstraction.AWrapper;
import org.faktorips.devtools.abstraction.exception.IpsException;

public class EclipseResourceDelta extends AWrapper<IResourceDelta> implements AResourceDelta {

    public EclipseResourceDelta(IResourceDelta resourceDelta) {
        super(resourceDelta);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IResourceDelta unwrap() {
        return super.unwrap();
    }

    IResourceDelta resourceDelta() {
        return unwrap();
    }

    @Override
    public AResourceDelta findMember(IPath path) {
        return wrap(resourceDelta().findMember(path)).as(AResourceDelta.class);
    }

    @Override
    public AResource getResource() {
        return wrap(resourceDelta().getResource()).as(AResource.class);
    }

    @Override
    public AResourceDeltaKind getKind() {
        int kind = resourceDelta().getKind();
        switch (kind) {
            case IResourceDelta.ADDED:
                return AResourceDeltaKind.ADDED;
            case IResourceDelta.REMOVED:
                return AResourceDeltaKind.REMOVED;
            default:
                return AResourceDeltaKind.CHANGED;
        }
    }

    @Override
    public int getFlags() {
        return resourceDelta().getFlags();
    }

    @Override
    public void accept(AResourceDeltaVisitor visitor) {
        try {
            resourceDelta().accept(new IResourceDeltaVisitor() {

                @Override
                public boolean visit(IResourceDelta delta) throws CoreException {
                    return visitor.visit(new EclipseResourceDelta(delta));
                }
            });
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

}