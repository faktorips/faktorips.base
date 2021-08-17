/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.runtime.internal.ValueToXmlHelper;

public interface IConfiguredDefault extends IConfigElement {

    public static final String LEGACY_TAG_NAME = ValueToXmlHelper.XML_TAG_VALUE;

    public static final String TAG_NAME = ValueToXmlHelper.XML_TAG_CONFIGURED_DEFAULT;

    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "CONFIGUREDDEFAULT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value is not contained in the value set.
     */
    public static final String MSGCODE_VALUE_NOT_IN_VALUESET = MSGCODE_PREFIX + "ValueNotInValueSet"; //$NON-NLS-1$

    /**
     * Returns the attribute's value.
     */
    public String getValue();

    /**
     * Sets the attribute's value.
     */
    public void setValue(String newValue);

    /**
     * Overrides {@link IPropertyValue#findTemplateProperty(IIpsProject)} to return co-variant
     * {@code IConfiguredDefault}.
     * 
     * @see IPropertyValue#findTemplateProperty(IIpsProject)
     */
    @Override
    public IConfiguredDefault findTemplateProperty(IIpsProject ipsProject);

    /**
     * Returns the {@link IValueSet} that defines the allowed values for this configured default
     * value. As this is only a default value and no concrete value, the <code>null</code> value is
     * always allowed unless in case of a primitive datatype.
     * 
     */
    public IValueSet getValueSet();
}
