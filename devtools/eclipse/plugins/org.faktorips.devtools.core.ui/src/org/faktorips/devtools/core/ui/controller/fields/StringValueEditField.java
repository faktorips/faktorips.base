/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This {@link EditField} uses String as the underlying data container. That means it could be used
 * to show any data type in the input field but always converting the value to {@link String}. It is
 * mostly used for all inputs that should be written to any model object because we only store
 * string values in model objects. This field also could handle the null representation string. To
 * use this funktionality, use the methods {@link #prepareObjectForGet(String)} and
 * {@link #prepareObjectForSet(String)}.
 * 
 * @author dirmeier
 */
public abstract class StringValueEditField extends DefaultEditField<String> {

    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences) if the given
     * object is <code>null</code>, the unmodified object otherwise.
     * 
     */
    public String prepareObjectForSet(String object) {
        return prepareObjectForSet(object, supportsNullStringRepresentation());
    }

    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences) if the given
     * object is <code>null</code>, the unmodified object otherwise.
     * 
     */
    public static String prepareObjectForSet(String object, boolean supportsNull) {
        if (object == null) {
            if (supportsNull) {
                return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
            } else {
                return IpsStringUtils.EMPTY;
            }
        }
        return "" + object; //$NON-NLS-1$
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, the
     * unmodified value otherwise.
     */
    public String prepareObjectForGet(String value) {
        return prepareObjectForGet(value, supportsNullStringRepresentation());
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, the
     * unmodified value otherwise.
     */
    public static String prepareObjectForGet(String value, boolean supportsNull) {
        if (supportsNull && IpsPlugin.getDefault().getIpsPreferences().getNullPresentation().equals(value)) {
            return null;
        }
        return value;
    }

}
