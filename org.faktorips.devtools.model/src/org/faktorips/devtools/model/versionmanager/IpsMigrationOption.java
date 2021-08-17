/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.versionmanager;

import java.util.Objects;

/**
 * Configuration for an {@link AbstractIpsProjectMigrationOperation}. Should be used if it is not
 * clear what a migration should do.
 *
 * @since 21.12
 */
// Erster Wurf. Vermutlich genügt eine binäre Entscheidung. Ggf. später weitere Optionsarten, z.B.
// mit Enum als Wert.
public class IpsMigrationOption {

    public static final String PROPERTY_ACTIVE = "active"; //$NON-NLS-1$

    private final String id;
    private final String text;
    private boolean active;

    /**
     * Creates a new {@link IpsMigrationOption}
     * 
     * @param id the ID is used to identify IpsMigrationOptions of the same type to provide a common
     *            setting.
     * @param text description of what this setting does
     * @param activeByDefault whether this setting should be active by default
     */
    public IpsMigrationOption(String id, String text, boolean activeByDefault) {
        this.id = id;
        this.text = text;
        this.active = activeByDefault;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IpsMigrationOption)) {
            return false;
        }
        IpsMigrationOption other = (IpsMigrationOption)obj;
        return Objects.equals(id, other.id) && Objects.equals(text, other.text);
    }

    @Override
    public String toString() {
        return "IpsMigrationOption [" + id + ", " + text + "=" + active + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
