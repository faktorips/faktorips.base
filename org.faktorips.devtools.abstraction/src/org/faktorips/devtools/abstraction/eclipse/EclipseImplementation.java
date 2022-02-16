/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.faktorips.devtools.abstraction.Wrappers.wrap;

import java.util.Locale;
import java.util.Objects;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.ALog;
import org.faktorips.devtools.abstraction.AVersion;
import org.faktorips.devtools.abstraction.AWorkspace;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;
import org.faktorips.devtools.abstraction.Wrappers.WrapperBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class EclipseImplementation implements AImplementation, BundleActivator {

    public static final String PLUGIN_ID = "org.faktorips.devtools.abstraction.eclipse"; //$NON-NLS-1$

    /** The shared instance. */
    @CheckForNull
    private static EclipseImplementation instance;

    private Bundle bundle;

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public EclipseImplementation() {
        instance = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        bundle = context.getBundle();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        bundle = null;
    }

    public static EclipseImplementation get() {
        return Objects.requireNonNull(instance, "Plugin " + PLUGIN_ID + " was not activated yet."); //$NON-NLS-1$//$NON-NLS-2$
    }

    public static boolean isStarted() {
        return instance != null;
    }

    @Override
    public boolean isEclipse() {
        return true;
    }

    @Override
    public String getId() {
        return PLUGIN_ID;
    }

    @Override
    public WrapperBuilder getWrapperBuilder(Object original) {
        return new EclipseWrapperBuilder(original);
    }

    @Override
    public AWorkspace getWorkspace() {
        return wrap(ResourcesPlugin.getWorkspace()).as(AWorkspace.class);
    }

    @Override
    public Locale getLocale() {
        String nl = Platform.getNL();
        // As of now, only the language is of concern to us, not the country.
        if (nl.length() > 2) {
            nl = nl.substring(0, 2);
        }
        return new Locale(nl);
    }

    @Override
    public AVersion getVersion() {
        String bundleVersion = Platform.getBundle("org.faktorips.devtools.model").getHeaders().get("Bundle-Version"); //$NON-NLS-1$ //$NON-NLS-2$
        return AVersion.parse(bundleVersion);
    }

    @Override
    public ALog getLog() {
        return wrap(Platform.getLog(get().bundle)).as(ALog.class);
    }

}