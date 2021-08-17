/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.function.BooleanSupplier;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;

/**
 * An {@link ActionContributionItem} that can switch it's {@link #isVisible() visibility}.
 */
public class DynamicallyVisibleActionContributionItem extends ActionContributionItem {

    private BooleanSupplier visibleSupplier;

    public DynamicallyVisibleActionContributionItem(IAction action, BooleanSupplier visibleSupplier) {
        super(action);
        this.visibleSupplier = visibleSupplier;
        update();
    }

    @Override
    public void update(String propertyName) {
        setVisible(visibleSupplier.getAsBoolean());
        super.update(propertyName);
    }

}
