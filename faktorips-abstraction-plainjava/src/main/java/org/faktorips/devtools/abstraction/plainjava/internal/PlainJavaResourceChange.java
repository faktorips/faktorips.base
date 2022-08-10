/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava.internal;

public class PlainJavaResourceChange {

    private final PlainJavaResource changedResource;
    private final Type type;

    PlainJavaResourceChange(PlainJavaResource changedResource, Type type) {
        this.changedResource = changedResource;
        this.type = type;
    }

    public PlainJavaResource getChangedResource() {
        return changedResource;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADDED,
        REMOVED,
        CONTENT_CHANGED
    }

}
