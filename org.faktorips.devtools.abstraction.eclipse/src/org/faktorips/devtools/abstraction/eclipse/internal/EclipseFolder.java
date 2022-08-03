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

import static org.faktorips.devtools.abstraction.Wrappers.run;
import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.AFolder;

public class EclipseFolder extends EclipseContainer implements AFolder {

    EclipseFolder(IFolder folder) {
        super(folder);
    }

    @SuppressWarnings("unchecked")
    @Override
    public IFolder unwrap() {
        return (IFolder)super.unwrap();
    }

    IFolder folder() {
        return unwrap();
    }

    @Override
    public AFile getFile(String name) {
        return wrap(folder().getFile(name)).as(AFile.class);
    }

    @Override
    public AFolder getFolder(String name) {
        return wrap(folder().getFolder(name)).as(AFolder.class);
    }

    @Override
    public void create(IProgressMonitor monitor) {
        run(() -> folder().create(true, true, monitor));
    }

}
