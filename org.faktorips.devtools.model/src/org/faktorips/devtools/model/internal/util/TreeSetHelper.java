/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.util;

import java.util.TreeSet;

import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;

/**
 * A helper class to create tree sets with special comparators.
 * 
 * @author dirmeier
 */
public class TreeSetHelper {

    private TreeSetHelper() {
        // Utility class not to be instantiated.
    }

    /**
     * Creates and returns a tree set containing <code>IIpsSrcFile</code>s with alphabetic ordered
     * names. If two names are equal but different source files, both source files are stored in the
     * tree set.
     */
    public static TreeSet<IIpsSrcFile> newIpsSrcFileTreeSet() {
        return new TreeSet<>((o1, o2) -> {
            if (o1.equals(o2)) {
                return 0;
            }
            int result = o1.getName().compareToIgnoreCase(o2.getName());
            if (result == 0) {
                return -1;
            } else {
                return result;
            }
        });
    }

}
