/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Yet another version pattern implementation.
 */
public class AVersion implements Comparable<AVersion> {

    public static final AVersion VERSION_ZERO = new AVersion(new int[] { 0 }, IpsStringUtils.EMPTY);

    private static final String DELIMITER = "."; //$NON-NLS-1$
    private static final Pattern NUMERIC = Pattern.compile("\\d+"); //$NON-NLS-1$
    private static final String QUALIFIER = "qualifier"; //$NON-NLS-1$

    private final String versionString;
    private final int[] numericParts;
    private final String qualifier;

    private AVersion(int[] numericParts, String qualifier) {
        this.numericParts = numericParts;
        this.qualifier = qualifier;
        this.versionString = toString(numericParts, qualifier);
    }

    private static final String toString(int[] numericParts, String qualifier) {
        Stream<String> parts = Arrays.stream(numericParts).mapToObj(i -> Integer.toString(i));
        if (IpsStringUtils.isNotBlank(qualifier)) {
            parts = Stream.concat(parts, Stream.of(qualifier));
        }
        return parts.collect(Collectors.joining(DELIMITER));
    }

    /**
     * Parses the given version string to {@link AVersion}, dropping {@value #QUALIFIER} and
     * trailing '.0' parts.
     */
    public static AVersion parse(String versionString) {
        requireNonNull(versionString, "versionString must not be null"); //$NON-NLS-1$
        LinkedList<Integer> numericParts = new LinkedList<>();
        String qualifier = IpsStringUtils.EMPTY;

        for (int lastIndex = 0, index = getNextDelimiterPosition(versionString, lastIndex);
                /* as long as we find another delimiter */
                index > 0;
                /* look for another delimiter, beginning just after the last one */
                lastIndex = index + 1, index = getNextDelimiterPosition(versionString, lastIndex)) {
            String part = versionString.substring(lastIndex, index);
            if (NUMERIC.matcher(part).matches()) {
                numericParts.add(Integer.parseInt(part));
            } else {
                qualifier = versionString.substring(lastIndex);
                break;
            }
        }
        while (numericParts.size() > 1 && numericParts.getLast().equals(0)) {
            numericParts.removeLast();
        }
        if (QUALIFIER.equals(qualifier)) {
            qualifier = IpsStringUtils.EMPTY;
        }
        return new AVersion(numericParts.stream().mapToInt(i -> i).toArray(), qualifier);
    }

    private static int getNextDelimiterPosition(String versionString, int lastIndex) {
        if (lastIndex >= versionString.length()) {
            return -1;
        }
        int index = versionString.indexOf('.', lastIndex);
        return index < 0 ? versionString.length() : index;
    }

    @Override
    public int compareTo(AVersion o) {
        for (int i = 0; i < Math.min(numericParts.length, o.numericParts.length); i++) {
            int diff = numericParts[i] - o.numericParts[i];
            if (diff != 0) {
                return diff;
            }
        }
        if (numericParts.length == o.numericParts.length) {
            return qualifier.compareTo(o.qualifier);
        }
        return numericParts.length - o.numericParts.length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(numericParts);
        result = prime * result + qualifier.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AVersion other = (AVersion)obj;
        if (!Arrays.equals(numericParts, other.numericParts)) {
            return false;
        }
        return qualifier.equals(other.qualifier);
    }

    @Override
    public String toString() {
        return versionString;
    }

    /**
     * Returns a new {@link AVersion version} consisting only of the {@link #getMajor() major} and
     * {@link #getMinor() minor} parts of this version number.
     */
    public AVersion majorMinor() {
        return numericParts.length >= 2
                ? new AVersion(Arrays.copyOfRange(numericParts, 0, 2), IpsStringUtils.EMPTY)
                : new AVersion(Arrays.copyOf(numericParts, numericParts.length), IpsStringUtils.EMPTY);
    }

    /**
     * Returns the major part of this version number, which is the number before the first
     * {@value #DELIMITER}. Should the version number not contain a major part, {@code "0"} is
     * returned.
     */
    public String getMajor() {
        return numericParts.length >= 1 ? Integer.toString(numericParts[0]) : "0"; //$NON-NLS-1$
    }

    /**
     * Returns the minor part of this version number, which is the number between the first and
     * second {@value #DELIMITER}. Should the version number not contain a minor part, {@code "0"}
     * is returned.
     */
    public String getMinor() {
        return numericParts.length >= 2 ? Integer.toString(numericParts[1]) : "0"; //$NON-NLS-1$
    }

}
