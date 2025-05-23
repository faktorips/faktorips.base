/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * Extend the {@link DecoratingStyledCellLabelProvider} to add the possibility to get a ToolTip of
 * the actual element. The ToolTip contains the validation Messages of the {@link Validatable}
 * element.
 *
 * @author frank
 * @since 3.10.0
 */
public class ValidationToolTipLabelProvider extends DecoratingStyledCellLabelProvider {

    public ValidationToolTipLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator,
            IDecorationContext decorationContext) {
        super(labelProvider, decorator, decorationContext);
    }

    @Override
    public String getToolTipText(Object element) {
        return switch (element) {
            case IProductCmptReference productCmptReference -> validateAndReturnErrorMessages(
                    productCmptReference.getProductCmpt());
            case IProductCmptStructureTblUsageReference productCmptStructureTblUsageReference -> validateAndReturnErrorMessages(
                    productCmptStructureTblUsageReference.getTableContentUsage());
            default -> super.getToolTipText(element);
        };
    }

    private String validateAndReturnErrorMessages(Validatable validatable) {
        MessageList msgList = validatable.validate(validatable.getIpsProject());
        String text = msgList.getMessagesBySeverity(Severity.ERROR).getText();
        if (!IpsStringUtils.isEmpty(text)) {
            return text;
        }
        return super.getToolTipText(validatable);
    }

}
