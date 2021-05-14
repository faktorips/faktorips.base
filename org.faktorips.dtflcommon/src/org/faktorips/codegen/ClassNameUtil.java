/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen;

import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassNameUtil {

    private ClassNameUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Takes a name like a class name and removes the package information from the beginning.
     */
    public static final String unqualifiedName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf("."); //$NON-NLS-1$
        if (index == -1) {
            return qualifiedName;
        }
        return qualifiedName.substring(index + 1);
    }

    /**
     * Returns the qualified name for the given package name and unqualified name. If packageName is
     * <code>null</code> or the empty String the unqualified name is returned.
     * 
     * @throws NullPointerException if unqualifiedName is <code>null</code>.
     */
    public static final String qualifiedName(String packageName, String unqualifiedName) {
        ArgumentCheck.notNull(unqualifiedName);
        if (packageName == null || packageName.length() == 0) {
            return unqualifiedName;
        }
        return packageName + '.' + unqualifiedName;
    }

    /**
     * Returns the package name for a given class name. Returns an empty String if the class name
     * does not contain a package name.
     * 
     * @throws NullPointerException if the qualifiedClassName is null.
     */
    public static final String getPackageName(String qualifiedClassName) {
        int index = qualifiedClassName.lastIndexOf("."); //$NON-NLS-1$
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedClassName.substring(0, index);
    }
}
