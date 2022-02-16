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

import static org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaFileUtil.withMonitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.IProgressMonitor;
import org.faktorips.devtools.abstraction.AFile;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.runtime.internal.IpsStringUtils;

public class PlainJavaFile extends PlainJavaResource implements AFile {

    public PlainJavaFile(File file) {
        super(file);
    }

    @Override
    public AResourceType getType() {
        return AResourceType.FILE;
    }

    @Override
    void create() {
        try {
            file().createNewFile();
        } catch (IOException e) {
            throw new IpsException("Creating " + file() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public void create(InputStream source, IProgressMonitor monitor) {
        withMonitor(file(), monitor, "Creating", p -> { //$NON-NLS-1$
            p.getParent().toFile().mkdirs();
            Files.copy(source, p, StandardCopyOption.REPLACE_EXISTING);
        });
    }

    @Override
    public InputStream getContents() {
        try {
            return new FileInputStream(file());
        } catch (FileNotFoundException e) {
            throw new IpsException("Can't read " + file(), e); //$NON-NLS-1$
        }
    }

    @Override
    public void setContents(InputStream source, boolean keepHistory, IProgressMonitor monitor) {
        // TODO history?
        withMonitor(file(), monitor, "Writing", p -> //$NON-NLS-1$
        Files.copy(source, p, StandardCopyOption.REPLACE_EXISTING));
    }

    @Override
    public boolean isReadOnly() {
        // TODO Files from JARs?
        return false;
    }

    @Override
    public String getExtension() {
        String name = getName();
        int lastPeriod = name.lastIndexOf('.');
        if (lastPeriod > 0) {
            return name.substring(lastPeriod + 1);
        }
        return IpsStringUtils.EMPTY;
    }

}