/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject.refactor;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveIpsObjectProcessor;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.MoveRenameIpsObjectHelper;
import org.faktorips.devtools.core.internal.model.ipsobject.refactor.RenameIpsObjectProcessor;

/**
 * Parent interface for {@link MoveIpsObjectProcessor} and {@link RenameIpsObjectProcessor}, which
 * share a common implementation in {@link MoveRenameIpsObjectHelper} because they both inherit from
 * different processors.
 * 
 * @author dschwering
 */
public interface IIpsMoveRenameIpsObjectProcessor {

    /**
     * Returns the Java elements generated for the refactoring's target.
     */
    List<IJavaElement> getTargetJavaElements();

}
