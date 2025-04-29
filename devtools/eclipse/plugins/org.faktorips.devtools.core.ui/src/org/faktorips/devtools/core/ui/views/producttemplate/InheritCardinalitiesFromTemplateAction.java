/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.producttemplate;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpt.template.TemplateValueStatus;

public class InheritCardinalitiesFromTemplateAction extends Action {

    private final List<IProductCmptLink> selection;

    public InheritCardinalitiesFromTemplateAction(List<IProductCmptLink> templateLinks) {
        super(Messages.InheritCardinalitiesFromTemplateAction_label,
                IpsUIPlugin.getImageHandling().getSharedImageDescriptor("templateInherited16.png", true));
        selection = templateLinks;
    }

    @Override
    public String getToolTipText() {
        return Messages.InheritCardinalitiesFromTemplateAction_tooltip;
    }

    @Override
    public void run() {
        selection.stream()
                .filter(this::hasTemplate)
                .forEach(link -> link.setTemplateValueStatus(TemplateValueStatus.INHERITED));
    }

    private boolean hasTemplate(IProductCmptLink link) {
        return link != null && link.findTemplateProperty(link.getIpsProject()) != null;
    }
}
