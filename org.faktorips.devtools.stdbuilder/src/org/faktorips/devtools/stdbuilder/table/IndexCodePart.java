/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.table;

import java.util.Arrays;
import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.runtime.internal.tableindex.KeyStructure;
import org.faktorips.runtime.internal.tableindex.RangeStructure;
import org.faktorips.runtime.internal.tableindex.ResultStructure;
import org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure;
import org.faktorips.runtime.internal.tableindex.UniqueResultStructure;

/**
 * For every index there exists this set of variable. Some variables may not be used depending on
 * the kind of index.
 * 
 */
class IndexCodePart {

    private final IIndex index;

    /**
     * The class name of the field holding the index structure
     */
    private String keyStructureFieldClassName;

    /**
     * The name of the field holding the index structure
     */
    private String keyStructureFieldName;

    /**
     * The class name of the key, for example Index0 or if there are only ranges the datatype of the
     * fist range.
     */
    private String indexClassName;

    /**
     * The parameters of the key class constructor, hence the name of all columns that builds the
     * index without ranges!
     */
    private List<String> indexClassParameterNames;

    /**
     * The class names of the parameters of the key class constructor in the same order as
     * {@link #indexClassParameterNames}
     */
    private List<String> indexClassParameterTypes;

    /**
     * The names of all parameters in the find method. This may differ from keyClassParameters if
     * the key contains ranges.
     */
    private List<String> allItemParameterNames;

    /**
     * The class names of all parameters in the find method in the same order as
     * {@link #allItemParameterNames}
     */
    private List<String> allItemParameterTypes;

    /**
     * The suffix of the find method. The method name for the finder methods are provided by the
     * methods {@link TableImplBuilder#getMethodNameFindRow(String, boolean)}
     * {@link TableImplBuilder#getMethodNameFindExistingRow(String)} and
     * {@link TableImplBuilder#getMethodNameFindRowNullRowReturned(String)}. This suffix is provided
     * to the methods. It is only filled if there are two keys with the same datatypes and hence
     * same finder method signatures.
     * <p>
     * For example for a key that contains two columns, gender and age, this suffix may be
     * "ByGenderAge". The resulting finder method name is "findRowByGenderAge"
     */
    private String findMethodNameSuffix;

    public IndexCodePart(IIndex index) {
        this.index = index;
    }

    public String getKeyStructureFieldClass(String qualifiedTableRowName) {
        List<IKeyItem> keyItemList = Arrays.asList(index.getKeyItems());
        return getStructureClass(keyItemList, qualifiedTableRowName);
    }

    public String getIndexClassName() {
        return indexClassName;
    }

    public void setIndexClassName(String indexClassName) {
        this.indexClassName = indexClassName;
    }

    public String getStructureClass(List<IKeyItem> nextKeyItems, String qualifiedTableRowName) {
        StringBuffer className = new StringBuffer();
        if (nextKeyItems.isEmpty()) {
            className.append(getResultStructureClassName());
            className.append("<").append(qualifiedTableRowName).append(">");
        } else {
            IKeyItem firstKeyItem = nextKeyItems.get(0);
            String structureType = getStructureType(firstKeyItem);
            className.append(structureType);
            String nestedGenerics = getGenericsForStructureClass(nextKeyItems, qualifiedTableRowName);
            className.append(nestedGenerics);
        }
        return className.toString();
    }

    public String getGenericsForStructureClass(List<IKeyItem> keyItems, String qualifiedTableRowName) {
        IKeyItem firstKeyItem = keyItems.get(0);
        List<IKeyItem> nextKeyItems = nextKeyItems(keyItems);
        String keyClassName = getKeyClassName(firstKeyItem);
        StringBuffer generics = new StringBuffer();
        generics.append("<").append(keyClassName).append(", ");
        generics.append(getStructureClass(nextKeyItems, qualifiedTableRowName));
        generics.append(", ").append(qualifiedTableRowName);
        generics.append(">");
        return generics.toString();
    }

    /**
     * Returns a sublist of the keyItemList. Strips at least the first element and every further
     * element that is no range.
     */
    private List<IKeyItem> nextKeyItems(List<IKeyItem> keyItemList) {
        List<IKeyItem> nextKeyItems = keyItemList.subList(1, keyItemList.size());
        if (!nextKeyItems.isEmpty() && !nextKeyItems.get(0).isRange()) {
            nextKeyItems = nextKeyItems(nextKeyItems);
        }
        return nextKeyItems;
    }

    private String getKeyClassName(IKeyItem firstKeyItem) {
        String nextKeyClassName;
        if (firstKeyItem.isRange()) {
            nextKeyClassName = getJavaClassName(firstKeyItem);
        } else {
            nextKeyClassName = getIndexClassName();
        }
        return nextKeyClassName;
    }

    private String getResultStructureClassName() {
        if (index.isUniqueKey()) {
            return UniqueResultStructure.class.getName();
        } else {
            return ResultStructure.class.getName();
        }
    }

    public String getStructureType(IKeyItem keyItem) {
        if (keyItem == null) {
            return getResultStructureClassName();
        } else if (keyItem.isRange()) {
            if (((IColumnRange)keyItem).getColumnRangeType().isTwoColumn()) {
                return TwoColumnRangeStructure.class.getName();
            } else {
                return RangeStructure.class.getName();
            }
        } else {
            return KeyStructure.class.getName();
        }
    }

    protected String getJavaClassName(IKeyItem keyItem) {
        Datatype datatypeForKeyName;
        datatypeForKeyName = TableImplBuilder.findDatatype(keyItem.getDatatype(), index.getIpsProject());
        if (datatypeForKeyName != null) {
            return datatypeForKeyName.getJavaClassName();
        } else {
            return null;
        }
    }

    public String getKeyStructureFieldClassName() {
        return keyStructureFieldClassName;
    }

    public void setKeyStructureFieldClassName(String keyStructureFieldClassName) {
        this.keyStructureFieldClassName = keyStructureFieldClassName;
    }

    public String getKeyStructureFieldName() {
        return keyStructureFieldName;
    }

    public void setKeyStructureFieldName(String keyStructureFieldName) {
        this.keyStructureFieldName = keyStructureFieldName;
    }

    public List<String> getIndexClassParameterNames() {
        return indexClassParameterNames;
    }

    public void setIndexClassParameterNames(List<String> indexClassParameterNames) {
        this.indexClassParameterNames = indexClassParameterNames;
    }

    public List<String> getIndexClassParameterTypes() {
        return indexClassParameterTypes;
    }

    public void setIndexClassParameterTypes(List<String> indexClassParameterTypes) {
        this.indexClassParameterTypes = indexClassParameterTypes;
    }

    public List<String> getAllItemParameterNames() {
        return allItemParameterNames;
    }

    public void setAllItemParameterNames(List<String> allItemParameterNames) {
        this.allItemParameterNames = allItemParameterNames;
    }

    public List<String> getAllItemParameterTypes() {
        return allItemParameterTypes;
    }

    public void setAllItemParameterTypes(List<String> allItemParameterTypes) {
        this.allItemParameterTypes = allItemParameterTypes;
    }

    public String getFindMethodNameSuffix() {
        return findMethodNameSuffix;
    }

    public void setFindMethodNameSuffix(String findMethodNameSuffix) {
        this.findMethodNameSuffix = findMethodNameSuffix;
    }

}