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

import org.faktorips.devtools.abstraction.ABuilder;
import org.faktorips.devtools.abstraction.AResourceDelta;

class PlainJavaBuilder implements ABuilder {

    private final PlainJavaProject project;

    private PlainJavaBuilder(PlainJavaProject project) {
        this.project = project;
    }

    @Override
    public PlainJavaProject getProject() {
        return project;
    }

    @Override
    public AResourceDelta getDelta() {
        // TODO erstmal genügt es, hier null zurückzugeben, das forciert einen full build
        return null;
    }
}