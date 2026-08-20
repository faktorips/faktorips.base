/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import static org.faktorips.abstracttest.MockUtil.createMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoSession;

public class AbstractExcelImportOperationTest {


    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractExcelImportOperation operation;

    private MockitoSession mockito;

    @BeforeEach
    void setUp() {
        mockito = createMocks(this);
    }

    @AfterEach
    void tearDown() {
        mockito.finishMocking();
    }

    @Test
    public void testRoundNumericCellValue_SmallPositive() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(2.9347826125000003);

        assertEquals(0, BigDecimal.valueOf(2.9347826125).compareTo(value), value.toString());
    }

    @Test
    public void testRoundNumericCellValue_BigPositive() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(29347826125000004.0);

        assertEquals(0, BigDecimal.valueOf(29347826125000000.0).compareTo(value), value.toString());
    }

    @Test
    public void testRoundNumericCellValue_BigNegative() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(-29347826125000004.0);

        assertEquals(0, BigDecimal.valueOf(-29347826125000000.0).compareTo(value), value.toString());
    }

    @Test
    public void testRoundNumericCellValue_SmallNegative() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(-0.000010000000000000002);

        assertEquals(0, BigDecimal.valueOf(-0.000010000000000000000).compareTo(value), value.toString());
    }

    @Test
    public void testRoundNumericCellValue_closeToZero() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(-0.000000000000000000001);

        assertEquals(0, BigDecimal.valueOf(-0.000000000000000000001).compareTo(value), value.toString());
    }

    @Test
    public void testRoundNumericCellValue_zero() throws Exception {
        BigDecimal value = operation.roundNumericCellValue(-0.00000000000000000000);

        assertEquals(0, BigDecimal.valueOf(-0.0).compareTo(value), value.toString());
    }
}
