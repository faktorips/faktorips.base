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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Yet another version pattern implementation.
 */
public final class AVersion implements Comparable<AVersion> {

    public static final AVersion VERSION_ZERO = new AVersion(new long[] { 0L }, IpsStringUtils.EMPTY);

    private static final String DELIMITER = "."; //$NON-NLS-1$
    private static final Pattern NUMERIC = Pattern.compile("\\d+"); //$NON-NLS-1$
    private static final String QUALIFIER = "qualifier"; //$NON-NLS-1$
    // 24.1.0.ci_20230711-1341
    private static final Pattern QUALIFIER_SNAPSHOT = Pattern.compile("ci_(\\d{8})-\\d{4}"); //$NON-NLS-1$
    // 24.1.0.a20221117-02
    private static final Pattern QUALIFIER_ALPHA = Pattern.compile("a(\\d{8})-\\d{2}"); //$NON-NLS-1$
    // 24.1.0.m01
    private static final Pattern QUALIFIER_MILESTONE = Pattern.compile("m\\d{2}"); //$NON-NLS-1$
    // 24.1.0.rc01
    private static final Pattern QUALIFIER_RELEASE_CANDIDATE = Pattern.compile("rc\\d{2}"); //$NON-NLS-1$
    // 24.1.0.release
    private static final String QUALIFIER_RELEASE = "release"; //$NON-NLS-1$

    private final String versionString;
    private final long[] numericParts;
    private final String qualifier;
    // cached
    private final int hashCode;

    private AVersion(long[] numericParts, String qualifier) {
        this.numericParts = numericParts;
        this.qualifier = qualifier;
        versionString = toString(numericParts, qualifier);
        // cache since immutable
        hashCode = computeHashCode();
    }

    private static String toString(long[] numericParts, String qualifier) {
        Stream<String> parts = Arrays.stream(numericParts).mapToObj(Long::toString);
        if (IpsStringUtils.isNotBlank(qualifier)) {
            parts = Stream.concat(parts, Stream.of(qualifier));
        }
        return parts.collect(Collectors.joining(DELIMITER));
    }

    /**
     * Parses the given version string to {@link AVersion}, dropping {@value #QUALIFIER} and
     * trailing {@code .0} parts.
     */
    public static AVersion parse(String versionString) {
        requireNonNull(versionString, "versionString must not be null"); //$NON-NLS-1$
        if (IpsStringUtils.isBlank(versionString)) {
            return VERSION_ZERO;
        }

        LinkedList<Long> numericParts = new LinkedList<>();
        String qualifier = IpsStringUtils.EMPTY;

        for (int lastIndex = 0, index = getNextDelimiterPosition(versionString, lastIndex);
                /* as long as we find another delimiter */
                index > 0;
                /* look for another delimiter, beginning just after the last one */
                lastIndex = index + 1, index = getNextDelimiterPosition(versionString, lastIndex)) {
            String part = versionString.substring(lastIndex, index);
            if (NUMERIC.matcher(part).matches()) {
                numericParts.add(Long.parseLong(part));
            } else {
                qualifier = versionString.substring(lastIndex);
                break;
            }
        }
        while (numericParts.size() > 1 && numericParts.getLast().equals(0L)) {
            numericParts.removeLast();
        }
        if (QUALIFIER.equals(qualifier)) {
            qualifier = IpsStringUtils.EMPTY;
        }
        return new AVersion(numericParts.stream().mapToLong(i -> i).toArray(), qualifier);
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
            int cmp = Long.compare(numericParts[i], o.numericParts[i]);
            if (cmp != 0) {
                return cmp;
            }
        }
        if (numericParts.length != o.numericParts.length) {
            return Integer.compare(numericParts.length, o.numericParts.length);
        }
        return compareQualifiers(o);
    }

    private int compareQualifiers(AVersion o) {
        VersionType thisType = VersionType.from(qualifier);
        VersionType otherType = VersionType.from(o.qualifier);

        // Timestamp-based qualifiers are always compared chronologically first
        if (thisType.isTimestampBased() && otherType.isTimestampBased()) {
            int cmp = thisType.extractTimestamp(qualifier)
                    .compareTo(otherType.extractTimestamp(o.qualifier));
            if (cmp != 0) {
                return cmp;
            }
            // same date -> fall through
        }

        if (thisType == otherType) {
            // For same type, lexical qualifier comparison (e.g. rc02 > rc01)
            return qualifier.compareTo(o.qualifier);
        }

        return Integer.compare(thisType.getPriority(), otherType.getPriority());
    }

    private int computeHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(numericParts);
        return prime * result + qualifier.hashCode();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AVersion other)) {
            return false;
        }
        return Arrays.equals(numericParts, other.numericParts)
                && qualifier.equals(other.qualifier);
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
     * Returns a new {@link AVersion version} consisting only of the {@link #getMajor() major},
     * {@link #getMinor() minor} and {@link #getPatch() patch} parts of this version number.
     */
    public AVersion majorMinorPatch() {
        return numericParts.length >= 3
                ? new AVersion(Arrays.copyOfRange(numericParts, 0, 3), IpsStringUtils.EMPTY)
                : new AVersion(Arrays.copyOf(numericParts, numericParts.length), IpsStringUtils.EMPTY);
    }

    /**
     * Returns the major part of this version number, which is the number before the first
     * {@value #DELIMITER}. Should the version number not contain a major part, {@code "0"} is
     * returned.
     */
    public String getMajor() {
        return numericParts.length >= 1 ? Long.toString(numericParts[0]) : "0"; //$NON-NLS-1$
    }

    /**
     * Returns the minor part of this version number, which is the number between the first and
     * second {@value #DELIMITER}. Should the version number not contain a minor part, {@code "0"}
     * is returned.
     */
    public String getMinor() {
        return numericParts.length >= 2 ? Long.toString(numericParts[1]) : "0"; //$NON-NLS-1$
    }

    /**
     * Returns the patch part of this version number, which is the number between the second and
     * third {@value #DELIMITER}. Should the version number not contain a patch part, {@code "0"} is
     * returned.
     */
    public String getPatch() {
        return numericParts.length >= 3 ? Long.toString(numericParts[2]) : "0"; //$NON-NLS-1$
    }

    /**
     * Whether the qualifier identifies the version as release.
     *
     * @return {@code true} if the version ends with {@code .release}
     */
    public boolean isRelease() {
        return VersionType.RELEASE == VersionType.from(qualifier);
    }

    /**
     * Whether the qualifier identifies the version as release candidate.
     *
     * @return {@code true} if the version ends with {@code .rcXX}
     */
    public boolean isReleaseCandidate() {
        return VersionType.RELEASE_CANDIDATE == VersionType.from(qualifier);
    }

    /**
     * Whether the qualifier identifies the version as milestone.
     *
     * @return {@code true} if the version ends with {@code .mXX}
     */
    public boolean isMilestone() {
        return VersionType.MILESTONE == VersionType.from(qualifier);
    }

    /**
     * Whether the qualifier identifies the version as alpha.
     *
     * @return {@code true} if the version ends with {@code .aYYYYMMDD-XX}
     */
    public boolean isAlpha() {
        return VersionType.ALPHA == VersionType.from(qualifier);
    }

    /**
     * Whether the qualifier identifies the version as snapshot.
     *
     * @return {@code true} if the version ends with {@code .ci_YYYYMMDD-HHMM}
     */
    public boolean isSnapshot() {
        return VersionType.SNAPSHOT == VersionType.from(qualifier);
    }

    /**
     * Internal enum to classify the qualifier of a version.
     */
    private enum VersionType {

        NONE(0, false),
        OTHER(10, false),
        SNAPSHOT(20, true),
        ALPHA(30, true),
        MILESTONE(40, false),
        RELEASE_CANDIDATE(50, false),
        RELEASE(60, false);

        private final int priority;
        private final boolean timestampBased;

        VersionType(int priority, boolean timestampBased) {
            this.priority = priority;
            this.timestampBased = timestampBased;
        }

        /**
         * The priority of a version is determined by its qualifier. The order of the qualifiers
         * are: no, some, snapshot, alpha, milestone, release candidate and release.
         *
         * @return the priority of a version qualifier
         */
        public int getPriority() {
            return priority;
        }

        /**
         * {@return {@code true} if this {@link VersionType} uses a qualifier that encodes a
         * timestamp (e.g. {@code a20251024-01} or {@code ci_20251024-1359}). {@code false}
         * otherwise.}
         */
        public boolean isTimestampBased() {
            return timestampBased;
        }

        /**
         * Extracts the timestamp portion of the given qualifier for this {@link VersionType}.
         *
         * <p>
         * This method is only valid for timestamp-based types:
         * <ul>
         * <li>{@link #ALPHA} with qualifiers like {@code a20250424-01}</li>
         * <li>{@link #SNAPSHOT} (CI builds) with qualifiers like {@code ci_20250424-1359}</li>
         * </ul>
         *
         * @param qualifier the qualifier string part of an {@link AVersion}
         * @return the extracted timestamp as a string in {@code yyyyMMdd} format
         * @throws IllegalArgumentException if the qualifier does not match the expected pattern or
         *             this version type is not timestamp-based
         */
        public String extractTimestamp(String qualifier) {
            if (this == ALPHA) {
                Matcher m = QUALIFIER_ALPHA.matcher(qualifier);
                if (m.matches()) {
                    return m.group(1);
                }
            } else if (this == SNAPSHOT) {
                Matcher m = QUALIFIER_SNAPSHOT.matcher(qualifier);
                if (m.matches()) {
                    return m.group(1);
                }
            }
            throw new IllegalArgumentException(
                    "Invalid qualifier for timestamp-based type " + this + ": " + qualifier);
        }

        /**
         * Parses the qualifier of a version.
         *
         * @param qualifier the last non numeric part of a version e.g.: 24.1.1.i_am_the_qualifier
         * @return an enum representing the type of version.
         */
        public static VersionType from(String qualifier) {
            if (QUALIFIER_SNAPSHOT.matcher(qualifier).matches()) {
                return SNAPSHOT;
            } else if (QUALIFIER_ALPHA.matcher(qualifier).matches()) {
                return ALPHA;
            } else if (QUALIFIER_MILESTONE.matcher(qualifier).matches()) {
                return MILESTONE;
            } else if (QUALIFIER_RELEASE_CANDIDATE.matcher(qualifier).matches()) {
                return RELEASE_CANDIDATE;
            } else if (QUALIFIER_RELEASE.equals(qualifier)) {
                return RELEASE;
            } else if (IpsStringUtils.isNotBlank(qualifier)) {
                // some qualifier is higher than no qualifier
                return OTHER;
            } else {
                return NONE;
            }
        }
    }
}
