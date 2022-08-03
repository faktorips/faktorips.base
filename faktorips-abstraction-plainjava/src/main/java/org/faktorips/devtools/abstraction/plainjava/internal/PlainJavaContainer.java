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
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.faktorips.devtools.abstraction.AContainer;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;
import org.faktorips.devtools.abstraction.AResource;

public abstract class PlainJavaContainer extends PlainJavaResource implements AContainer {

    private volatile SortedSet<PlainJavaResource> members;

    public PlainJavaContainer(File directory) {
        super(PlainJavaFileUtil.directory(directory));
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
        if (files == null) {
            return Collections.emptySortedSet();
        }
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
