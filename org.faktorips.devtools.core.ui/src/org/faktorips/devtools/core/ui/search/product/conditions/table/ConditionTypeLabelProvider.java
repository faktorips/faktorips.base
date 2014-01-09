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

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.faktorips.devtools.core.ui.search.product.conditions.types.IConditionType;

/**
 * This is the {@link CellLabelProvider} for the column of the condition types.
 * 
 * @author dicker
 */
final class ConditionTypeLabelProvider extends ColumnLabelProvider {
    @Override
    public String getText(Object element) {
        IConditionType conditionType = ((ProductSearchConditionPresentationModel)element).getConditionType();
        if (conditionType == null) {
            return StringUtils.EMPTY;
        }
        return conditionType.getName();
    }
}