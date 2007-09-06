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
     * Transform the qualified name to a String array. Each segment of the name
     * is placed in hierarchy order, e.g. "de.faktorips.devtools
     * <p>
     * <blockquote><pre>
     * segments[0] = de
     * segments[1] = faktorips
     * segments[2] = devtools
     * </pre></blockquote>
     * <p>
     * @param qName Full qualified package name.
     * @return Segments of the package name as an array.
     */
    public final static String[] getSegments(String qName) {
        String [] segments;

        if ((qName == null) || (qName.length()==0)) {
            return new String[0];
        }

        segments = StringUtils.split(qName, ".");

        return segments;
    }

    /**
     * Get the number of segments (folders) of a <code>IpsPackageFragment</code> or <code>IpsPackageFragmentRoot</code>.
     *
     * @param qName Full qualified package name.
     * @return Number of segments.
     */
    public final static int getSegmentCount(String qName) {
        return getSegments(qName).length;
    }

    /**
     * Extract subpackage name from a qualified name. The new string starts at the first
     * position of <code>qName</code> and ends at segment <code>max</code>.
     *
     * Returns <code>qName</code> if <code>max</code> is 0 or negative.
     * Returns an empty String if <code>qName</code> is empty.
     *
     * <p>
     * <blockquote><pre>
     * QNameUtil.getSubSegments("org.faktorips.devtools.model", 2);
     * </pre></blockquote>
     * <p>
     * returns "org.faktorips"
     *
     * @param qName Qualified name of the package.
     * @param max Amount of segments from the beginning.
     * @return Subpackage name.
     */
    public final static String getSubSegments(String qName, int max) {
        int segmentCount = getSegmentCount(qName);

        if (segmentCount == 0) {
           return new String("");
        }

        if ((max > 0) && (max < segmentCount)) {
            segmentCount = max;
        }

        String[] segments = getSegments(qName);
        String subString = new String("");

        for (int i = 0; i < segmentCount; i++) {
            if ((i+1) == segmentCount) {
                subString = subString.concat(segments[i]);
            } else {
                subString = subString.concat(segments[i] + ".");
            }
        }

        return subString;
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
