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

package org.faktorips.codegen;

import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Jan Ortmann
 */
public class ClassNameUtil {

    /**
     * Takes a name like a class name and removes the package information from the beginning.
     */
    public final static String unqualifiedName(String qualifiedName) {
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
    public final static String qualifiedName(String packageName, String unqualifiedName) {
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
    public final static String getPackageName(String qualifiedClassName) {
        int index = qualifiedClassName.lastIndexOf("."); //$NON-NLS-1$
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return qualifiedClassName.substring(0, index);
    }

    private ClassNameUtil() {
        // Utility class not to be instantiated.
    }

}
