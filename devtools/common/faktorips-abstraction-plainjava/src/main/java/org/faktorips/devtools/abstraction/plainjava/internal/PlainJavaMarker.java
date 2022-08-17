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

import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AWrapper;

public class PlainJavaMarker extends AWrapper<PlainJavaMarkerImpl> implements AMarker {

    public PlainJavaMarker(PlainJavaResource resource, String type) {
        this(new PlainJavaMarkerImpl(resource, type));
    }

    public PlainJavaMarker(PlainJavaMarkerImpl original) {
        super(original);
    }

    private PlainJavaMarkerImpl marker() {
        return unwrap();
    }

    @Override
    public void delete() {
        marker().getResource().deleteMarker(this);
    }

    @Override
    public Object getAttribute(String attributeName) {
        return marker().getAttribute(attributeName);
    }

    @Override
    public String getAttribute(String attributeName, String defaultValue) {
        return marker().getAttribute(attributeName, defaultValue);
    }

    @Override
    public int getAttribute(String attributeName, int defaultValue) {
        return marker().getAttribute(attributeName, defaultValue);
    }

    @Override
    public boolean getAttribute(String attributeName, boolean defaultValue) {
        return marker().getAttribute(attributeName, defaultValue);
    }

    @Override
    public void setAttribute(String attributeName, Object value) {
        marker().setAttribute(attributeName, value);
    }

    @Override
    public void setAttributes(String[] attributeNames, Object[] values) {
        marker().setAttributes(attributeNames, values);
    }

    @Override
    public boolean isError() {
        return marker().isError();
    }

    @Override
    public String getType() {
        return marker().getType();
    }

    @Override
    public long getId() {
        return marker().getId();
    }

}
