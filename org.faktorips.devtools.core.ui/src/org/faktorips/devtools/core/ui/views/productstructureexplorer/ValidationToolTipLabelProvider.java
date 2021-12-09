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

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.faktorips.devtools.model.Validatable;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.Severity;

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
        try {
            if (element instanceof IProductCmptReference) {
                IProductCmpt productCmpt = ((IProductCmptReference)element).getProductCmpt();
                return validateAndReturnErrorMessages(productCmpt);
            } else if (element instanceof IProductCmptStructureTblUsageReference) {
                ITableContentUsage tableContentUsage = ((IProductCmptStructureTblUsageReference)element)
                        .getTableContentUsage();
                return validateAndReturnErrorMessages(tableContentUsage);
            } else {
                return super.getToolTipText(element);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    private String validateAndReturnErrorMessages(Validatable validatable) throws CoreRuntimeException {
        MessageList msgList = validatable.validate(validatable.getIpsProject());
        String text = msgList.getMessagesBySeverity(Severity.ERROR).getText();
        if (!StringUtils.isEmpty(text)) {
            return text;
        }
        return super.getToolTipText(validatable);
    }

}
