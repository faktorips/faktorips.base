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

package org.faktorips.runtime.internal;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.faktorips.runtime.internal.indexstructure.TwoColumnKey;


public class TableRangeIndexStructure<Row, Index0> {

    private MapStructure<Index0, TreeStructure<Integer, Date, TreeStructure<Date, Object, ResultStructure<Row>, Row>, Row>, Row> key0Structure;

    private ArrayList<Row> rows;

    private Index0 currentIndex;

    private Integer currentAge;

    private Date currentGueltig;

    protected void initKeyMaps() {
        key0Structure = new MapStructure<Index0, TreeStructure<Integer, Date, TreeStructure<Date, Object, ResultStructure<Row>, Row>, Row>, Row>();
        for (Row r : rows) {
            ResultStructure<Row> result0 = new ResultStructure<Row>(r);
            TreeStructure<Date, Object, ResultStructure<Row>, Row> gueltigMap0 = new TreeStructure<Date, Object, TableRangeIndexStructure.ResultStructure<Row>, Row>();
            gueltigMap0.put(currentGueltig, result0);
            TreeStructure<Integer, Date, TreeStructure<Date, Object, ResultStructure<Row>, Row>, Row> ageMap0 = new TreeStructure<Integer, Date, TreeStructure<Date, Object, ResultStructure<Row>, Row>, Row>();
            ageMap0.put(currentAge, gueltigMap0);
            key0Structure.put(currentIndex, ageMap0);
        }

        // key0Structure.put(currentIndex, )
        //
        // TreeStructure<Date, ResultStructure<Row>, Row> gueltigMap0 = new TreeStructure<Date,
        // TableRangeIndexStructure.ResultStructure<Row>, Row>();
        // TreeStructure<Integer, TreeStructure<Date, ResultStructure<Row>, Row>, Row> ageMap0 = new
        // TreeStructure<Integer, TreeStructure<Date, ResultStructure<Row>, Row>, Row>();
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

    public static abstract class Structure<K, R, S extends Structure<K, R, S>> {

        abstract void merge(S otherStructure);

        abstract Structure<?, R, ?> get(K key);

        abstract Set<R> get();

        R getUnique() {
            final Set<R> set = get();
            if (set.size() > 1) {
                throw new RuntimeException();
            } else if (set.isEmpty()) {
                return null;
            }
            return set.iterator().next();
        }

    }

    public static class ResultStructure<R> extends Structure<Object, R, ResultStructure<R>> {

        private final Set<R> resultSet;

        public ResultStructure() {
            resultSet = new HashSet<R>();
        }

        public ResultStructure(Set<R> result) {
            resultSet = result;
        }

        public ResultStructure(R result) {
            this();
            resultSet.add(result);
        }

        @Override
        public Structure<?, R, ResultStructure<R>> get(Object key) {
            return this;
        }

        @Override
        public Set<R> get() {
            return resultSet;
        }

        @Override
        void merge(ResultStructure<R> otherStructure) {
            resultSet.addAll(otherStructure.resultSet);
        }

    }

    public static class MapStructure<K, V extends Structure<?, R, ?>, R> extends Structure<K, R, MapStructure<K, V, R>> {

        private Map<K, V> internalMap;

        public MapStructure() {
            this.internalMap = new HashMap<K, V>();
        }

        void put(K key, V value) {
            internalMap.put(key, value);
        }

        @Override
        public Structure<?, R, ?> get(K key) {
            V result = internalMap.get(key);
            if (result == null) {
                return new ResultStructure<R>();
            }
            return result;
        }

        @Override
        public Set<R> get() {
            HashSet<R> resultSet = new HashSet<R>();
            for (V value : internalMap.values()) {
                Set<R> set = value.get();
                resultSet.addAll(set);
            }
            return resultSet;
        }

        @Override
        void merge(MapStructure<K, V, R> otherStructure) {
            // TODO
        }

    }

    public static class TreeStructure<K, K2, V extends Structure<K2, R, V>, R> extends
            Structure<K, R, TreeStructure<K, K2, V, R>> {

        private TreeMap<K, V> treeMap;

        public TreeStructure() {
            this.treeMap = new TreeMap<K, V>();
        }

        void put(K key, V value) {
            treeMap.put(key, value);
        }

        @Override
        void merge(TreeStructure<K, K2, V, R> otherTree) {
            TreeMap<K, V> otherTreeMap = otherTree.treeMap;
            for (Entry<K, V> entry : otherTreeMap.entrySet()) {
                if (treeMap.containsKey(entry.getKey())) {
                    V myValue = treeMap.get(entry.getKey());
                    V otherValue = entry.getValue();
                    myValue.merge(otherValue);
                } else {
                    treeMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        public Structure<?, R, ?> get(K key) {
            V result = treeMap.get(key);
            // TODO tree traversal
            if (result == null) {
                return new ResultStructure<R>();
            }
            return result;
        }

        @Override
        public Set<R> get() {
            HashSet<R> resultSet = new HashSet<R>();
            for (V value : treeMap.values()) {
                Set<R> set = value.get();
                resultSet.addAll(set);
            }
            return resultSet;
        }

    }

    public static class TwoColumnTreeStructure<K extends Comparable<K>, K2, V extends Structure<K2, R, V>, R> extends
            Structure<K, R, TwoColumnTreeStructure<K, K2, V, R>> {

        private TreeMap<TwoColumnKey<K>, V> treeMap;

        public TwoColumnTreeStructure() {
            this.treeMap = new TreeMap<TwoColumnKey<K>, V>();
        }

        void put(K lower, K upper, V value) {
            treeMap.put(new TwoColumnKey<K>(lower, upper), value);
        }

        @Override
        public Structure<?, R, ?> get(K key) {
            V result = treeMap.get(new TwoColumnKey<K>(key, key));
            // TODO tree traversal
            if (result == null) {
                return new ResultStructure<R>();
            }
            return result;
        }

        @Override
        public Set<R> get() {
            HashSet<R> resultSet = new HashSet<R>();
            for (V value : treeMap.values()) {
                Set<R> set = value.get();
                resultSet.addAll(set);
            }
            return resultSet;
        }

        @Override
        void merge(TwoColumnTreeStructure<K, K2, V, R> otherStructure) {
            // TODO Auto-generated method stub

        }

    }

}
