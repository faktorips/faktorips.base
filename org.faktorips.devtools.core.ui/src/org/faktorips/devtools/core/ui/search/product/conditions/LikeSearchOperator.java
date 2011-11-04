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

package org.faktorips.devtools.core.ui.search.product.conditions;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.ui.search.matcher.WildcardMatcher;

public class LikeSearchOperator extends AbstractSearchOperator<LikeSearchOperatorType> {

    private final WildcardMatcher matcher;

    public LikeSearchOperator(ValueDatatype valueDatatype, LikeSearchOperatorType searchOperatorType,
            IOperandProvider operandProvider, String argument) {
        super(valueDatatype, searchOperatorType, operandProvider, argument);
        matcher = new WildcardMatcher(argument);
    }

    @Override
    protected boolean check(Object searchOperand, IProductCmptGeneration productCmptGeneration) {
        if (searchOperand instanceof String) {
            return checkInternal((String)searchOperand);
        }
        return false;
    }

    private boolean checkInternal(String searchOperand) {
        return matcher.isMatching(searchOperand) == getSearchOperatorType().isNegation();
    }

}
