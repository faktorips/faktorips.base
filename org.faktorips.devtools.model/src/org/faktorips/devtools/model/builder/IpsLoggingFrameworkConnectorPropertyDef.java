/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.internal.ipsproject.properties.IpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSet;
import org.faktorips.devtools.model.ipsproject.IIpsBuilderSetPropertyDef;
import org.faktorips.devtools.model.ipsproject.IIpsLoggingFrameworkConnector;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;

/**
 * This implementation of the {@link IIpsBuilderSetPropertyDef} interface can be used by
 * {@link IIpsArtefactBuilderSet} implementations that use implementations of the
 * {@code DefaultJavaSourceFileBuilder}. Since this builder has the ability to generate logging
 * code. To configure the {@link IIpsLoggingFrameworkConnector} within the properties of the builder
 * set configuration this class has to be specified in the property definition for the logging
 * connector of the builder set.
 * 
 * @author Peter Erzberger
 */
public class IpsLoggingFrameworkConnectorPropertyDef extends IpsBuilderSetPropertyDef {

    @Override
    public boolean isAvailable(IIpsProject ipsProject) {
        return true;
    }

    @Override
    public String getDefaultValue(IIpsProject ipsProject) {
        return "None"; //$NON-NLS-1$
    }

    @Override
    public String getDisableValue(IIpsProject ipsProject) {
        return "None"; //$NON-NLS-1$
    }

    @Override
    public String[] getDiscreteValues() {
        String[] values = super.getDiscreteValues();
        List<String> newValues = new ArrayList<String>();
        newValues.add(getDisableValue(null));
        newValues.addAll(Arrays.asList(values));
        return newValues.toArray(new String[newValues.size()]);
    }

    @Override
    public Object parseValue(String value) {
        if (value == null || "None".equals(value)) { //$NON-NLS-1$
            return null;
        }
        IIpsLoggingFrameworkConnector connector = IIpsModelExtensions.get().getIpsLoggingFrameworkConnector(value);
        if (connector == null) {
            throw new IllegalArgumentException("Unparsable value: " + value); //$NON-NLS-1$
        }
        return connector;
    }

    @Override
    public Message validateValue(IIpsProject ipsProject, String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        try {
            parseValue(value);
            return null;
        } catch (IllegalArgumentException e) {
            return getStandardValidationMessage(value);
        }
    }

}
