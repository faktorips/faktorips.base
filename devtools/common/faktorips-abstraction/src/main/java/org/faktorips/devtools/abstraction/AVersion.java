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

import org.apache.commons.lang3.StringUtils;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Yet another version pattern implementation.
 */
public class AVersion implements Comparable<AVersion> {

    public static final AVersion VERSION_ZERO = new AVersion(new long[] { 0L }, IpsStringUtils.EMPTY);

    private static final String DELIMITER = "."; //$NON-NLS-1$
    private static final Pattern NUMERIC = Pattern.compile("\\d+"); //$NON-NLS-1$
    private static final String QUALIFIER = "qualifier"; //$NON-NLS-1$
    // 24.1.0.ci_20230711-1341
    private static final Pattern QUALIFIER_SNAPSHOT = Pattern.compile("ci_\\d{8}-\\d{4}");
    // 24.1.0.a20221117-02
    private static final Pattern QUALIFIER_ALPHA = Pattern.compile("a\\d{8}-\\d{2}");
    // 24.1.0.m01
    private static final Pattern QUALIFIER_MILESTONE = Pattern.compile("m\\d{2}");
    // 24.1.0.rc01
    private static final Pattern QUALIFIER_RELEASE_CANDIDATE = Pattern.compile("rc\\d{2}");
    // 24.1.0.release
    private static final String QUALIFIER_RELEASE = "release";

    private final String versionString;
    private final long[] numericParts;
    private final String qualifier;

    private AVersion(long[] numericParts, String qualifier) {
        this.numericParts = numericParts;
        this.qualifier = qualifier;
        versionString = toString(numericParts, qualifier);
    }

    private static final String toString(long[] numericParts, String qualifier) {
        Stream<String> parts = Arrays.stream(numericParts).mapToObj(Long::toString);
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
            long diff = numericParts[i] - o.numericParts[i];
            if (diff != 0) {
                return diff > Integer.MAX_VALUE ? Integer.MAX_VALUE
                        : diff < Integer.MIN_VALUE ? Integer.MIN_VALUE : (int)diff;
            }
        }
        if (numericParts.length == o.numericParts.length) {
            return compareQualifiers(o);
        }
        return numericParts.length - o.numericParts.length;
    }

    private int compareQualifiers(AVersion o) {
        VersionType thisVersionType = VersionType.from(qualifier);
        VersionType otherVersionType = VersionType.from(o.qualifier);
        if (thisVersionType == otherVersionType) {
            // rc02 is higher than rc01
            return qualifier.compareTo(o.qualifier);
        }
        return thisVersionType.getPriority() - otherVersionType.getPriority();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(numericParts);
        return prime * result + qualifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
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

        NONE(0),
        OTHER(10),
        SNAPSHOT(20),
        ALPHA(30),
        MILESTONE(40),
        RELEASE_CANDIDATE(50),
        RELEASE(60);

        private final int priority;

        VersionType(int priority) {
            this.priority = priority;
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
            } else if (StringUtils.isNotBlank(qualifier)) {
                // some qualifier is higher than no qualifier
                return OTHER;
            } else {
                return NONE;
            }
        }
    }
}
