/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.internal.model.ipsproject;

import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;

/**
 * {@link AbstractSearch} is designed for processing different {@link IpsObjectPathEntry}s.
 */
public abstract class AbstractSearch {

    public abstract SearchEnum processEntry(IIpsObjectPathEntry entry);

    public enum SearchEnum {
        STOP_SEARCH,
        CONTINUE_SEARCH;
    }
}
