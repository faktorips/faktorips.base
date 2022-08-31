/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.productcmpt;

import static org.faktorips.testsupport.IpsMatchers.hasInvalidObject;
import static org.faktorips.testsupport.IpsMatchers.hasMessageCode;
import static org.faktorips.testsupport.IpsMatchers.isEmpty;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.faktorips.devtools.model.productcmpt.Cardinality;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CardinalityTest {

    @Mock
    private IProductCmptLink link;

    @Test
    public void testValidate_Ok() throws Exception {
        assertThat(new Cardinality(0, 1, 0).validate(link), isEmpty());
        assertThat(new Cardinality(1, 1, 1).validate(link), isEmpty());
        assertThat(new Cardinality(0, Cardinality.CARDINALITY_MANY, 0).validate(link), isEmpty());
        assertThat(new Cardinality(0, Cardinality.CARDINALITY_MANY, 1).validate(link), isEmpty());
        assertThat(new Cardinality(1, Cardinality.CARDINALITY_MANY, 1).validate(link), isEmpty());
        assertThat(new Cardinality(100, Cardinality.CARDINALITY_MANY, 500).validate(link), isEmpty());
    }

    @Test
    public void testValidate_Min_Bewlow_Zero() throws Exception {
        Cardinality cardinality = new Cardinality(-1, 1, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MIN_CARDINALITY_IS_LESS_THAN_0));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Max_Bewlow_1() throws Exception {
        Cardinality cardinality = new Cardinality(0, 0, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_1));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Max_Lt_Min() throws Exception {
        Cardinality cardinality = new Cardinality(2, 1, 2);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_MAX_CARDINALITY_IS_LESS_THAN_MIN));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Lt_Min() throws Exception {
        Cardinality cardinality = new Cardinality(1, 5, 0);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Gt_Max() throws Exception {
        Cardinality cardinality = new Cardinality(1, 5, 6);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testValidate_Default_Too_High() throws Exception {
        Cardinality cardinality = new Cardinality(1, Cardinality.CARDINALITY_MANY, Cardinality.CARDINALITY_MANY);

        assertThat(cardinality.validate(link), hasMessageCode(Cardinality.MSGCODE_DEFAULT_CARDINALITY_OUT_OF_RANGE));
        assertThat(cardinality.validate(link).size(), is(1));
        assertThat(cardinality.validate(link).getMessage(0), hasInvalidObject(link));
    }

    @Test
    public void testFormat() {
        assertThat(new Cardinality(0, 1, 0).format(), is("[0..1, 0]"));
    }

    @Test
    public void testCompareTo_Null() {
        assertThat(new Cardinality(0, 1, 0).compareTo(null), is(-1));
    }

    @Test
    public void testCompareTo_EqualsCardinalities() {
        Cardinality c1 = new Cardinality(0, 1, 0);
        Cardinality c2 = new Cardinality(0, 1, 0);

        assertThat(c1.compareTo(c1), is(0));
        assertThat(c1.compareTo(c2), is(0));
        assertThat(c2.compareTo(c1), is(0));
    }

    @Test
    public void testCompareTo_ComparesMin() {
        Cardinality c1 = new Cardinality(0, 9, 9);
        Cardinality c2 = new Cardinality(1, 1, 0);
        assertThat(c1.compareTo(c2), is(-1));
        assertThat(c2.compareTo(c1), is(1));
    }

    @Test
    public void testCompareTo_ComparesMax() {
        Cardinality c1 = new Cardinality(1, 1, 9);
        Cardinality c2 = new Cardinality(1, 2, 0);
        assertThat(c1.compareTo(c2), is(-1));
        assertThat(c2.compareTo(c1), is(1));
    }

    @Test
    public void testCompareTo_ComparesDefault() {
        Cardinality c1 = new Cardinality(1, 1, 0);
        Cardinality c2 = new Cardinality(1, 1, 1);
        assertThat(c1.compareTo(c2), is(-1));
        assertThat(c2.compareTo(c1), is(1));
    }

    @Test
    public void testCompareTo_UndefinedCardinality() {
        Cardinality c0 = new Cardinality(0, 0, 0);
        Cardinality c1 = new Cardinality(1, 1, 1);

        assertThat(c0.compareTo(Cardinality.UNDEFINED), is(-1));
        assertThat(Cardinality.UNDEFINED.compareTo(c0), is(1));

        assertThat(c1.compareTo(Cardinality.UNDEFINED), is(-1));
        assertThat(Cardinality.UNDEFINED.compareTo(c1), is(1));

        assertThat(Cardinality.UNDEFINED.compareTo(Cardinality.UNDEFINED), is(0));
        assertThat(Cardinality.UNDEFINED.compareTo(null), is(-1));
    }

    @Test
    public void testUndefinedCardinalityGetters() {
        assertThat(Cardinality.UNDEFINED.getMin(), is(0));
        assertThat(Cardinality.UNDEFINED.getMax(), is(0));
        assertThat(Cardinality.UNDEFINED.getDefault(), is(0));
    }

    @Test
    public void testUndefinedCardinalityValidation() {
        assertThat(Cardinality.UNDEFINED.validate(link), isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUndefinedCardinalityWithCreatesNewCardinality() {
        assertThat((Class<Cardinality>)Cardinality.UNDEFINED.withMin(1).getClass(),
                is(sameInstance(Cardinality.class)));
    }

}
