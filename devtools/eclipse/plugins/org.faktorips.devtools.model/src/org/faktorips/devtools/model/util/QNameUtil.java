/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.faktorips.runtime.internal.IpsStringUtils;

import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * A collection of utility methods regarding qualified names and packages.
 * 
 * @author Jan Ortmann
 */
public class QNameUtil {

    private QNameUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Returns the package name for a given class name. Returns an empty String if the class name
     * does not contain a package name.
     * 
     * @throws NullPointerException if <code>qName</code> is <code>null</code>.
     */
    public static final String getPackageName(String qName) {
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
     * <code>qName</code> is <code>null</code>.
     */
    public static final String getUnqualifiedName(String qName) {
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
     * Concatenates package prefix and the <code>packOrUnqualifiedName</code>. If the package prefix
     * is <code>null</code> or the empty string the <code>packOrUnqualifiedName</code> is returned.
     */
    public static final String concat(String packagePrefix, String packOrUnqualifiedName) {
        if (IpsStringUtils.isEmpty(packagePrefix)) {
            return packOrUnqualifiedName;
        }
        if (IpsStringUtils.isEmpty(packOrUnqualifiedName)) {
            return packagePrefix;
        }
        return packagePrefix + "." + packOrUnqualifiedName; //$NON-NLS-1$
    }

    /**
     * Transforms the qualified name to a string array containing it's segments and returns it. Each
     * segment of the name is placed in hierarchical order, e.g. "de.faktorips.devtools":
     * 
     * <pre>
     * segments[0] = de
     * segments[1] = faktorips
     * segments[2] = devtools
     * </pre>
     * 
     * @param qName The fully qualified package name.
     */
    public static final String[] getSegments(String qName) {
        if ((qName == null) || (qName.length() == 0)) {
            return new String[0];
        }

        return StringUtils.split(qName, ".");
    }

    /**
     * Get the number of segments (folders) of a <code>IpsPackageFragment</code> or
     * <code>IpsPackageFragmentRoot</code>.
     * 
     * @param qName Full qualified package name.
     * @return Number of segments.
     */
    public static final int getSegmentCount(String qName) {
        return getSegments(qName).length;
    }

    /**
     * Extracts the sub package name from a qualified name. The new string starts at the first
     * position of <code>qName</code> and ends at segment <code>numberOfSegments</code>.
     * <p>
     * Returns the <code>qName</code> if <code>numberOfSegments</code> is less equals 0 or exceeds
     * the number of segments of <code>qName</code>. Returns an empty String if <code>qName</code>
     * is empty.
     * <p>
     * Example: The following call returns "org.faktorips".
     * 
     * <pre>
     * QNameUtil.getSubSegments(&quot;org.faktorips.devtools.model&quot;, 2);
     * </pre>
     * 
     * @param qName Qualified name of the package.
     * @param numberOfSegments Amount of segments from the beginning.
     */
    public static final String getSubSegments(String qName, int numberOfSegments) {
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
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segmentCount; i++) {
            sb.append(segments[i]);
            if (i + 1 < segmentCount) {
                sb.append('.');
            }
        }

        return sb.toString();
    }

    /**
     * Transforms the given qualified name to a path. Returns <code>null</code> if
     * <code>qName</code> is <code>null</code>.
     */
    @CheckForNull
    public static final Path toPath(String qName) {
        if (IpsStringUtils.isBlank(qName)) {
            return null;
        }
        String[] pathParts = qName.split("\\."); //$NON-NLS-1$
        if (pathParts.length == 1) {
            return Path.of(pathParts[0]);
        }
        return Path.of(pathParts[0], Arrays.copyOfRange(pathParts, 1, pathParts.length));
    }
}
