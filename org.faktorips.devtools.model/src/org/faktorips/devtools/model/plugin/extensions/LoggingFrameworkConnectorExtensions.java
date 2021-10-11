/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.plugin.extensions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * {@link IIpsLoggingFrameworkConnector}-{@link List}-supplier for all implementations of the
 * extension point {@value #EXTENSION_POINT_ID_LOGGING_FRAMEWORK_CONNECTOR}.
 * 
 * @deprecated since 21.12.
 */
@Deprecated(since = "21.12")
public class LoggingFrameworkConnectorExtensions extends
        LazyCollectionExtension<IIpsLoggingFrameworkConnector, List<IIpsLoggingFrameworkConnector>> {

    /**
     * The extension point id of the extension point
     * {@value #EXTENSION_POINT_ID_LOGGING_FRAMEWORK_CONNECTOR}.
     */
    public static final String EXTENSION_POINT_ID_LOGGING_FRAMEWORK_CONNECTOR = "loggingFrameworkConnector"; //$NON-NLS-1$

    public LoggingFrameworkConnectorExtensions(ExtensionPoints extensionPoints) {
        super(extensionPoints,
                EXTENSION_POINT_ID_LOGGING_FRAMEWORK_CONNECTOR,
                ExtensionPoints.CONFIG_ELEMENT_PROPERTY_CLASS,
                IIpsLoggingFrameworkConnector.class,
                ArrayList::new,
                LoggingFrameworkConnectorExtensions::initializeIpsLoggingFrameworkConnector);
    }

    private static void initializeIpsLoggingFrameworkConnector(IConfigurationElement configElement,
            IIpsLoggingFrameworkConnector connector,
            List<IIpsLoggingFrameworkConnector> list) {
        String uniqueIdentifier = configElement.getDeclaringExtension().getUniqueIdentifier();
        connector.setId(uniqueIdentifier == null ? IpsStringUtils.EMPTY : uniqueIdentifier);
        list.add(connector);
    }

}
