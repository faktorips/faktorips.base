/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.Comparator;
import java.util.TreeSet;

import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;

/**
 * A helper class to create tree sets with special comparators.
 * 
 * @author dirmeier
 */
public class TreeSetHelper {

    /**
     * Creates and returns a tree set containing <code>IIpsSrcFile</code>s with alphabetic ordered
     * names. If two names are equal but different source files, both source files are stored in the
     * tree set.
     */
    public static TreeSet<IIpsSrcFile> newIpsSrcFileTreeSet() {
        return new TreeSet<IIpsSrcFile>(new Comparator<IIpsSrcFile>() {
            @Override
            public int compare(IIpsSrcFile o1, IIpsSrcFile o2) {
                if (o1.equals(o2)) {
                    return 0;
                }
                int result = o1.getName().compareToIgnoreCase(o2.getName());
                if (result == 0) {
                    return -1;
                } else {
                    return result;
                }
            }
        });
    }

    private TreeSetHelper() {
        // Utility class not to be instantiated.
    }

}
