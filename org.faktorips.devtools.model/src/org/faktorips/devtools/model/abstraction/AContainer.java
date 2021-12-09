/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import static org.faktorips.devtools.model.abstraction.Wrappers.wrap;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.faktorips.devtools.model.abstraction.AWorkspaceRoot.PlainJavaWorkspaceRoot;

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

    @Override
    public default Iterator<AResource> iterator() {
        @SuppressWarnings("unchecked")
        Iterator<AResource> iterator = (Iterator<AResource>)getMembers().iterator();
        return iterator;
    }

    public abstract static class AEclipseContainer extends AEclipseResource implements AContainer {

        AEclipseContainer(IContainer container) {
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
            return wrap(container()::members).asSortedSetOf(AResource.class);
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

    public abstract static class PlainJavaContainer extends PlainJavaResource implements AContainer {

        private volatile SortedSet<PlainJavaResource> members;

        public PlainJavaContainer(File directory) {
            super(PlainJavaUtil.directory(directory));
        }

        File directory() {
            return unwrap();
        }

        @Override
        public SortedSet<PlainJavaResource> getMembers() {
            if (members == null) {
                synchronized (this) {
                    if (members == null) {
                        members = findMembers();
                    }
                }
            }

            return new TreeSet<>(members);
        }

        private SortedSet<PlainJavaResource> findMembers() {
            File[] files = directory().listFiles();
            PlainJavaWorkspaceRoot root = getWorkspace().getRoot();
            return Arrays.stream(files)
                    .map(File::toPath)
                    .map(root::get)
                    .collect(Collectors.toCollection(TreeSet::new));
        }

        @Override
        public AResource findMember(String path) {
            return getWorkspace().getRoot().get(directory().toPath().resolve(path));
        }

        @Override
        public AFile getFile(Path path) {
            return getWorkspace().getRoot().file(directory().toPath().resolve(path));
        }

        @Override
        public AFolder getFolder(Path path) {
            return getWorkspace().getRoot().folder(directory().toPath().resolve(path));
        }

        @Override
        protected void refreshInternal() {
            members = null;
            // TODO alte Members merken, neue direkt holen und vergleichen, Workspace ggf. über
            // gelöschte benachrichtigen
            super.refreshInternal();
        }

        @Override
        protected boolean isSynchronizedInternal() {
            return super.isSynchronizedInternal() && Objects.equals(members, findMembers());
        }

        @Override
        protected void recursive(Consumer<PlainJavaResource> consumer, AResourceTreeTraversalDepth depth) {
            super.recursive(consumer, depth);
            if (depth != AResourceTreeTraversalDepth.RESOURCE_ONLY) {
                for (PlainJavaResource resource : getMembers()) {
                    resource.recursive(consumer, depth.decrement());
                }
            }
        }

    }

}
