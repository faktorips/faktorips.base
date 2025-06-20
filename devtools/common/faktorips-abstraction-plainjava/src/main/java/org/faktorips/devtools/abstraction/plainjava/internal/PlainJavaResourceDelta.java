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

import java.nio.file.Path;
import java.util.Set;

import org.faktorips.devtools.abstraction.AResource;
import org.faktorips.devtools.abstraction.AResourceDelta;
import org.faktorips.devtools.abstraction.AResourceDeltaVisitor;
import org.faktorips.devtools.abstraction.AWrapper;

public class PlainJavaResourceDelta extends AWrapper<PlainJavaResourceChange> implements AResourceDelta {
    private final PlainJavaResourceChange change;

    public PlainJavaResourceDelta(PlainJavaResourceChange change) {
        super(change);
        this.change = change;
    }

    @Override
    public AResourceDeltaKind getKind() {
        return switch (change.getType()) {
            case ADDED -> AResourceDeltaKind.ADDED;
            case REMOVED -> AResourceDeltaKind.REMOVED;
            default -> AResourceDeltaKind.CHANGED;
        };
    }

    @Override
    public AResourceDelta findMember(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AResource getResource() {
        return change.getChangedResource();
    }

    @Override
    public Set<AResourceDeltaFlag> getFlags() {
        return Set.of(AResourceDeltaFlag.CONTENT);
    }

    @Override
    public void accept(AResourceDeltaVisitor visitor) {
        throw new UnsupportedOperationException();
    }
}
