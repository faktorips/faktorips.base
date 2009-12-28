/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.refactor;

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.ArgumentCheck;

/**
 * A <tt>LocationDescriptor</tt> represents a location of an <tt>IIpsElement</tt>. It is currently
 * used by the "Rename Type" and "Move Type" refactorings for example.
 * <p>
 * A location consists of an <tt>IIpsPackageFragment</tt>, and an unqualified name.
 * 
 * @author Alexander Weickmann
 */
public final class LocationDescriptor {

    /** The location's <tt>IIpsPackageFragment</tt>. */
    private final IIpsPackageFragment ipsPackageFragment;

    /** The location's unqualified name. */
    private final String name;

    /** Lazily initialized, cached hash code. */
    private volatile int hashCode;

    /**
     * Creates a <tt>LocationDescriptor</tt>.
     * 
     * @param ipsPackageFragment The location's <tt>IIpsPackageFragment</tt>.
     * @param name The location's unqualified name.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public LocationDescriptor(IIpsPackageFragment ipsPackageFragment, String name) {
        ArgumentCheck.notNull(new Object[] { ipsPackageFragment, name });
        this.ipsPackageFragment = ipsPackageFragment;
        this.name = name;
    }

    /** Returns the location's <tt>IIpsPackageFragment</tt>. */
    public IIpsPackageFragment getIpsPackageFragment() {
        return ipsPackageFragment;
    }

    /** Returns the location's name. */
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LocationDescriptor)) {
            return false;
        }
        LocationDescriptor otherLocationDescriptor = (LocationDescriptor)obj;
        return ipsPackageFragment.equals(otherLocationDescriptor.ipsPackageFragment)
                && name.equals(otherLocationDescriptor.name);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + ipsPackageFragment.hashCode();
            result = 31 * result + name.hashCode();
            hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return "LocationDescriptor: IPS Package Fragment [" + ipsPackageFragment + "] Name [" + name + "]";
    }

    /**
     * Returns the qualified name build from the location's <tt>IIpsPackageFragment</tt> and the
     * location's unqualified name.
     */
    public String getQualifiedName() {
        return (ipsPackageFragment.isDefaultPackage()) ? name : ipsPackageFragment.getName() + "." + name;
    }

}
