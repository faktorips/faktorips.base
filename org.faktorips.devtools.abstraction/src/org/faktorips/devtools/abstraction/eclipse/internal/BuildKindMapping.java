/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse.internal;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.faktorips.devtools.abstraction.ABuildKind;

public class BuildKindMapping {

    private BuildKindMapping() {
        // util
    }

    /**
     * Returns the {@link IncrementalProjectBuilder}-constant for the given build kind used in
     * Eclipse builds.
     */
    public static int forEclipse(ABuildKind kind) {
        switch (kind) {
            case FULL_BUILD:
                return IncrementalProjectBuilder.FULL_BUILD;
            case INCREMENTAL_BUILD:
                return IncrementalProjectBuilder.INCREMENTAL_BUILD;
            case CLEAN_BUILD:
                return IncrementalProjectBuilder.CLEAN_BUILD;

            default:
                return 0;
        }
    }
}
