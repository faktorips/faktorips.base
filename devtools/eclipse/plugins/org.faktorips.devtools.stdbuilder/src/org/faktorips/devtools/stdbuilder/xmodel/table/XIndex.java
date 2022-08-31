/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.xmodel.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.util.NLS;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.ColumnRangeType;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xmodel.MethodParameter;
import org.faktorips.devtools.stdbuilder.xmodel.ModelService;
import org.faktorips.devtools.stdbuilder.xtend.GeneratorModelContext;
import org.faktorips.runtime.internal.tableindex.KeyStructure;
import org.faktorips.runtime.internal.tableindex.RangeStructure;
import org.faktorips.runtime.internal.tableindex.RangeType;
import org.faktorips.runtime.internal.tableindex.ResultStructure;
import org.faktorips.runtime.internal.tableindex.TwoColumnRangeStructure;
import org.faktorips.runtime.internal.tableindex.UniqueResultStructure;

public class XIndex extends AbstractGeneratorModelNode {

    private static final String FIELD_NAME_SEARCHSTRUCTURE = "SearchStructure";
    private static final String EXCEPTION_FIND_EXISTING_ROW = "EXCEPTION_FIND_EXISTING_ROW";

    private int indexInList;

    private String tableRowClass;

    /**
     * range key names uncapitalized
     */
    private List<String> rangeKeyNames = new ArrayList<>();
    private List<IKeyItem> rangeKeys = new ArrayList<>();
    /**
     * column key names uncapitalized
     */
    private List<String> columnKeyNames = new ArrayList<>();
    private List<XColumn> columnKeys = new ArrayList<>();

    public XIndex(IIndex index, GeneratorModelContext context, ModelService modelService) {
        super(index, context, modelService);
        createRangeKeyList();
    }

    protected IIndex getIndex() {
        return (IIndex)getIpsObjectPartContainer();
    }

    /**
     * Add all key items those are range structure to range list, column keys to column key list
     */
    private void createRangeKeyList() {
        for (IKeyItem keyItem : getIndex().getKeyItems()) {
            if (keyItem.isRange()) {
                rangeKeyNames.add(StringUtils.uncapitalize(getTableStructure().getRange(keyItem.getName())
                        .getParameterName()));
                rangeKeys.add(keyItem);
            } else if (getTableStructure().hasColumn(keyItem.getName())) {
                columnKeyNames.add(StringUtils.uncapitalize(keyItem.getName()));
                columnKeys.add(getModelNode(keyItem, XColumn.class));
            }
        }
    }

    /**
     * (from: IndexCodePart#getIndexClassName()) This method is called from XTable.
     * 
     * @param indexInList position of the index in getKeyItems
     */
    public void setIndexInList(int indexInList) {
        this.indexInList = indexInList;
    }

    /**
     * This method must be called from XTable!
     */
    public void setTableRowName(String tableRowClass) {
        this.tableRowClass = tableRowClass;
    }

    /**
     * @return class name if any key is a column. Else returns the class name of the type of the
     *             first key item
     */
    public String getClassOrTypeName() {
        if (getIndex().containsColumns()) {
            return "Index" + indexInList;
        } else {
            return getJavaClassName(getIndex().getKeyItemAt(0));
        }
    }

    /**
     * @return key + index position + FIELD_NAME_SEARCHSTRUCTURE
     */
    public String getKeySearchStructureName() {
        return "key" + indexInList + FIELD_NAME_SEARCHSTRUCTURE;
    }

    public List<String> getRangeKeyNames() {
        return rangeKeyNames;
    }

    public String getColumnKeyNames() {
        return StringUtils.join(columnKeyNames, ",");
    }

    public List<XColumn> getColumnKeys() {
        return columnKeys;
    }

    public boolean hasColumnKeys() {
        return !columnKeys.isEmpty();
    }

    public boolean isUniqueKey() {
        return getIndex().isUniqueKey();
    }

    private ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObjectPartContainer().getIpsObject();
    }

    public boolean isFirstKeyRange() {
        return getIndex().getKeyItems().length > 0 && getIndex().getKeyItemAt(0).isRange();
    }

    /**
     * (from: IndexCodePart)
     * 
     * @return data type of the given key item
     */
    public String getJavaClassName(IKeyItem keyItem) {
        return addImport(findDatatypeHelper(keyItem.getDatatype(), keyItem.getIpsProject()).getJavaClassName());
    }

    private static DatatypeHelper findDatatypeHelper(String name, IIpsProject ipsProject) {
        return ipsProject.findDatatypeHelper(name);
    }

    /**
     * (from: IndexCodePart)
     * 
     * @return KeyStructure, TwoColumnRangeStructure or RangeStructure as type
     */
    public String getStructureType(IKeyItem keyItem) {
        if (keyItem == null) {
            return getResultStructureClassName();
        } else if (keyItem.isRange()) {
            if (((IColumnRange)keyItem).getColumnRangeType().isTwoColumn()) {
                return addImport(TwoColumnRangeStructure.class.getName());
            } else {
                return addImport(RangeStructure.class.getName());
            }
        } else {
            return addImport(KeyStructure.class.getName());
        }
    }

    /**
     * (from: IndexCodePart)
     * 
     * @return UniqueResultStructure as string if index is a unique key, else ResultStructure
     */
    public String getResultStructureClassName() {
        if (getIndex().isUniqueKey()) {
            return addImport(UniqueResultStructure.class.getName());
        } else {
            return addImport(ResultStructure.class.getName());
        }
    }

    private List<IKeyItem> getRangeKeysNotFirst() {
        List<IKeyItem> rangeKeysForGeneric = new ArrayList<>();
        if (isFirstKeyRange()) {
            rangeKeysForGeneric.addAll(rangeKeys.subList(1, rangeKeys.size()));
        } else {
            rangeKeysForGeneric.addAll(rangeKeys);
        }
        return rangeKeysForGeneric;
    }

    /**
     * This method returns the class of the field for this index.
     * 
     * <p>
     * The structure class is a generic type whose generic parameters are generated recursively. It
     * has the form structureClass[classOrTypeName, genericParameters, tableRowClass]. It is
     * generated for the first key, then iterated through all remaining range keys.
     * </p>
     * 
     * @return class of the field for this index as @GenericTypeStringWrapper
     */
    public GenericTypeStringWrapper getStructureClass() {
        List<IKeyItem> keyItems = new ArrayList<>();
        keyItems.add(getIndex().getKeyItems()[0]);
        keyItems.addAll(getRangeKeysNotFirst());
        return getStructureClass(keyItems);
    }

    private GenericTypeStringWrapper getStructureClass(List<IKeyItem> keyItems) {
        if (keyItems.size() == 0) {
            return new GenericTypeStringWrapper(getResultStructureClassName(), tableRowClass);
        } else {
            List<GenericTypeStringWrapper> structureClassGenArgs = new ArrayList<>();
            if (keyItems.get(0).isRange()) {
                structureClassGenArgs.add(new GenericTypeStringWrapper(getJavaClassName(keyItems.get(0))));
            } else {
                // must be the first key
                structureClassGenArgs.add(new GenericTypeStringWrapper(getClassOrTypeName()));
            }
            structureClassGenArgs.add(getStructureClass(keyItems.subList(1, keyItems.size())));
            structureClassGenArgs.add(new GenericTypeStringWrapper(tableRowClass));

            return new GenericTypeStringWrapper(getStructureType(keyItems.get(0)), structureClassGenArgs);
        }
    }

    public boolean hasRangeKeysNotFirst() {
        return getRangeKeysNotFirst().size() > 0;
    }

    /**
     * This method returns a list of wrappers that contains all information needed for the method
     * initKeyMap. A wrapper is created for each key item, starting from the last one. The wrapper
     * contains the structure class constructed from all processed key items, searchStructure name,
     * searchStructure name of the previous key item, and the key item.
     * 
     * @return list of search structures for range key items
     */
    public List<XColumnRangeSearchStructure> getKeyItemsForInitKeyMap() {

        List<XColumnRangeSearchStructure> rangeKeyItemHelpers = new ArrayList<>();

        List<IKeyItem> keyItemList = getRangeKeysNotFirst();
        ArrayList<IKeyItem> processedKeys = new ArrayList<>();

        String prevStructureName = "";
        for (ListIterator<IKeyItem> iterator = keyItemList.listIterator(keyItemList.size()); iterator.hasPrevious();) {
            IKeyItem keyItem = iterator.previous();
            processedKeys.add(0, keyItem);

            String searchStructureName = ((IColumnRange)keyItem).getParameterName() + FIELD_NAME_SEARCHSTRUCTURE
                    + indexInList;
            GenericTypeStringWrapper genericType = getStructureClass(processedKeys);
            rangeKeyItemHelpers.add(new XColumnRangeSearchStructure((IColumnRange)keyItem, getContext(),
                    getModelService(), searchStructureName, prevStructureName, genericType));
            prevStructureName = searchStructureName;

        }
        return rangeKeyItemHelpers;
    }

    public List<XColumn> getColumnsForFirstKey() {
        ArrayList<XColumn> columns = new ArrayList<>();
        for (IColumn column : getIndex().getKeyItemAt(0).getColumns()) {
            columns.add(getModelNode(column, XColumn.class));
        }
        return columns;
    }

    /**
     * @return parameters with data type as a list for constructor
     */
    public List<MethodParameter> getConstructorParameters() {
        List<MethodParameter> params = new ArrayList<>();
        for (XColumn key : columnKeys) {
            params.add(new MethodParameter(key.getDatatypeName(), key.getAttributeName()));
        }
        return params;
    }

    public String getRangeStructureParameter() {
        IKeyItem keyItem = getIndex().getKeyItemAt(0);
        String parameter = "";
        if (keyItem != null && keyItem.isRange()) {
            ColumnRangeType columnRangeType = ((IColumnRange)keyItem).getColumnRangeType();
            if (columnRangeType.isOneColumnFrom()) {
                parameter = addImport(RangeType.class) + "." + RangeType.LOWER_BOUND_EQUAL.name();
            } else if (columnRangeType.isOneColumnTo()) {
                parameter = addImport(RangeType.class) + "." + RangeType.UPPER_BOUND_EQUAL.name();
            }
        }
        return parameter;
    }

    public boolean hasPreviousAndLastKeyIsRange() {
        List<IKeyItem> keys = Arrays.asList(getIndex().getKeyItems());
        for (ListIterator<IKeyItem> iterator = keys.listIterator(keys.size()); iterator.hasPrevious();) {
            IKeyItem lastKeyItem = iterator.previous();
            if (iterator.hasPrevious() && lastKeyItem.isRange()) {
                return true;
            }
        }
        return false;
    }

    public XColumn createFrom(IColumn column) {
        return getModelNode(column, XColumn.class);
    }

    /**
     * @return parameters with data type as a list for find row methods
     */
    public List<MethodParameter> getMethodParametersFindRow() {
        List<MethodParameter> params = new ArrayList<>();
        String parameterName;
        for (IKeyItem key : getIndex().getKeyItems()) {
            if (key.isRange()) {
                parameterName = StringUtils
                        .uncapitalize(getTableStructure().getRange(key.getName()).getParameterName());
            } else {
                parameterName = StringUtils.uncapitalize(key.getAccessParameterName());
            }
            DatatypeHelper datatype = getIpsProject().findDatatypeHelper(key.getDatatype());
            params.add(new MethodParameter(addImport(datatype.getJavaClassName()), parameterName));
        }
        return params;
    }

    public String getFindExistingRowExceptionMessage() {
        StringBuilder text = new StringBuilder();
        text.append("\"");
        text.append(NLS.bind(getLocalizedText(EXCEPTION_FIND_EXISTING_ROW), "\" + getName() + \""));
        text.append(" ");
        List<MethodParameter> parameterNames = getMethodParametersFindRow();
        for (int i = 0; i < parameterNames.size(); i++) {
            text.append(parameterNames.get(i).getName());
            text.append(" = \" + ");
            text.append(parameterNames.get(i).getName());
            if (i < parameterNames.size() - 1) {
                text.append(" + ");
                text.append("\", ");
            }
        }
        return text.toString();
    }
}
