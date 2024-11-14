/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.valueset;

import java.io.Serial;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link ValueSet} implementation that validates strings against a regular expression pattern.
 * <p>
 * This value set is designed for programmatic use in handwritten code only. It cannot be configured
 * through the IPS user interface or product definition files.
 * <p>
 * The value set never contains {@code null} and treats empty and blank strings as null values, i.e.
 * {@link #contains(String)} returns false. This behavior matches the {@code StringLengthValueSet}.
 * Note that {@link #getValues(boolean)} is not supported for this value set
 * 
 * @since 25.1
 */
public class RegexValueSet implements ValueSet<String> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Pattern pattern;

    /**
     * Creates a new regex value set with the given regular expression pattern.
     * 
     * @param regex the regular expression pattern, must not be blank
     * @throws IllegalArgumentException if regex is blank
     */
    public RegexValueSet(String regex) {
        if (regex == null || regex.isBlank()) {
            throw new IllegalArgumentException("The regex may not be null or empty");
        }
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean contains(String value) {
        return value != null && !value.isBlank() && pattern.matcher(value).matches();
    }

    @Override
    public boolean isDiscrete() {
        return false;
    }

    @Override
    public boolean containsNull() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isRange() {
        return false;
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Set<String> getValues(boolean excludeNull) {
        throw new UnsupportedOperationException("The method getValues is not supported for RegexValueSet");
    }

    @Override
    public boolean isUnrestricted(boolean excludeNull) {
        return false;
    }

    @Override
    public boolean isSubsetOf(ValueSet<String> otherValueSet) {
        if (otherValueSet instanceof UnrestrictedValueSet) {
            return true;
        }
        if (otherValueSet instanceof RegexValueSet other) {
            return other.pattern.pattern().equals(pattern.pattern());
        }
        return false;
    }

    @Override
    public Optional<Class<String>> getDatatype() {
        return Optional.of(String.class);
    }
}