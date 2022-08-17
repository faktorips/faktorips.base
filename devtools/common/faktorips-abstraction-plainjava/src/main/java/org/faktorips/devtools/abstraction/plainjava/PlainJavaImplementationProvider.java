/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.abstraction.plainjava;

import com.google.auto.service.AutoService;

import org.faktorips.devtools.abstraction.AImplementationProvider;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;
import org.faktorips.devtools.abstraction.plainjava.internal.PlainJavaImplementation;

@AutoService(AImplementationProvider.class)
public class PlainJavaImplementationProvider implements AImplementationProvider {

    @Override
    public AImplementation get() {
        return PlainJavaImplementation.INSTANCE;
    }

    @Override
    public boolean canRun() {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
