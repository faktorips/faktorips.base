/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.abstraction;

import org.faktorips.devtools.model.abstraction.AWorkspace.PlainJavaWorkspace;
import org.faktorips.devtools.model.abstraction.Abstractions.PlainJavaImplementation;

public class PlainJavaImplementationAccess {

    private PlainJavaImplementationAccess() {
        // test util
    }

    public static void setWorkspace(PlainJavaWorkspace workspace) {
        PlainJavaImplementation.get().setWorkspace(workspace);
    }
}
