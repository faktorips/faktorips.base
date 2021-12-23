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
 * <li>A {@link #FULL_BUILD full build} builds every applicable item in a project.</li>
 * <li>An {@link #INCREMENTAL_BUILD incremental build} builds only items affected by changes since a
 * previous build.</li>
 * <li>A {@link #CLEAN_BUILD clean build} discards previous build results and then performs a
 * {@link #FULL_BUILD}.</li>
 * </ul>
 */
public enum ABuildKind {
    FULL_BUILD,
    INCREMENTAL_BUILD,
    CLEAN_BUILD;
}