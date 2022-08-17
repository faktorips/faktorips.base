/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.builder.flidentifier;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.faktorips.devtools.model.internal.builder.flidentifier.ast.IdentifierNodeType;
import org.junit.Test;

public class IdentifierProposalTest {

    @Test
    public void testCompareSameIdentifierNodeTypes() {
        IdentifierProposal enumClass1 = new IdentifierProposal("ZZenumClass", "ZZenumClass", "description", "",
                IdentifierNodeType.ENUM_CLASS);
        IdentifierProposal enumClass2 = new IdentifierProposal("10enumClass", "10enumClass", "description", "",
                IdentifierNodeType.ENUM_CLASS);
        IdentifierProposal enumClass3 = new IdentifierProposal("AAenumClass", "AAenumClass", "description", "",
                IdentifierNodeType.ENUM_CLASS);

        List<IdentifierProposal> list = new ArrayList<>();
        list.add(enumClass1);
        list.add(enumClass2);
        list.add(enumClass3);

        Collections.sort(list);

        assertEquals(list.get(0), enumClass2);
        assertEquals(list.get(1), enumClass3);
        assertEquals(list.get(2), enumClass1);

    }

    @Test
    public void testCompareToWholeList() {
        List<IdentifierProposal> sortedList = initList();
        List<IdentifierProposal> unsortedList = new ArrayList<>();
        unsortedList.add(sortedList.get(2));
        unsortedList.add(sortedList.get(6));
        unsortedList.add(sortedList.get(1));
        unsortedList.add(sortedList.get(3));
        unsortedList.add(sortedList.get(5));
        unsortedList.add(sortedList.get(0));
        unsortedList.add(sortedList.get(7));
        unsortedList.add(sortedList.get(4));

        assertThat(unsortedList, not(equalTo(sortedList)));

        Collections.sort(unsortedList);

        assertEquals(unsortedList, sortedList);
    }

    public List<IdentifierProposal> initList() {
        IdentifierProposal association1 = new IdentifierProposal("Xasso", "Xasso", "description", "",
                IdentifierNodeType.ASSOCIATION);
        IdentifierProposal association2 = new IdentifierProposal("Yasso", "Yasso", "description", "",
                IdentifierNodeType.ASSOCIATION);

        IdentifierProposal attribute1 = new IdentifierProposal("Gattr", "Gattr", "description", "",
                IdentifierNodeType.ATTRIBUTE);
        IdentifierProposal attribute2 = new IdentifierProposal("Hattr", "Hattr", "description", "",
                IdentifierNodeType.ATTRIBUTE);

        IdentifierProposal param1 = new IdentifierProposal("Iparam", "Iparam", "description", "",
                IdentifierNodeType.PARAMETER);
        IdentifierProposal param2 = new IdentifierProposal("Jparam", "Jparam", "description", "",
                IdentifierNodeType.PARAMETER);

        IdentifierProposal enumValue1 = new IdentifierProposal("VenumValue", "VenumValue", "description", "",
                IdentifierNodeType.ENUM_VALUE);
        IdentifierProposal enumValue2 = new IdentifierProposal("WenumValue", "WenumValue", "description", "",
                IdentifierNodeType.ENUM_VALUE);

        List<IdentifierProposal> sortedList = new ArrayList<>();
        sortedList.add(param1);
        sortedList.add(param2);
        sortedList.add(attribute1);
        sortedList.add(attribute2);
        sortedList.add(association1);
        sortedList.add(association2);
        sortedList.add(enumValue1);
        sortedList.add(enumValue2);
        return sortedList;
    }
}
