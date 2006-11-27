/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A collection of methods for qualified names and packages.
 * 
 * @author Jan Ortmann
 */
public class QNameUtil {

    /**
     * Returns the package name for a given class name. Returns an empty String
     * if the class name does not contain a package name.
     * 
     * @throws NullPointerException if the qName is null.
     */
    public final static String getPackageName(String qName) {
        if (qName==null) {
            return null;
        }
        int index = qName.lastIndexOf("."); //$NON-NLS-1$
        if (index == -1)
        {
            return ""; //$NON-NLS-1$
        }
        return qName.substring(0, index);
    }
    
    /**
     * Returns the unqualified name part of the given qualified name.
     * Returns <code>null</code> if qName is <code>null</code>.
     */
    public final static String getUnqualifiedName(String qName) {
        if (qName==null) {
            return null;
        }
        int index = qName.lastIndexOf('.');
        if (index==-1) {
            return qName;
        }
        if (index==qName.length()-1) {
            return ""; //$NON-NLS-1$
        }
        return qName.substring(index + 1);
    }

    /**
     * Concatenates package prefix and the packOrUnqualifiedName. If the package prefix 
     * is <code>null</code> or the empty string the packOrUnqualifiedName is returned.
     */
    public final static String concat(String packagePrefix, String packOrUnqualifiedName) {
        if (StringUtils.isEmpty(packagePrefix)) {
            return packOrUnqualifiedName;
        }
        if (StringUtils.isEmpty(packOrUnqualifiedName)) {
            return packagePrefix;
        }
        return packagePrefix + "." + packOrUnqualifiedName; //$NON-NLS-1$
    }
    
    /**
     * Transforms the given qualified name to a path. Returns <code>null</code> if qName 
     * is <code>null</code>.
     */
    public final static Path toPath(String qName) {
        if (qName==null) {
            return null;
        }
        return new Path(qName.replace('.', IPath.SEPARATOR));
    }
    
    private QNameUtil() {
    }

}
