/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.commands.messages"; //$NON-NLS-1$
    public static String IpsDeleteHandler_deleteResources;
    public static String SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentElements;
    public static String SwitchTemplatePropertyValueHandler_warning_illegalSelection_differentValue;
    public static String SwitchTemplatePropertyValueHandler_warning_title;
    public static String SetTemplateValueStatus_warning_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
