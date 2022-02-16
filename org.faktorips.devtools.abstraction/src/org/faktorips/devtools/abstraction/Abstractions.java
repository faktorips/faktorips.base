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

import java.util.Locale;

import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;
import org.faktorips.devtools.abstraction.eclipse.EclipseImplementationProvider;
import org.faktorips.devtools.abstraction.plainjava.PlainJavaImplementationProvider;

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
        EclipseImplementationProvider eclipseImplementationProvider = new EclipseImplementationProvider();
        return eclipseImplementationProvider.canRun() ? eclipseImplementationProvider.get()
                : new PlainJavaImplementationProvider().get();
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
