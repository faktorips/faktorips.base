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

import org.eclipse.core.runtime.Platform;
import org.faktorips.devtools.abstraction.AImplementationProvider;
import org.faktorips.devtools.abstraction.Abstractions.AImplementation;

public class EclipseImplementationProvider implements AImplementationProvider {

    @Override
    public AImplementation get() {
        return EclipseImplementation.get();
    }

    @Override
    public boolean canRun() {
        return Platform.isRunning();
    }

    @Override
    public int getPriority() {
        return 1;
    }

}
