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

import static org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResourceChange.Type.CONTENT_CHANGED;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.ALogListener;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;
import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaResourceChange.Type;

public enum PlainJavaImplementation implements AImplementation {
    INSTANCE;

    static final String ID = "Plain Java Faktor-IPS"; //$NON-NLS-1$
    private final Locale locale = Locale.getDefault();
    private volatile PlainJavaWorkspace workspace;
    private final ALog log = new PlainJavaLog();
    private final ResourceChanges resourceChanges = new ResourceChanges();

    public static PlainJavaImplementation get() {
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
        // TODO FIPS-8693: Aus dem Manifest des diese Klasse enthaltenden JARs lesen?
        return AVersion.parse("22.12"); //$NON-NLS-1$
    }

    @Override
    public ALog getLog() {
        return log;
    }

    @Override
    public String getDebugOption(String option) {
        // TODO später anpassen, z.B. Konfiguration über Maven?
        String property = System.getProperty(option);
        if (property == null) {
            property = "false";
        }
        return property;
    }

    public static ResourceChanges getResourceChanges() {
        return get().resourceChanges;
    }

    private static final class PlainJavaLog implements ALog {
        private final Map<ALogListener, Void> logListeners = new WeakHashMap<>();

        @Override
        public <T> T unwrap() {
            throw new UnsupportedOperationException(
                    PlainJavaLog.class.getSimpleName() + " does not wrap a single object"); //$NON-NLS-1$
        }

        @Override
        public void addLogListener(ALogListener listener) {
            logListeners.put(listener, null);
        }

        @Override
        public void removeLogListener(ALogListener listener) {
            logListeners.remove(listener);
        }

        @Override
        public void log(IStatus status) {
            logListeners.keySet().forEach(l -> l.logging(status, ID));
            if (status.getSeverity() == IStatus.ERROR) {
                Logger.getLogger(ID).log(Level.SEVERE, status.getMessage());
            } else if (status.getSeverity() == IStatus.WARNING) {
                Logger.getLogger(ID).log(Level.WARNING, status.getMessage());
            } else if (status.getSeverity() == IStatus.INFO) {
                Logger.getLogger(ID).log(Level.INFO, status.getMessage());
            } else {
                Logger.getLogger(ID).log(Level.FINE, status.getMessage());
            }
        }
    }

    public static final class ResourceChanges {
        private final Set<Consumer<PlainJavaResourceChange>> resourceChangeListeners = new LinkedHashSet<>();
        private final Deque<PlainJavaResourceChange> delayedChangeEvents = new LinkedList<>();
        private boolean delayChangeEvents;

        void hold() {
            delayChangeEvents = true;
        }

        void resume() {
            Deque<PlainJavaResourceChange> eventsToResend;
            synchronized (delayedChangeEvents) {
                eventsToResend = new LinkedList<>(delayedChangeEvents);
                delayedChangeEvents.clear();
                delayChangeEvents = false;
            }
            eventsToResend.forEach(this::notifyResourceChangeListeners);
        }

        private void notifyResourceChangeListeners(PlainJavaResourceChange change) {
            if (delayChangeEvents) {
                delayedChangeEvents.add(change);
            } else {
                resourceChangeListeners.forEach(listener -> listener.accept(change));
            }
            PlainJavaResource changedResource = change.getChangedResource();
            if (changedResource instanceof PlainJavaContainer container) {
                container.getMembers().stream()
                        .map(r -> new PlainJavaResourceChange(r, change.getType()))
                        .forEach(this::notifyResourceChangeListeners);
            }
        }

        public void resourceChanged(PlainJavaResource changedResource) {
            notifyResourceChangeListeners(new PlainJavaResourceChange(changedResource, CONTENT_CHANGED));
        }

        public void resourceMoved(PlainJavaResource oldResource, PlainJavaResource newResource) {
            notifyResourceChangeListeners(new PlainJavaResourceChange(oldResource, Type.REMOVED));
            notifyResourceChangeListeners(new PlainJavaResourceChange(newResource, Type.ADDED));
        }

        public void resourceCreated(PlainJavaResource newResource) {
            notifyResourceChangeListeners(new PlainJavaResourceChange(newResource, Type.ADDED));
        }

        public void resourceRemoved(PlainJavaResource removedResource) {
            notifyResourceChangeListeners(new PlainJavaResourceChange(removedResource, Type.REMOVED));
        }

        public void addListener(Consumer<PlainJavaResourceChange> listener) {
            resourceChangeListeners.add(listener);
        }

        public void removeListener(Consumer<PlainJavaResourceChange> listener) {
            resourceChangeListeners.remove(listener);
        }
    }

}
