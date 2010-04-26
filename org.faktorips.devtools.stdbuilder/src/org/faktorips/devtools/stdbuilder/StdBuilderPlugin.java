/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;

/**
 * The plugin class for this faktor ips standard builder plugin.
 * 
 * @author Peter Erzberger
 */
public class StdBuilderPlugin extends Plugin {

    /**
     * The plugin id like it is defined in the plugin.xml file
     */
    public final static String PLUGIN_ID = "org.faktorips.stdbuilder"; //$NON-NLS-1$

    /**
     * The id of the standard builder set extension like it is defined in the plugin.xml file
     */
    public final static String STANDARD_BUILDER_EXTENSION_ID = "org.faktorips.devtools.stdbuilder.ipsstdbuilderset"; //$NON-NLS-1$

    // The shared instance.
    private static StdBuilderPlugin plugin;

    /**
     * Returns the shared instance.
     */
    public static StdBuilderPlugin getDefault() {
        return plugin;
    }

    /**
     * The constructor.
     */
    public StdBuilderPlugin() {
        super();
        plugin = this;
    }

    /**
     * Logs the core exception
     */
    public final static void log(CoreException e) {
        log(e.getStatus());
    }

    /**
     * Logs the status.
     */
    public final static void log(IStatus status) {
        plugin.getLog().log(status);
    }

}
