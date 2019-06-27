/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.internal;

import java.util.Observable;
import java.util.Observer;

/**
 * This is just another name for the {@link Observer} interface to mark a listener for the
 * {@link DeferredStructuredContentProvider}. We use the benefit of the thread safe implementation
 * of {@link Observer} and {@link Observable}.
 * 
 * @author Cornelius Dirmeier
 */
public interface ICollectorFinishedListener extends Observer {

    // at the moment this is only a wrapper for observer to give a proper name
    // maybe it becomes a real listener once

}
