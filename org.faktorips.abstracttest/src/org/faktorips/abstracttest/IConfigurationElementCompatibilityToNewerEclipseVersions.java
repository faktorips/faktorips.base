/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.abstracttest;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * This is a hack because newer eclipse versions add {@link #getHandleId()} to the API of
 * {@link IConfigurationElement} and we want our code to be compatible with old and new target
 * platforms.
 */
public interface IConfigurationElementCompatibilityToNewerEclipseVersions {
    int getHandleId();
}