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

import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.abstraction.internal.OsgiImplementationProvider;

/**
 * Static access to the {@link AAbstraction abstractions} used to hide whether Faktor-IPS is running
 * in Eclipse or on plain old Java.
 */
public final class Abstractions {

    private static final AImplementation IMPLEMENTATION = get();

    private static final boolean ECLIPSE_RUNNING = IMPLEMENTATION.isEclipse();

    private Abstractions() {
        // util
    }

    private static AImplementation get() {
        OsgiImplementationProvider osgiProvider = OsgiImplementationProvider.getInstance();
        if (osgiProvider != null) {
            return osgiProvider.get();
        }
        Optional<AImplementationProvider> implementationProvider = ServiceLoader
                .load(AImplementationProvider.class).stream()
                .map(Provider::get)
                .sorted(Comparator.comparing(AImplementationProvider::getPriority).reversed())
                .findFirst();
        return implementationProvider
                .orElseThrow(() -> new IpsException("No implementation provider found!")) //$NON-NLS-1$
                .get();
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
    public static AVersion getVersion() {
        return IMPLEMENTATION.getVersion();
    }

    /**
     * Returns the log used by the implementation.
     */
    public static ALog getLog() {
        return IMPLEMENTATION.getLog();
    }

    /**
     * Returns the implementation's ID.
     */
    public static String getImplementationId() {
        return IMPLEMENTATION.getId();
    }

    static WrapperBuilder getWrapperBuilder(Object original) {
        return IMPLEMENTATION.getWrapperBuilder(original);
    }

    public interface AImplementation {

        String getId();

        boolean isEclipse();

        WrapperBuilder getWrapperBuilder(Object original);

        AWorkspace getWorkspace();

        Locale getLocale();

        AVersion getVersion();

        ALog getLog();
    }

}
