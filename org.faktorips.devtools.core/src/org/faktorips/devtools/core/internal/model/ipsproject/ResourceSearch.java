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
 * An implementation of {@link AbstractSearch} to determine if specific {@link IIpsObjectPathEntry}s
 * contains specific resources.
 */
public class ResourceSearch extends AbstractSearch {

    private IIpsObjectPathEntry resource;

    private final String path;

    private boolean containsResource;

    public ResourceSearch(String path) {
        this.path = path;
    }

    @Override
    public SearchState processEntry(IIpsObjectPathEntry entry) {
        if (!(isProjectRefEntry(entry)) || isContainerEntry(entry)) {
            if (entry.containsResource(path)) {
                resource = entry;
                containsResource = true;
                return SearchState.STOP_SEARCH;
            }
        }
        return SearchState.CONTINUE_SEARCH;

    }

    public IIpsObjectPathEntry getResource() {
        return resource;
    }

    public boolean containsResource() {
        return containsResource;
    }
}
