/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.value;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.model.IInternationalString;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.ISupportedLanguage;
import org.faktorips.devtools.model.value.IValue;
import org.faktorips.values.LocalizedString;

/**
 * This class bundles a set of utility methods for {@link IValue} objects. At the moment only
 * {@link StringValue} and {@link InternationalStringValue} are supported.
 * <p>
 * To use the util create an instance of this class by calling {@link #createUtil(IValue)}.
 * 
 * @author dirmeier
 */
public abstract class ValueUtil {

    /**
     * Create a new ValueUtil for an instance of {@link IValue}.
     * 
     */
    @SuppressWarnings("unchecked")
    // this type cannot be checked safety but is verified by checking the content
    public static ValueUtil createUtil(IValue<?> value) {
        if (value.getContent() instanceof IInternationalString) {
            return new InternationalStringValueUtil((IValue<IInternationalString>)value);
        } else if (value.getContent() == null || value.getContent() instanceof String) {
            return new StringValueUtil((IValue<String>)value);
        } else {
            throw new IllegalArgumentException("The value " + value + " is not supported by ValidationUtil."); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    /**
     * Create a list of string values. Every entry does identically represent one value in the
     * {@link IValue}.
     * <p>
     * The list of String is not intended to be displayed.
     * 
     */
    public abstract Set<LocalizedString> getLocalizedIdentifiers();

    /**
     * Check whether this value is empty or at least partly empty.
     * 
     * @param ipsProject The project is needed in case of international string to get all available
     *            languages
     */
    public abstract boolean isPartlyEmpty(IIpsProject ipsProject);

    private static class InternationalStringValueUtil extends ValueUtil {

        private final IValue<IInternationalString> value;

        public InternationalStringValueUtil(IValue<IInternationalString> value) {
            this.value = value;
        }

        @Override
        public Set<LocalizedString> getLocalizedIdentifiers() {
            HashSet<LocalizedString> result = new HashSet<>();
            for (LocalizedString localizedString : value.getContent().values()) {
                result.add(localizedString);
            }
            return result;
        }

        @Override
        public boolean isPartlyEmpty(IIpsProject ipsProject) {
            Set<ISupportedLanguage> supportedLanguages = ipsProject.getReadOnlyProperties().getSupportedLanguages();
            for (ISupportedLanguage supportedLanguage : supportedLanguages) {
                LocalizedString localizedString = value.getContent().get(supportedLanguage.getLocale());
                if (StringUtils.isEmpty(localizedString.getValue())) {
                    return true;
                }
            }
            return false;
        }

    }

    private static class StringValueUtil extends ValueUtil {

        private final IValue<String> value;

        public StringValueUtil(IValue<String> value) {
            this.value = value;
        }

        @Override
        public Set<LocalizedString> getLocalizedIdentifiers() {
            HashSet<LocalizedString> result = new HashSet<>();
            result.add(new LocalizedString(null, value.getContent()));
            return result;
        }

        @Override
        public boolean isPartlyEmpty(IIpsProject ipsProject) {
            return StringUtils.isEmpty(value.getContent());
        }

    }

}
