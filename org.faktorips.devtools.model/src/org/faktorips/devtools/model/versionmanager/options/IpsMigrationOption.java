/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.versionmanager.options;

import java.util.Objects;

import org.faktorips.devtools.model.versionmanager.AbstractIpsProjectMigrationOperation;

/**
 * Configuration for an {@link AbstractIpsProjectMigrationOperation}. Should be used if it is not
 * clear what a migration should do. This class is intentional abstract because of the type erasure
 * the generic information at runtime is gone and the binder needs concrete types.
 *
 * @since 22.6
 */
public abstract class IpsMigrationOption<T> {

    public static final String PROPERTY_SELECTED = "selectedValue"; //$NON-NLS-1$

    private final String text;
    private final String id;

    private T selectedValue;

    public IpsMigrationOption(String id, String text, T defaultValue) {
        this.id = id;
        this.text = text;
        selectedValue = defaultValue;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setSelectedValue(T selected) {
        selectedValue = selected;
    }

    public T getSelectedValue() {
        return selectedValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IpsMigrationOption)) {
            return false;
        }
        IpsMigrationOption<?> other = (IpsMigrationOption<?>)obj;
        return Objects.equals(id, other.id) && Objects.equals(text, other.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }
}
