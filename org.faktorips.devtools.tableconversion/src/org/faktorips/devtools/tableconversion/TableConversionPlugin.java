package org.faktorips.devtools.tableconversion;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Roman Grutza
 */
public class TableConversionPlugin extends AbstractUIPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "org.faktorips.devtools.tableconversion"; //$NON-NLS-1$

    /** The shared instance */
    private static TableConversionPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     */
    public static TableConversionPlugin getDefault() {
        return plugin;
    }

}
