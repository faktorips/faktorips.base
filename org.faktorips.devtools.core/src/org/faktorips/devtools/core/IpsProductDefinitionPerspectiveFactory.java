/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
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

    public final static String PRODUCTDEFINITIONPERSPECTIVE_ID = "org.faktorips.devtools.core.productDefinitionPerspective"; //$NON-NLS-1$

    @Override
    public void createInitialLayout(IPageLayout layout) {
        // nothing to do
    }

}
