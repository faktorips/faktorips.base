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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;
import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;
import org.faktorips.devtools.abstraction.exception.IpsException;

public enum PlainJavaImplementation implements AImplementation {
    INSTANCE;

    private static final String ID = "Plain Java Faktor-IPS"; //$NON-NLS-1$
    private final Locale locale = Locale.getDefault();
    private volatile PlainJavaWorkspace workspace;
    private final ALog log = new PlainJavaLog();

    static PlainJavaImplementation get() {
        return INSTANCE;
    }

    private static File createTmpWorkspaceDirectory() {
        try {
            return Files.createTempDirectory("Workspace").toFile(); //$NON-NLS-1$
        } catch (IOException e) {
            throw new IpsException("Could not create temporary workspace directory", e); //$NON-NLS-1$
        }
    }

    public void setWorkspace(PlainJavaWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public boolean isEclipse() {
        return false;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public WrapperBuilder getWrapperBuilder(Object original) {
        return new PlainJavaWrapperBuilder(original);
    }

    @Override
    public PlainJavaWorkspace getWorkspace() {
        PlainJavaWorkspace result = workspace;
        if (result == null) {
            synchronized (this) {
                if (workspace == null) {
                    // CSOFF: InnerAssignment
                    // Efficient lazy initialization pattern from "Effective Java", 3rd edition,
                    // p.334
                    workspace = result = new PlainJavaWorkspace(createTmpWorkspaceDirectory());
                    // CSON: InnerAssignment
                }
            }
        }
        return result;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public AVersion getVersion() {
        // TODO read from Manifest?
        return AVersion.parse("21.12"); //$NON-NLS-1$
    }

    @Override
    public ALog getLog() {
        return log;
    }

    private static final class PlainJavaLog implements ALog {
        private final Map<ALogListener, Void> logListeners = new WeakHashMap<>();

        @Override
        public void removeLogListener(ALogListener listener) {
            logListeners.remove(listener);
        }

        @Override
        public void log(IStatus status) {
            // TODO formatting, logging frameworks/maven logging,...
            logListeners.keySet().forEach(l -> l.logging(status, ID));
            if (status.getSeverity() == IStatus.ERROR) {
                System.err.println(status);
            } else {
                System.out.println(status);
            }
        }

        @Override
        public void addLogListener(ALogListener listener) {
            logListeners.put(listener, null);
        }

        @Override
        public <T> T unwrap() {
            throw new UnsupportedOperationException();
        }
    }

}