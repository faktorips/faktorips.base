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

package org.faktorips.devtools.core.ui.controls.chooser;

import javax.swing.text.html.ListView;

import org.faktorips.devtools.core.IpsPlugin;

/**
 * This class represents a value in the list chooser. It adds the ability to be null. This is
 * important because we could not add null to a {@link ListView}
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
