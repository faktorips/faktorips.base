/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.plugin.ExtensionPoints;

public class DatatypeHelperFactoryDefinition {

    static final String FACTORY_DEFINTIION = "datatypeHelperFactory"; //$NON-NLS-1$
    static final String DATATYPE_CLASS = "datatypeClass"; //$NON-NLS-1$
    static final String FACTORY_CLASS = "factoryClass"; //$NON-NLS-1$

    private static final String ILLEGAL_DEFINITION = "Illegal datatype helper factory definition %s. Expected Config Element <datatypeHelperFactory> but was %s"; //$NON-NLS-1$

    private final Datatype datatype;
    private final DatatypeHelperFactory factory;

    public DatatypeHelperFactoryDefinition(IExtension extension, IConfigurationElement configElement) {
        super();
        if (!StringUtils.equalsIgnoreCase(FACTORY_DEFINTIION, configElement.getName())) {
            String text = String.format(ILLEGAL_DEFINITION, extension.getUniqueIdentifier(), configElement.getName());
            throw new IllegalStateException(text);
        }
        this.datatype = ExtensionPoints.createExecutableExtension(extension, configElement, DATATYPE_CLASS,
                Datatype.class);
        this.factory = ExtensionPoints.createExecutableExtension(extension, configElement, FACTORY_CLASS,
                DatatypeHelperFactory.class);
    }

    public DatatypeHelperFactory getFactory() {
        return factory;
    }

    public Datatype getDatatype() {
        return datatype;
    }

}
