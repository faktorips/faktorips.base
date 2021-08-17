/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject.search;

import java.io.InputStream;

import org.faktorips.devtools.model.ipsproject.IIpsObjectPathEntry;

/**
 * By using a specific path a {@link ResourceSearch} can determine if the according resources can be
 * found and it can return the stream of the found {@link IIpsObjectPathEntry}.
 */

public class ResourceSearch extends AbstractSearch {

    private IIpsObjectPathEntry resource;

    private final String path;

    private boolean containsResource;

    public ResourceSearch(String path) {
        this.path = path;
    }

    @Override
    public void processEntry(IIpsObjectPathEntry entry) {
        if (entry.containsResource(path)) {
            resource = entry;
            containsResource = true;
            setStopSearch();
        }
    }

    public InputStream getResourceAsStream() {
        if (resource != null) {
            return resource.getResourceAsStream(path);
        } else {
            return null;
        }
    }

    public boolean containsResource() {
        return containsResource;
    }
}
