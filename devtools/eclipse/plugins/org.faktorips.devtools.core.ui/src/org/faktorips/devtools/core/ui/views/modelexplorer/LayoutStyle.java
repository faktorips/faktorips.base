/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modelexplorer;

import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;

/**
 * Layout styles for the model explorer.
 * 
 * @author Jan Ortmann
 */
public enum LayoutStyle {

    /**
     * All package fragments are displayed under their IpsPackageFragmentRoot. The explorer shows
     * packages not in a hierarchical way. The flat layout also contains the default package. (This
     * is the package with the name "").
     * <p>
     * "(defaultPackage)"<br>
     * "org"<br>
     * "org.life"<br>
     * "org.life.coverages"<br>
     * "org.motor"<br>
     */
    FLAT(1),

    /**
     * Package fragments are displayed in a hierarchical way that corresponds to the underlying
     * folder structure. Note that the hierarchical layout does not contain the default package!
     * (This is the package with the name "").
     * <p>
     * Example:
     * <p>
     * "org"<br>
     * " life"<br>
     * " coverages"<br>
     * " motor"<br>
     */
    HIERACHICAL(0);

    // for historical reason: 0 is hierarchical, and flat is 1.
    private int id;

    LayoutStyle(int id) {
        this.id = id;
    }

    /**
     * Returns the layout style by id. Needed as before version 2.4.2, the layout style was
     * represented by int constants and is saved in the dialog settings.
     */
    public static final LayoutStyle getById(int id) {
        if (id == HIERACHICAL.getId()) {
            return HIERACHICAL;
        }
        if (id == FLAT.getId()) {
            return FLAT;
        }
        throw new RuntimeException("Unknown layout style id " + id); //$NON-NLS-1$
    }

    /**
     * Returns the layout style's id. Needed as before version 2.4.2, the layout style was
     * represented by int constants and is saved in the dialog settings.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the package's parent according to the layout style. If the style is {@link #FLAT}, a
     * package's parent is always the package fragment root it belongs to. If the style is
     * {@link #HIERACHICAL}, it is the parent package (e.g. for "org.coverages" the parent package
     * is "org"). If this is a top level package (like "org" or "com"), the parent is the package
     * fragment root (and not the default package)!
     * 
     * @return The package's parent or <code>null</code> if pack is <code>null</code>.
     */
    public IIpsElement getParent(IIpsPackageFragment pack) {
        if (pack == null) {
            return null;
        }
        if (this == FLAT) {
            return pack.getParent();
        }
        if (this == HIERACHICAL) {
            if (pack.isDefaultPackage()) {
                return pack.getRoot();
            }
            IIpsPackageFragment parentPack = pack.getParentIpsPackageFragment();
            if (parentPack.isDefaultPackage()) {
                return parentPack.getRoot();
            }
            return parentPack;
        }
        throw new RuntimeException("Unknown layout style " + this); //$NON-NLS-1$
    }

}
