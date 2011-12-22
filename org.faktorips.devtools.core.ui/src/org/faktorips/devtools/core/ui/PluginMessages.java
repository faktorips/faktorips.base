/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.osgi.util.NLS;

/**
 * This NLS class loads the plugin.properties file that is used to internationalize labels
 * contributed by the plugin.xml. However sometimes you want to use the same message also in your
 * code. If you do so, add the name of the message as static {@link String} variable to this class.
 * <p>
 * Note: check whether the translated messages are found properly. Some plugins have problems
 * because they use an internal nl1.jar instead of directly bundling all property files.
 * 
 * @author dirmeier
 */
public class PluginMessages extends NLS {

    private static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, PluginMessages.class);
    }

    private PluginMessages() {
        // Messages bundles shall not be initialized.
    }

    public static String newProductCmpt_label;

}
