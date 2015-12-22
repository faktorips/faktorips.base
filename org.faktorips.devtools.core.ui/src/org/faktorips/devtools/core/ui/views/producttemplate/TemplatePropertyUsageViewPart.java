/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.ui.views.producttemplate;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class TemplatePropertyUsageViewPart extends ViewPart {

    private TemplatePropertyUsageView productTemplateDiffView;

    public TemplatePropertyUsageViewPart() {
    }

    @Override
    public void createPartControl(Composite parent) {
        productTemplateDiffView = new TemplatePropertyUsageView(parent);
    }

    @Override
    public void setFocus() {
        productTemplateDiffView.setFocus();
    }

}
