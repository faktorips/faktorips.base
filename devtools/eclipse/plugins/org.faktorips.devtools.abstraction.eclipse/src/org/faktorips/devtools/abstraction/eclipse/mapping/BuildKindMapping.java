/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.abstraction.eclipse.mapping;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.faktorips.devtools.abstraction.ABuildKind;

public class BuildKindMapping {

    private BuildKindMapping() {
        // util
    }

    /**
     * Returns the {@link IncrementalProjectBuilder IncrementalProjectBuilder-Constant} for the
     * given {@link ABuildKind ABuildKind-Enum}.
     *
     * @param kind the build kind used in our abstractions
     * @return the int constant used in eclipse
     */
    public static int buildKind(ABuildKind kind) {
        switch (kind) {
            case FULL:
                return IncrementalProjectBuilder.FULL_BUILD;
            case INCREMENTAL:
                return IncrementalProjectBuilder.INCREMENTAL_BUILD;
            case CLEAN:
                return IncrementalProjectBuilder.CLEAN_BUILD;
            case AUTO:
                return IncrementalProjectBuilder.AUTO_BUILD;
            default:
                return 0;
        }
    }

    /**
     * Returns the {@link ABuildKind ABuildKind-Enum} for a given {@link IncrementalProjectBuilder
     * IncrementalProjectBuilder-Constant}.
     * 
     * @param kind the int constant used in eclipse
     * @return the build kind used in our abstractions
     */
    public static ABuildKind buildKind(int kind) {
        switch (kind) {
            case IncrementalProjectBuilder.INCREMENTAL_BUILD:
                return ABuildKind.INCREMENTAL;
            case IncrementalProjectBuilder.CLEAN_BUILD:
                return ABuildKind.CLEAN;
            case IncrementalProjectBuilder.AUTO_BUILD:
                return ABuildKind.AUTO;
            case IncrementalProjectBuilder.FULL_BUILD:
            default:
                return ABuildKind.FULL;
        }
    }

}
