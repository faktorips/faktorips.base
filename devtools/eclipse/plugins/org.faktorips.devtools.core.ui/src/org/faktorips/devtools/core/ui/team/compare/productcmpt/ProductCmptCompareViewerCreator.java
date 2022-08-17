/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class ProductCmptCompareViewerCreator implements IViewerCreator {

    public ProductCmptCompareViewerCreator() {
        super();
    }

    @Override
    public Viewer createViewer(Composite parent, CompareConfiguration config) {
        return new ProductCmptCompareViewer(parent, config);
    }

}
