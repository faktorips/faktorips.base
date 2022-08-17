/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Perspective for ProductDefinition.
 * 
 * @author Thorsten Guenther
 */
public class IpsProductDefinitionPerspectiveFactory implements IPerspectiveFactory {

    public static final String PRODUCTDEFINITIONPERSPECTIVE_ID = "org.faktorips.devtools.core.productDefinitionPerspective"; //$NON-NLS-1$

    @Override
    public void createInitialLayout(IPageLayout layout) {
        // nothing to do
    }

}
