/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.chooser;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ListChooserValue other = (ListChooserValue)obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
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
