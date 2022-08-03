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
 * There are different kinds of build jobs:
 * <ul>
 * <li>A {@link #FULL full build} builds every applicable item in a project.</li>
 * <li>An {@link #INCREMENTAL incremental build} builds only items affected by changes since a
 * previous build.</li>
 * <li>A {@link #CLEAN clean build} discards previous build results and then performs a
 * {@link #FULL}.</li>
 * <li>A {@link #AUTO auto build} operates like an incremental build, but is triggered automatically
 * whenever a resources changes</li>
 * </ul>
 */
public enum ABuildKind {
    FULL,
    INCREMENTAL,
    CLEAN,
    AUTO;
}
