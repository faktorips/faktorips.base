/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.ipsobject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

/**
 * The kind of modifiers.
 */
public enum Modifier implements IAdaptable {

    PUBLISHED("published", Modifier.AccPublic, java.lang.reflect.Modifier.PUBLIC) { //$NON-NLS-1$

    },

    PUBLIC("public", Modifier.AccPublic, java.lang.reflect.Modifier.PUBLIC) { //$NON-NLS-1$

    };

    // from org.eclipse.jdt.core.Flags
    private static final int AccPublic = 0x0001;

    /** Flags constant according to org.eclipse.jdt.core.Flags */
    private int jdtFlags;

    /** Modifier according to java.lang.reflect.Modifier */
    private int javaModifier;

    private String id;

    /**
     * Returns the appropriate JDT Flags constants corresponding to the modifier.
     * 
     * @see org.eclipse.jdt.core.Flags
     */
    public int getJdtFlags() {
        return jdtFlags;
    }

    /**
     * Returns the Java modifier.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier() {
        return javaModifier;
    }

    public boolean isPublished() {
        return this == PUBLISHED;
    }

    public boolean isPublic() {
        return this == PUBLIC;
    }

    Modifier(String id, int jdtFlags, int javaModifier) {
        this.id = id;
        this.jdtFlags = jdtFlags;
        this.javaModifier = javaModifier;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static final Modifier getModifier(String id) {
        for (Modifier m : values()) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
