package org.faktorips.devtools.stdbuilder.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class StdBuilderUIPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.faktorips.devtools.stdbuilder.ui"; //$NON-NLS-1$

    // The shared instance
    private static StdBuilderUIPlugin plugin;

    /**
     * The constructor
     */
    public StdBuilderUIPlugin() {
        super();
    }

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
     * 
     * @return the shared instance
     */
    public static StdBuilderUIPlugin getDefault() {
        return plugin;
    }

}
