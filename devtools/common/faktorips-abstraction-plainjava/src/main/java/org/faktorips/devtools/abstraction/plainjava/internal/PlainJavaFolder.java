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

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;

public class PlainJavaFolder extends PlainJavaContainer implements AFolder {

    public PlainJavaFolder(File directory) {
        super(directory);
    }

    @Override
    public AResourceType getType() {
        return AResourceType.FOLDER;
    }

    @Override
    public AFile getFile(String name) {
        return getWorkspace().getRoot().file(directory().toPath().resolve(name));
    }

    @Override
    public AFolder getFolder(String name) {
        return getWorkspace().getRoot().folder(directory().toPath().resolve(name));
    }

    @Override
    public void create(IProgressMonitor monitor) {
        if (monitor != null) {
            monitor.beginTask("Creating folder " + directory(), 1); //$NON-NLS-1$
        }
        create();
        if (monitor != null) {
            monitor.done();
        }
    }

    @Override
    void create() {
        directory().mkdirs();
        refreshParent();
        PlainJavaImplementation.getResourceChanges().resourceCreated(this);
    }

}
