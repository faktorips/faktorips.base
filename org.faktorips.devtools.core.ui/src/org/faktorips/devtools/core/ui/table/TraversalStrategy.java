/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.table;

import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.TraverseListener;

/**
 * Interface for all classes that support navigation/Traversal in {@link IpsCellEditor}s.
 * 
 * @author Stefan Widmaier
 */
public interface TraversalStrategy extends TraverseListener, KeyListener, FocusListener {

    // No methods as of now

}
