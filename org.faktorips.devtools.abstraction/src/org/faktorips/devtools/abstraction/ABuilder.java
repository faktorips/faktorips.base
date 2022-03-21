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

/**
 * A builder reads source files and creates output files. It is associated with {@link AProject a
 * project}.
 */
public interface ABuilder {

    /**
     * Returns the project this builder is building.
     */
    AProject getProject();

    /**
     * Returns the delta between this builder's last build of its project and that project's current
     * state.
     */
    AResourceDelta getDelta();

}
