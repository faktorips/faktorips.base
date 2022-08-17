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
import static org.faktorips.devtools.abstraction.Wrappers.wrapSupplier;

import java.nio.file.Path;
import java.util.SortedSet;

import org.eclipse.core.resources.IContainer;
import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;

public abstract class EclipseContainer extends EclipseResource implements AContainer {

    EclipseContainer(IContainer container) {
        super(container);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IContainer unwrap() {
        return (IContainer)super.unwrap();
    }

    IContainer container() {
        return unwrap();
    }

    @Override
    public SortedSet<AResource> getMembers() {
        return wrapSupplier(container()::members).asSortedSetOf(AResource.class);
    }

    @Override
    public AFile getFile(Path path) {
        return wrap(container().getFile(org.eclipse.core.runtime.Path.fromOSString(path.toString())))
                .as(AFile.class);
    }

    @Override
    public AFolder getFolder(Path path) {
        return wrap(container().getFolder(org.eclipse.core.runtime.Path.fromOSString(path.toString())))
                .as(AFolder.class);
    }

    @Override
    public AResource findMember(String path) {
        return wrap(container().findMember(path)).as(AResource.class);
    }

}
