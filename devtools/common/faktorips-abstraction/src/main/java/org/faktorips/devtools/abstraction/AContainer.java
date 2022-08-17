/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.SortedSet;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A container is a resource containing other resources, its {@link #getMembers() members}.
 */
public interface AContainer extends AResource, Iterable<AResource> {

    /**
     * Returns this container's members, sorted by their name.
     *
     * @return this container's members
     */
    SortedSet<? extends AResource> getMembers();

    /**
     * Returns the member of this container (or one of its members) denoted by the given path
     * (interpreted as relative to this resource), if it exists.
     *
     * @param path a path, relative to this container
     * @return the member identified by the path or {@code null} if no such member exists
     */
    @CheckForNull
    AResource findMember(String path);

    /**
     * Returns the file that is a member of this container (or one of its members) denoted by the
     * given path (interpreted as relative to this resource). It may not {@link #exists() exist}.
     *
     * @param path a path, relative to this container
     * @return the file identified by the path
     */
    AFile getFile(Path path);

    /**
     * Returns the folder that is a member of this container (or one of its members) denoted by the
     * given path (interpreted as relative to this resource). It may not {@link #exists() exist}.
     *
     * @param path a path, relative to this container
     * @return the folder identified by the path
     */
    AFolder getFolder(Path path);

    @SuppressWarnings("unchecked")
    @Override
    default Iterator<AResource> iterator() {
        return (Iterator<AResource>)getMembers().iterator();
    }

}
