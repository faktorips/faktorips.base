/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.Flags;

/**
 * The kind of modifiers.
 */
public enum Modifier implements IAdaptable {

    PUBLISHED("published", Flags.AccPublic, java.lang.reflect.Modifier.PUBLIC) { //$NON-NLS-1$

    },

    PUBLIC("public", Flags.AccPublic, java.lang.reflect.Modifier.PUBLIC) { //$NON-NLS-1$

    };

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

    // private Modifier(DefaultEnumType type, String id, int jdtFlags, int javaModifier) {
    // super(type, id);
    // this.jdtFlags = jdtFlags;
    // this.javaModifier = javaModifier;
    // }

    public final static Modifier getModifier(String id) {
        for (Modifier m : values()) {
            if (m.id.equals(id)) {
                return m;
            }
        }
        return null;
    }

    // @Deprecated
    // public String getName() {
    // return toString();
    // }
    //
    // @Deprecated
    // public final static Class<Modifier> getEnumType() {
    // return Modifier.class;
    // }

    @SuppressWarnings("unchecked")
    // eclipse interface is not type safe
    @Override
    public Object getAdapter(Class adapter) {
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
