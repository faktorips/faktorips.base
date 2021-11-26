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

/**
 * The possible values for the unifyValueSetMethods setting for the
 * {@link IIpsArtefactBuilderSetConfig Faktor-IPS compiler options}.
 * 
 */
public enum UnifyValueSetMethods {
    /**
     * Unify all value set methods to getSetOfAllowedValuesFor
     */
    NewMethods,
    /**
     * Keep the method names getSetOfAllowedValuesFor, getAllowedValuesFor or getRangeFor
     */
    OldMethods,
    /**
     * Do both, but set the old methods deprecated, and let the new methods delegate to the old ones
     */
    Both;

}
