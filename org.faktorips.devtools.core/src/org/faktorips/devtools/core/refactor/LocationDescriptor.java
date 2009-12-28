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

import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.util.ArgumentCheck;

/**
 * A <tt>LocationDescriptor</tt> represents a location of an <tt>IIpsElement</tt>. It is currently
 * used by the "Rename Type" and "Move Type" refactorings for example.
 * <p>
 * A location consists of an <tt>IIpsPackageFragmentRoot</tt>, and a qualified name.
 * 
 * @author Alexander Weickmann
 */
public final class LocationDescriptor {

    /** The location's <tt>IIpsPackageFragmentRoot</tt>. */
    private final IIpsPackageFragmentRoot ipsPackageFragmentRoot;

    /** The location's qualified name. */
    private final String qualifiedName;

    /** Lazily initialized, cached hash code. */
    private volatile int hashCode;

    /**
     * Creates a <tt>LocationDescriptor</tt>.
     * 
     * @param ipsPackageFragmentRoot The location's <tt>IIpsPackageFragmentRoot</tt>.
     * @param name The location's qualified name.
     * 
     * @throws NullPointerException If any parameter is <tt>null</tt>.
     */
    public LocationDescriptor(IIpsPackageFragmentRoot ipsPackageFragmentRoot, String qualifiedName) {
        ArgumentCheck.notNull(new Object[] { ipsPackageFragmentRoot, qualifiedName });
        this.ipsPackageFragmentRoot = ipsPackageFragmentRoot;
        this.qualifiedName = qualifiedName;
    }

    /** Returns the location's <tt>IIpsPackageFragmentRoot</tt>. */
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return ipsPackageFragmentRoot;
    }

    /** Returns the location's qualified name. */
    public String getQualifiedName() {
        return qualifiedName;
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
        return ipsPackageFragmentRoot.equals(otherLocationDescriptor.ipsPackageFragmentRoot)
                && qualifiedName.equals(otherLocationDescriptor.qualifiedName);
    }

    @Override
    public int hashCode() {
        int result = hashCode;
        if (result == 0) {
            result = 17;
            result = 31 * result + ipsPackageFragmentRoot.hashCode();
            result = 31 * result + qualifiedName.hashCode();
            hashCode = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return "LocationDescriptor: IPS Package Fragment Root [" + ipsPackageFragmentRoot + "] Qualified Name ["
                + qualifiedName + "]";
    }

}
