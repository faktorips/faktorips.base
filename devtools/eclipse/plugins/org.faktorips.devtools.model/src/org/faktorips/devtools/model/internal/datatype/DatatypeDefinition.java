/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.datatype;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.plugin.ExtensionPoints;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;

public class DatatypeDefinition {

    static final String DATATYPE_DEFINTIION = "datatypeDefinition"; //$NON-NLS-1$
    static final String DATATYPE_CLASS = "datatypeClass"; //$NON-NLS-1$
    static final String HELPER_CLASS = "helperClass"; //$NON-NLS-1$

    private static final String ILLEGAL_DEFINITION = "Illegal datatype definition %s. Expected Config Element <datatypeDefinition> but was %s"; //$NON-NLS-1$

    private final Datatype datatype;
    private final DatatypeHelper helper;

    public DatatypeDefinition(IExtension extension, IConfigurationElement configElement) {
        super();
        if (!StringUtils.equalsIgnoreCase(DATATYPE_DEFINTIION, configElement.getName())) {
            String text = String.format(ILLEGAL_DEFINITION, extension.getUniqueIdentifier(), configElement.getName());
            IpsLog.log(new IpsStatus(text));
            datatype = null;
            helper = null;
            return;
        }
        Object datatypeObj = ExtensionPoints.createExecutableExtension(extension, configElement, DATATYPE_CLASS,
                Datatype.class);
        if (datatypeObj instanceof Datatype) {
            datatype = (Datatype)datatypeObj;
            Object helperObj = ExtensionPoints.createExecutableExtension(extension, configElement, HELPER_CLASS,
                    DatatypeHelper.class);
            if (helperObj instanceof DatatypeHelper) {
                helper = (DatatypeHelper)helperObj;
                helper.setDatatype(datatype);
            } else {
                helper = null;
            }
        } else {
            datatype = null;
            helper = null;
        }
    }

    public boolean hasDatatype() {
        return datatype != null;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    public boolean hasHelper() {
        return helper != null;
    }

    public DatatypeHelper getHelper() {
        return helper;
    }

}
