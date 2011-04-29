/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controller.fields;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;

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
        return prepareObjectForSet(object, supportsNull());
    }

    /**
     * Returns the null-representation-string defined by the user (see IpsPreferences) if the given
     * object is <code>null</code>, the unmodified object otherwise.
     * 
     */
    public static String prepareObjectForSet(String object, boolean supportsNull) {
        if (object == null && supportsNull) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        }
        return "" + object; //$NON-NLS-1$
    }

    /**
     * Returns <code>null</code> if the given value is the null-representation-string, the
     * unmodified value otherwise.
     */
    public String prepareObjectForGet(String value) {
        return prepareObjectForGet(value, supportsNull());
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
