/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.util;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * A collection of utility methods regarding qualified names and packages.
 * 
 * @author Jan Ortmann
 */
public class QNameUtil {

    /**
     * Returns the package name for a given class name. Returns an empty String if the class name
     * does not contain a package name.
     * 
     * @throws NullPointerException if <tt>qName</tt> is <tt>null</tt>.
     */
    public final static String getPackageName(String qName) {
        if (qName == null) {
            return null;
        }
        int index = qName.lastIndexOf("."); //$NON-NLS-1$
        if (index == -1) {
            return ""; //$NON-NLS-1$
        }
        return qName.substring(0, index);
    }

    /**
     * Returns the unqualified name part of the given qualified name. Returns <code>null</code> if
     * <tt>qName</tt> is <code>null</code>.
     */
    public final static String getUnqualifiedName(String qName) {
        if (qName == null) {
            return null;
        }
        int index = qName.lastIndexOf('.');
        if (index == -1) {
            return qName;
        }
        if (index == qName.length() - 1) {
            return ""; //$NON-NLS-1$
        }
        return qName.substring(index + 1);
    }

    /**
     * Concatenates package prefix and the <tt>packOrUnqualifiedName</tt>. If the package prefix is
     * <code>null</code> or the empty string the <tt>packOrUnqualifiedName</tt> is returned.
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
     * Transforms the qualified name to a string array containing it's segments and returns it. Each
     * segment of the name is placed in hierarchical order, e.g. "de.faktorips.devtools":
     * <p>
     * 
     * <pre>
     * segments[0] = de
     * segments[1] = faktorips
     * segments[2] = devtools
     * </pre>
     * 
     * @param qName The fully qualified package name.
     */
    public final static String[] getSegments(String qName) {
        String[] segments;

        if ((qName == null) || (qName.length() == 0)) {
            return new String[0];
        }

        segments = StringUtils.split(qName, "."); //$NON-NLS-1$

        return segments;
    }

    /**
     * Get the number of segments (folders) of a <code>IpsPackageFragment</code> or
     * <code>IpsPackageFragmentRoot</code>.
     * 
     * @param qName Full qualified package name.
     * @return Number of segments.
     */
    public final static int getSegmentCount(String qName) {
        return getSegments(qName).length;
    }

    /**
     * Extracts the sub package name from a qualified name. The new string starts at the first
     * position of <code>qName</code> and ends at segment <code>numberOfSegments</code>.
     * <p>
     * Returns the <code>qName</code> if <code>numberOfSegments</code> is less equals 0 or exceeds
     * the number of segments of <tt>qName</tt>. Returns an empty String if <code>qName</code> is
     * empty.
     * <p>
     * Example: The following call returns "org.faktorips".
     * <p>
     * 
     * <pre>
     * QNameUtil.getSubSegments(&quot;org.faktorips.devtools.model&quot;, 2);
     * </pre>
     * 
     * @param qName Qualified name of the package.
     * @param numberOfSegments Amount of segments from the beginning.
     */
    public final static String getSubSegments(String qName, int numberOfSegments) {
        if (qName == null) {
            return null;
        }

        int segmentCount = getSegmentCount(qName);
        if (segmentCount == 0) {
            return qName;
        }

        if (numberOfSegments > 0 && numberOfSegments < segmentCount) {
            segmentCount = numberOfSegments;
        }

        String[] segments = getSegments(qName);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < segmentCount; i++) {
            buf.append(segments[i]);
            if (i + 1 < segmentCount) {
                buf.append('.');
            }
        }

        return buf.toString();
    }

    /**
     * Transforms the given qualified name to a path. Returns <code>null</code> if <tt>qName</tt> is
     * <code>null</code>.
     */
    public final static Path toPath(String qName) {
        if (qName == null) {
            return null;
        }
        return new Path(qName.replace('.', IPath.SEPARATOR));
    }

    private QNameUtil() {
        // Utility class not to be instantiated.
    }

}
