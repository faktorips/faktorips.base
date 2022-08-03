/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

import java.util.Objects;

import org.eclipse.jface.viewers.ListViewer;
import org.faktorips.devtools.core.IpsPlugin;

/**
 * This class represents a value in the list chooser. It adds the ability to be null. This is
 * important because we could not add null to a {@link ListViewer}
 * 
 * @author dirmeier
 */
public class ListChooserValue {

    private final String value;

    public ListChooserValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isNullValue() {
        return value == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        ListChooserValue other = (ListChooserValue)obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        if (isNullValue()) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else {
            return value;
        }
    }

}
