/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.modeldescription;

import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;
import org.faktorips.devtools.model.exception.CoreRuntimeException;

/**
 * Mark a class for providing input to {@link ModelDescriptionView}.
 * 
 * @see PageBookView
 * 
 * @author blum
 * 
 */
public interface IModelDescriptionSupport {

    /**
     * Create a Page for {@link ModelDescriptionView}.
     * 
     * @return IPage new Page.
     */
    public IPage createModelDescriptionPage() throws CoreRuntimeException;
}
