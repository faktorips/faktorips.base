/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablestructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.faktorips.runtime.internal.IpsStringUtils;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.ipsobject.IpsObject;
import org.faktorips.devtools.model.internal.util.TreeSetHelper;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.IColumnRange;
import org.faktorips.devtools.model.tablestructure.IForeignKey;
import org.faktorips.devtools.model.tablestructure.IIndex;
import org.faktorips.devtools.model.tablestructure.IKey;
import org.faktorips.devtools.model.tablestructure.IKeyItem;
import org.faktorips.devtools.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
import org.faktorips.devtools.model.util.ListElementMover;
import org.faktorips.util.ArgumentCheck;
import org.w3c.dom.Element;

public class TableStructure extends IpsObject implements ITableStructure {

    private TableStructureType type = TableStructureType.MULTIPLE_CONTENTS;

    private List<IColumn> columns = new ArrayList<>(2);

    private List<IColumnRange> ranges = new ArrayList<>(0);

    private List<IIndex> indices = new ArrayList<>(1);

    private List<IForeignKey> foreignKeys = new ArrayList<>(0);

    public TableStructure(IIpsSrcFile file) {
        super(file);
    }

    /**
     * Constructor for testing purposes.
     */
    public TableStructure() {
        super();
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        ArrayList<IDependency> dependencies = new ArrayList<>();
        for (IColumn column : columns) {
            String datatype = column.getDatatype();
            IDependency dependency = new DatatypeDependency(getQualifiedNameType(), datatype);
            dependencies.add(dependency);
            addDetails(details, dependency, column, IColumn.PROPERTY_DATATYPE);
        }
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    @Override
    public boolean isMultipleContentsAllowed() {
        return type == TableStructureType.MULTIPLE_CONTENTS;
    }

    @Override
    public void setTableStructureType(TableStructureType type) {
        if (type == null) {
            return;
        }

        TableStructureType oldType = this.type;
        this.type = type;
        valueChanged(oldType, type);
    }

    @Override
    public TableStructureType getTableStructureType() {
        return type;
    }

    @Override
    public IColumn[] getColumns() {
        IColumn[] c = new IColumn[columns.size()];
        columns.toArray(c);
        return c;
    }

    @Override
    public IColumn getColumn(String name) {
        for (IColumn column : columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public IColumn getColumn(int index) {
        return columns.get(index);
    }

    @Override
    public int getNumOfColumns() {
        return columns.size();
    }

    @Override
    public IColumn newColumn() {
        IColumn newColumn = newColumnInternal(getNextPartId());
        objectHasChanged();
        return newColumn;
    }

    @Override
    public int[] moveColumns(int[] indexes, boolean up) {
        ListElementMover<IColumn> mover = new ListElementMover<>(columns);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IColumn newColumnInternal(String id) {
        IColumn newColumn = new Column(this, id);
        columns.add(newColumn);
        return newColumn;
    }

    void removeColumn(IColumn column) {
        columns.remove(column);
    }

    @Override
    public int getColumnIndex(IColumn column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == column) {
                return i;
            }
        }
        throw new IpsException("Can't get index for column " + column); //$NON-NLS-1$
    }

    @Override
    public int getColumnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equals(columnName)) {
                return i;
            }
        }
        throw new IpsException("Can't get index for column " + columnName); //$NON-NLS-1$
    }

    @Override
    public IColumnRange[] getRanges() {
        IColumnRange[] c = new IColumnRange[ranges.size()];
        ranges.toArray(c);
        return c;
    }

    @Override
    public IColumnRange getRange(String name) {
        for (IColumnRange range : ranges) {
            if (range.getName().equals(name)) {
                return range;
            }
        }
        return null;
    }

    @Override
    public int getNumOfRanges() {
        return ranges.size();
    }

    @Override
    public IColumnRange newRange() {
        IColumnRange newRange = newColumnRangeInternal(getNextPartId());
        objectHasChanged();
        return newRange;
    }

    @Override
    public int[] moveRanges(int[] indexes, boolean up) {
        ListElementMover<IColumnRange> mover = new ListElementMover<>(ranges);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IColumnRange newColumnRangeInternal(String id) {
        IColumnRange newRange = new ColumnRange(this, id);
        ranges.add(newRange);
        return newRange;
    }

    void removeRange(IColumnRange range) {
        ranges.remove(range);
    }

    @Override
    public IIndex[] getUniqueKeys() {
        ArrayList<IIndex> result = new ArrayList<>();
        for (IIndex index : indices) {
            if (index.isUniqueKey()) {
                result.add(index);
            }
        }
        return result.toArray(new IIndex[result.size()]);
    }

    @Override
    public IIndex getUniqueKey(String name) {
        for (IIndex key : indices) {
            if (key.isUniqueKey() && key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public int getNumOfUniqueKeys() {
        return getUniqueKeys().length;
    }

    @Override
    public List<IIndex> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    @Override
    public IIndex getIndex(String name) {
        for (IIndex key : indices) {
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public int getNumOfIndices() {
        return indices.size();
    }

    @Override
    public IIndex newIndex() {
        IIndex newIndex = newIndexInternal(getNextPartId());
        objectHasChanged();
        return newIndex;
    }

    @Override
    public int[] moveIndex(int[] indexes, boolean up) {
        ListElementMover<IIndex> mover = new ListElementMover<>(indices);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IIndex newIndexInternal(String id) {
        IIndex newIndex = new Index(this, id);
        indices.add(newIndex);
        return newIndex;
    }

    void removeIndex(IIndex index) {
        indices.remove(index);
    }

    @Override
    public IForeignKey[] getForeignKeys() {
        IForeignKey[] keys = new IForeignKey[foreignKeys.size()];
        foreignKeys.toArray(keys);
        return keys;
    }

    @Override
    public IForeignKey getForeignKey(String name) {
        for (IForeignKey key : foreignKeys) {
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    @Override
    public int getNumOfForeignKeys() {
        return foreignKeys.size();
    }

    @Override
    public IForeignKey newForeignKey() {
        IForeignKey newForeignKey = newForeignKeyInternal(getNextPartId());
        objectHasChanged();
        return newForeignKey;
    }

    @Override
    public int[] moveForeignKeys(int[] indexes, boolean up) {
        ListElementMover<IForeignKey> mover = new ListElementMover<>(foreignKeys);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IForeignKey newForeignKeyInternal(String id) {
        IForeignKey newForeignKey = new ForeignKey(this, id);
        foreignKeys.add(newForeignKey);
        return newForeignKey;
    }

    void removeForeignKey(IForeignKey key) {
        foreignKeys.remove(key);
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_STRUCTURE;
    }

    @Override
    public boolean hasRange(String name) {
        return getRange(name) != null;
    }

    @Override
    public boolean hasColumn(String name) {
        return getColumn(name) != null;
    }

    @Override
    public ITableAccessFunction[] getAccessFunctions() {
        if (indices.size() == 0) {
            return new ITableAccessFunction[0];
        }

        List<ITableAccessFunction> functions = new ArrayList<>();

        // add functions for each key and column which is not in the key
        for (IIndex index : indices) {
            IColumn[] columnsNotInKey = getColumnsNotInKey(index);
            for (IColumn element : columnsNotInKey) {
                functions.add(createFunction(index, element));
            }
        }

        return functions.toArray(new ITableAccessFunction[functions.size()]);
    }

    private ITableAccessFunction createFunction(IIndex key, IColumn column) {
        return new TableAccessFunction(key, column);
    }

    @Override
    public IColumn[] getColumnsNotInKey(IKey key) {
        ArgumentCheck.notNull(key);
        List<IColumn> columnsNotInKey = new ArrayList<>(columns);
        IKeyItem[] items = key.getKeyItems();
        for (IKeyItem item : items) {
            IColumn[] columnsInItem = item.getColumns();
            for (IColumn element : columnsInItem) {
                columnsNotInKey.remove(element);
            }
        }
        return columnsNotInKey.toArray(new IColumn[columnsNotInKey.size()]);
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TYPE, type.getId());
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);

        String typeId = element.getAttribute(PROPERTY_TYPE);

        if (IpsStringUtils.isNotEmpty(typeId)) {
            type = TableStructureType.getTypeForId(typeId);
        }
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        int size = columns.size() + ranges.size() + indices.size() + foreignKeys.size();
        List<IIpsElement> children = new ArrayList<>(size);
        children.addAll(columns);
        children.addAll(ranges);
        children.addAll(indices);
        children.addAll(foreignKeys);
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected void reinitPartCollectionsThis() {
        columns.clear();
        ranges.clear();
        indices.clear();
        foreignKeys.clear();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IColumn) {
            columns.add((IColumn)part);
            return true;
        } else if (part instanceof IColumnRange) {
            ranges.add((IColumnRange)part);
            return true;
        } else if (part instanceof IIndex) {
            indices.add((IIndex)part);
            return true;
        } else if (part instanceof IForeignKey) {
            foreignKeys.add((IForeignKey)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IColumn) {
            columns.remove(part);
            return true;
        } else if (part instanceof IColumnRange) {
            ranges.remove(part);
            return true;
        } else if (part instanceof IIndex) {
            indices.remove(part);
            return true;
        } else if (part instanceof IForeignKey) {
            foreignKeys.remove(part);
            return true;
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (Column.TAG_NAME.equals(xmlTagName)) {
            return newColumnInternal(id);
        } else if (ColumnRange.TAG_NAME.equals(xmlTagName)) {
            return newColumnRangeInternal(id);
        } else if (Index.TAG_NAME.equals(xmlTagName)) {
            return newIndexInternal(id);
        } else if (ForeignKey.TAG_NAME.equals(xmlTagName)) {
            return newForeignKeyInternal(id);
        }
        return newPartForDeprecatedXml(xmlTagName, id);
    }

    /**
     * @deprecated Load old XML format for backwards compatibility. May be removed when cleaning up
     *                 deprecated API in future releases.
     */
    @Deprecated
    private IIpsObjectPart newPartForDeprecatedXml(String xmlTagName, String id) {
        if ("UniqueKey".equals(xmlTagName)) { //$NON-NLS-1$
            IIndex newIndex = newIndexInternal(id);
            newIndex.setUniqueKey(true);
            return newIndex;
        }
        return null;
    }

    @Override
    public IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (partType.equals(IColumn.class)) {
            return newColumnInternal(getNextPartId());
        } else if (partType.equals(IColumnRange.class)) {
            return newColumnRangeInternal(getNextPartId());
        } else if (partType.equals(IIndex.class)) {
            return newIndexInternal(getNextPartId());
        } else if (partType.equals(IForeignKey.class)) {
            return newForeignKeyInternal(getNextPartId());
        }
        return null;
    }

    @Override
    public Collection<IIpsSrcFile> searchMetaObjectSrcFiles(boolean includeSubtypes) {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(project.findAllTableContentsSrcFiles(this));
        }
        return result;
    }

    @Override
    public boolean hasIndexWithSameDatatype() {
        Set<List<String>> keysDatatypes = new HashSet<>();
        for (IIndex index : indices) {
            List<String> keyDatatype = index.getDatatypes();
            if (keysDatatypes.contains(keyDatatype)) {
                return true;
            }
            keysDatatypes.add(keyDatatype);
        }
        return false;
    }

    /**
     * Returns the datatypes for all this table structure's columns.
     */
    public ValueDatatype[] findColumnDatatypes(IIpsProject ipsProject) {
        ValueDatatype[] datatypes = new ValueDatatype[columns.size()];
        int i = 0;
        for (IColumn column : columns) {
            datatypes[i++] = column.findValueDatatype(ipsProject);
        }
        return datatypes;
    }
}
