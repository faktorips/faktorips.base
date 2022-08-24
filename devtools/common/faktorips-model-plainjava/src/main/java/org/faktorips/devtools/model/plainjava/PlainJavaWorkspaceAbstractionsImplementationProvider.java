/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.model.plainjava;

import com.google.auto.service.AutoService;

import org.faktorips.devtools.model.abstractions.AWorkspaceAbstractionsImplementationProvider;
import org.faktorips.devtools.model.abstractions.WorkspaceAbstractions.AWorkspaceAbstractionsImplementation;
import org.faktorips.devtools.model.plainjava.internal.PlainJavaWorkspaceAbstractionsImplementation;

@AutoService(AWorkspaceAbstractionsImplementationProvider.class)
public class PlainJavaWorkspaceAbstractionsImplementationProvider
        implements AWorkspaceAbstractionsImplementationProvider {

    @Override
    public AWorkspaceAbstractionsImplementation getImplementation() {
        return PlainJavaWorkspaceAbstractionsImplementation.INSTANCE;
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
