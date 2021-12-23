/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.eclipse;

import static org.faktorips.devtools.abstraction.Wrappers.get;
import static org.faktorips.devtools.abstraction.Wrappers.run;

import org.eclipse.core.resources.IMarker;
import org.faktorips.devtools.abstraction.AMarker;
import org.faktorips.devtools.abstraction.AWrapper;

public class AEclipseMarker extends AWrapper<IMarker> implements AMarker {

    AEclipseMarker(IMarker marker) {
        super(marker);
    }

    IMarker marker() {
        return unwrap();
    }

    @Override
    public void delete() {
        run(marker()::delete);
    }

    @Override
    public Object getAttribute(String attributeName) {
        return get(() -> marker().getAttribute(attributeName));
    }

    @Override
    public String getAttribute(String attributeName, String defaultValue) {
        return get(() -> marker().getAttribute(attributeName, defaultValue));
    }

    @Override
    public int getAttribute(String attributeName, int defaultValue) {
        return get(() -> marker().getAttribute(attributeName, defaultValue));
    }

    @Override
    public boolean getAttribute(String attributeName, boolean defaultValue) {
        return get(() -> marker().getAttribute(attributeName, defaultValue));
    }

    @Override
    public void setAttribute(String attributeName, Object value) {
        run(() -> marker().setAttribute(attributeName, value));
    }

    @Override
    public void setAttributes(String[] attributeNames, Object[] values) {
        run(() -> marker().setAttributes(attributeNames, values));
    }

    @Override
    public boolean isError() {
        return marker().getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR) == IMarker.SEVERITY_ERROR;
    }

    @Override
    public String getType() {
        return get(marker()::getType);
    }

    @Override
    public long getId() {
        return marker().getId();
    }

}