/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.builder.settings;

import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.devtools.model.valueset.ValueSetType;

/**
 * The possible values for the valueSetMethods setting for the {@link IIpsArtefactBuilderSetConfig
 * Faktor-IPS compiler options}.
 */
public enum ValueSetMethods {
    /**
     * Unify all value set methods to {@code getSetOfAllowedValuesFor<Attribute>}.
     * <p>
     * This is the "new" way from Faktor-IPS 22.6 on, resulting in a single method name independent
     * of the {@link ValueSetType} that can easily be called and/or overwritten.
     */
    Unified,
    /**
     * Keep the method names {@code getSetOfAllowedValuesFor<Attribute>},
     * {@code getAllowedValuesFor<Attribute>} or {@code getRangeFor<Attribute>} depending on the
     * {@link ValueSetType}.
     * <p>
     * This is the "old" way and results in clear visibility of the {@link ValueSetType} from the
     * method name but one needs to know the type to find the method name when retrieving the value
     * set in custom code.
     */
    ByValueSetType,
    /**
     * Do both, but set the old methods deprecated, and let the new methods delegate to the old
     * ones.
     * <p>
     * This setting should be used in a migration scenario, to identify
     * overwritten/"{@code @generated NOT}" methods and move their code from the old to the new
     * method before switching to {@link ValueSetMethods#Unified}.
     */
    Both;

    /**
     * Returns whether this {@link ValueSetMethods} setting is {@link #Unified}.
     */
    public boolean isUnified() {
        return this == Unified;
    }

    /**
     * Returns whether this {@link ValueSetMethods} setting is {@link #ByValueSetType}.
     */
    public boolean isByValueSetType() {
        return this == ByValueSetType;
    }

    /**
     * Returns whether this {@link ValueSetMethods} setting is {@link #Both}.
     */
    public boolean isBoth() {
        return this == Both;
    }

    /**
     * Returns whether unified methods should be compiled (either {@link #Unified} or
     * {@link #Both}).
     */
    public boolean isCompileUnifiedMethods() {
        return this == Unified || this == Both;
    }

    /**
     * Returns whether differently named methods should be compiled (either {@link #ByValueSetType}
     * or {@link #Both}).
     */
    public boolean isCompileDifferentMethodsByValueSetType() {
        return this == ByValueSetType || this == Both;
    }
}
