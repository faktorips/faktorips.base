/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ICondition;

final class ConditionTypeLabelProvider extends ColumnLabelProvider {
    @Override
    public String getText(Object element) {
        ICondition condition = ((ProductSearchConditionPresentationModel)element).getCondition();
        if (condition == null) {
            return StringUtils.EMPTY;
        }
        return condition.getName();
    }
}