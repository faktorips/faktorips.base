/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion;

import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.WorkbookProvider;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
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
        addMissingPoiProviders();
    }

    /**
     * POI uses the java {@link java.util.ServiceLoader} to provide its two {@link WorkbookProvider
     * WorkbookProviders}. Unfortunately this does not work in an OSGi environment and we have to
     * provide the {@link WorkbookProvider WorkbookProviders} manually.
     */
    private void addMissingPoiProviders() {
        // in the unlikely event the java.util.ServiceLoader worked in an OSGi environment, remove
        // the providers before we add them twice
        WorkbookFactory.removeProvider(XSSFWorkbookFactory.class);
        WorkbookFactory.removeProvider(HSSFWorkbookFactory.class);
        WorkbookFactory.addProvider(new XSSFWorkbookFactory());
        WorkbookFactory.addProvider(new HSSFWorkbookFactory());
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
