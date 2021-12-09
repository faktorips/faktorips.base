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
import java.io.IOException;
import java.lang.Runtime.Version;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.model.abstraction.AWorkspace.PlainJavaWorkspace;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.plugin.IpsModelActivator;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.osgi.framework.Bundle;

/**
 * Static access to the {@link AAbstraction abstractions} used to hide whether Faktor-IPS is running
 * in Eclipse or on plain old Java.
 */
public final class Abstractions {

    private static final boolean ECLIPSE_RUNNING;
    static {
        boolean result = false;
        try {
            result = Platform.isRunning();
            // CSOFF: IllegalCatch
        } catch (Throwable exception) {
            // CSON: IllegalCatch
            // Assume that Eclipse isn't running.
        }
        ECLIPSE_RUNNING = result;
    }

    private static final AImplementation IMPLEMENTATION = isEclipseRunning()
            ? new EclipseImplementation()
            : new PlainJavaImplementation();

    private Abstractions() {
        // util
    }

    /**
     * Returns whether Eclipse is running
     */
    public static boolean isEclipseRunning() {
        return ECLIPSE_RUNNING;
    }

    /**
     * Returns the current workspace.
     */
    public static AWorkspace getWorkspace() {
        return IMPLEMENTATION.getWorkspace();
    }

    /**
     * Returns the locale the implementation is using, for example for texts or value formatting.
     */
    public static Locale getLocale() {
        return IMPLEMENTATION.getLocale();
    }

    /**
     * Returns the Faktor-IPS implementation version.
     */
    public static Version getVersion() {
        return IMPLEMENTATION.getVersion();
    }

    /**
     * Returns the log used by the implementation.
     */
    public static ILog getLog() {
        return IMPLEMENTATION.getLog();
    }

    protected abstract static class AImplementation {

        protected abstract AWorkspace getWorkspace();

        protected abstract Locale getLocale();

        protected abstract Version getVersion();

        protected abstract ILog getLog();

    }

    protected static class EclipseImplementation extends AImplementation {

        @Override
        protected AWorkspace getWorkspace() {
            return wrap(ResourcesPlugin.getWorkspace()).as(AWorkspace.class);
        }

        @Override
        protected Locale getLocale() {
            String nl = Platform.getNL();
            // As of now, only the language is of concern to us, not the country.
            if (nl.length() > 2) {
                nl = nl.substring(0, 2);
            }
            return new Locale(nl);
        }

        @Override
        protected Version getVersion() {
            String version = Platform.getBundle("org.faktorips.devtools.model").getHeaders().get("Bundle-Version"); //$NON-NLS-1$ //$NON-NLS-2$
            version = version.replace(".qualifier", StringUtils.EMPTY); //$NON-NLS-1$
            if (version.endsWith(".0")) { //$NON-NLS-1$
                version = version.substring(0, version.length() - 2);
            }
            return Version.parse(version);
        }

        @Override
        protected ILog getLog() {
            return IpsModelActivator.getLog();
        }

    }

    static class PlainJavaImplementation extends AImplementation {

        private final Locale locale;
        private volatile PlainJavaWorkspace workspace;
        private final ILog log = new PlainJavaLog();

        protected PlainJavaImplementation(Locale locale) {
            this.locale = locale;
        }

        protected PlainJavaImplementation() {
            this(Locale.getDefault());
        }

        static PlainJavaImplementation get() {
            return (PlainJavaImplementation)Abstractions.IMPLEMENTATION;
        }

        private static File createTmpWorkspaceDirectory() {
            try {
                return Files.createTempDirectory("Workspace").toFile(); //$NON-NLS-1$
            } catch (IOException e) {
                throw new CoreRuntimeException(new IpsStatus("Could not create temporary workspace directory", e)); //$NON-NLS-1$
            }
        }

        void setWorkspace(PlainJavaWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        protected AWorkspace getWorkspace() {
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
        protected Locale getLocale() {
            return locale;
        }

        @Override
        protected Version getVersion() {
            // TODO read from Manifest?
            return Version.parse("21.12"); //$NON-NLS-1$
        }

        @Override
        protected ILog getLog() {
            return log;
        }

        private static final class PlainJavaLog implements ILog {
            private final Map<ILogListener, Void> logListeners = new WeakHashMap<>();

            @Override
            public void removeLogListener(ILogListener listener) {
                logListeners.remove(listener);
            }

            @Override
            public void log(IStatus status) {
                // TODO formatting, logging frameworks/maven logging,...
                if (status.getSeverity() == IStatus.ERROR) {
                    System.err.println(status);
                } else {
                    System.out.println(status);
                }
            }

            @Override
            public Bundle getBundle() {
                // TODO eigenes Log-Interface ohne getBundle? Dann bräuchten wir auch eigene
                // LogListener und idealerweise einen Ersatz für IStatus...
                throw new UnsupportedOperationException();
            }

            @Override
            public void addLogListener(ILogListener listener) {
                logListeners.put(listener, null);
            }
        }

    }

}
