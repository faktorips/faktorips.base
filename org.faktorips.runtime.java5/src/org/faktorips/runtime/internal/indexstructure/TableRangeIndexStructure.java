/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal.indexstructure;

import java.util.ArrayList;
import java.util.Date;

public class TableRangeIndexStructure<Row, Index0> {

    private HashMapStructure<Index0, TreeStructure<Integer, TreeStructure<Date, ResultStructure<Row>, Row>, Row>, Row> key0Structure;
    private HashMapStructure<Index0, TreeStructure<Date, ResultStructure<Row>, Row>, Row> key0Structure2;
    private HashMapStructure<Index0, ResultStructure<Row>, Row> key0Structure3;
    private TreeStructure<Date, ResultStructure<Row>, Row> key0Structure4;
    private TreeStructure<Date, TreeStructure<Integer, ResultStructure<Row>, Row>, Row> key0Structure5;

    private ArrayList<Row> rows;

    private Index0 currentIndex;

    private Integer currentAge;

    private Date currentGueltig;

    protected void initKeyMaps() {
        key0Structure = HashMapStructure.create();
        for (Row r : rows) {
            TreeStructure<Date, ResultStructure<Row>, Row> gueltigMap0 = TreeStructure.create();
            TreeStructure<Integer, TreeStructure<Date, ResultStructure<Row>, Row>, Row> ageMap0 = TreeStructure
                    .create();
            ResultStructure<Row> result0 = ResultStructure.create(r);
            ageMap0.put(currentAge, gueltigMap0);
            gueltigMap0.put(currentGueltig, result0);
            key0Structure.put(currentIndex, ageMap0);
        }

        // key0Structure.put(currentIndex, )
        //
        // AbstractMapStructure<Date, ResultStructure<Row>, Row> gueltigMap0 = new
        // AbstractMapStructure<Date,
        // TableRangeIndexStructure.ResultStructure<Row>, Row>();
        // AbstractMapStructure<Integer, AbstractMapStructure<Date, ResultStructure<Row>, Row>, Row>
        // ageMap0 = new
        // AbstractMapStructure<Integer, AbstractMapStructure<Date, ResultStructure<Row>, Row>,
        // Row>();
        // key0Structure.put(key, value)
        //
        // Map<TwoColumnKey<Integer>, Map<TwoColumnKey<Date>, TableWithTwoColumnRangesAndKeysRow>>
        // ageMap = getMap(
        // key0MapTemp, new Index0(row.getCompany(), row.getGender()));
        // Map<TwoColumnKey<Date>, TableWithTwoColumnRangesAndKeysRow> gueltigMap = getMap(ageMap,
        // new TwoColumnKey<Integer>(row.getAgeFrom(), row.getAgeTo()));
        // gueltigMap.put(new TwoColumnKey<Date>(row.getGueltigAb(), row.getGueltigBis()), row);
        // key1Map.put(new Index1(row.getArt(), row.getCompany()), row);
        // }
        // key0Map = convert(key0MapTemp, new KeyType[] { KeyType.KEY_IS_TWO_COLUMN_KEY,
        // KeyType.KEY_IS_TWO_COLUMN_KEY },
        // true);
    }

}
