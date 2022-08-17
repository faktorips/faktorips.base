/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.test;

/**
 * Class to execute and test formulas on product cmpts.
 * 
 * @author Joerg Ortmann
 */
public abstract class IpsFormulaTestCase extends IpsTestCaseBase {

    public IpsFormulaTestCase(String qName) {
        super(qName);
    }

    @Override
    public int countTestCases() {
        return 1;
    }

}
