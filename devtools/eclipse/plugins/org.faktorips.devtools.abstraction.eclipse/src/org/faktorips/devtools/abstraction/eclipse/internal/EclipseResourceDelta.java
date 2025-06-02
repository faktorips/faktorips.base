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
import static org.faktorips.devtools.abstraction.eclipse.mapping.PathMapping.toEclipsePath;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
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
    public AResourceDelta findMember(Path path) {
        return wrap(resourceDelta().findMember(toEclipsePath(path))).as(AResourceDelta.class);
    }

    @Override
    public AResource getResource() {
        return wrap(resourceDelta().getResource()).as(AResource.class);
    }

    @Override
    public AResourceDeltaKind getKind() {
        int kind = resourceDelta().getKind();
        return switch (kind) {
            case IResourceDelta.ADDED -> AResourceDeltaKind.ADDED;
            case IResourceDelta.REMOVED -> AResourceDeltaKind.REMOVED;
            default -> AResourceDeltaKind.CHANGED;
        };
    }

    @Override
    public Set<AResourceDeltaFlag> getFlags() {
        Set<AResourceDeltaFlag> flagedChanges = new HashSet<>();
        int flags = resourceDelta().getFlags();
        if ((flags & IResourceDelta.CONTENT) != 0) {
            flagedChanges.add(AResourceDeltaFlag.CONTENT);
        }
        if ((flags & IResourceDelta.DERIVED_CHANGED) != 0) {
            flagedChanges.add(AResourceDeltaFlag.DERIVED_CHANGED);
        }
        if ((flags & IResourceDelta.DESCRIPTION) != 0) {
            flagedChanges.add(AResourceDeltaFlag.DESCRIPTION);
        }
        if ((flags & IResourceDelta.ENCODING) != 0) {
            flagedChanges.add(AResourceDeltaFlag.ENCODING);
        }
        if ((flags & IResourceDelta.LOCAL_CHANGED) != 0) {
            flagedChanges.add(AResourceDeltaFlag.LOCAL_CHANGED);
        }
        if ((flags & IResourceDelta.OPEN) != 0) {
            flagedChanges.add(AResourceDeltaFlag.OPEN);
        }
        if ((flags & IResourceDelta.MOVED_TO) != 0) {
            flagedChanges.add(AResourceDeltaFlag.MOVED_TO);
        }
        if ((flags & IResourceDelta.MOVED_FROM) != 0) {
            flagedChanges.add(AResourceDeltaFlag.MOVED_FROM);
        }
        if ((flags & IResourceDelta.COPIED_FROM) != 0) {
            flagedChanges.add(AResourceDeltaFlag.COPIED_FROM);
        }
        if ((flags & IResourceDelta.TYPE) != 0) {
            flagedChanges.add(AResourceDeltaFlag.TYPE);
        }
        if ((flags & IResourceDelta.SYNC) != 0) {
            flagedChanges.add(AResourceDeltaFlag.SYNC);
        }
        if ((flags & IResourceDelta.MARKERS) != 0) {
            flagedChanges.add(AResourceDeltaFlag.MARKERS);
        }
        if ((flags & IResourceDelta.REPLACED) != 0) {
            flagedChanges.add(AResourceDeltaFlag.REPLACED);
        }
        return flagedChanges;
    }

    @Override
    public void accept(AResourceDeltaVisitor visitor) {
        try {
            resourceDelta().accept(delta -> visitor.visit(new EclipseResourceDelta(delta)));
        } catch (CoreException e) {
            throw new IpsException(e);
        }
    }

}
