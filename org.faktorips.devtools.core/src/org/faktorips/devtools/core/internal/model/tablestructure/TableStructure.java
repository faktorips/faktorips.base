/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.tablestructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObject;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.IColumnRange;
import org.faktorips.devtools.core.model.tablestructure.IForeignKey;
import org.faktorips.devtools.core.model.tablestructure.IKey;
import org.faktorips.devtools.core.model.tablestructure.IKeyItem;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.tablestructure.IUniqueKey;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.devtools.core.util.TreeSetHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class TableStructure extends IpsObject implements ITableStructure {

    private TableStructureType type = TableStructureType.SINGLE_CONTENT;
    private List<IColumn> columns = new ArrayList<IColumn>(2);
    private List<IColumnRange> ranges = new ArrayList<IColumnRange>(0);
    private List<IUniqueKey> uniqueKeys = new ArrayList<IUniqueKey>(1);
    private List<IForeignKey> foreignKeys = new ArrayList<IForeignKey>(0);

    public TableStructure(IIpsSrcFile file) {
        super(file);
    }

    /**
     * Constructor for testing purposes.
     */
    public TableStructure() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfColumns() + getNumOfRanges() + getNumOfUniqueKeys() + getNumOfForeignKeys();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List<IIpsElement> childrenList = new ArrayList<IIpsElement>(numOfChildren);
        childrenList.addAll(columns);
        childrenList.addAll(ranges);
        childrenList.addAll(uniqueKeys);
        childrenList.addAll(foreignKeys);
        childrenList.toArray(childrenArray);
        return childrenArray;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMultipleContentsAllowed() {
        return type == TableStructureType.MULTIPLE_CONTENTS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTableStructureType(TableStructureType type) {
        if (type == null) {
            return;
        }

        TableStructureType oldType = this.type;
        this.type = type;
        valueChanged(oldType, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableStructureType getTableStructureType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumn[] getColumns() {
        IColumn[] c = new IColumn[columns.size()];
        columns.toArray(c);
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumn getColumn(String name) {
        for (IColumn column : columns) {
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumn getColumn(int index) {
        return columns.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumOfColumns() {
        return columns.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumn newColumn() {
        IColumn newColumn = newColumnInternal(getNextPartId());
        objectHasChanged();
        return newColumn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] moveColumns(int[] indexes, boolean up) {
        ListElementMover<IColumn> mover = new ListElementMover<IColumn>(columns);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnIndex(IColumn column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == column) {
                return i;
            }
        }
        throw new RuntimeException("Can't get index for column " + column); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumnRange[] getRanges() {
        IColumnRange[] c = new IColumnRange[ranges.size()];
        ranges.toArray(c);
        return c;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumnRange getRange(String name) {
        for (IColumnRange range : ranges) {
            if (range.getName().equals(name)) {
                return range;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumOfRanges() {
        return ranges.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumnRange newRange() {
        IColumnRange newRange = newColumnRangeInternal(getNextPartId());
        objectHasChanged();
        return newRange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] moveRanges(int[] indexes, boolean up) {
        ListElementMover<IColumnRange> mover = new ListElementMover<IColumnRange>(ranges);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public IUniqueKey[] getUniqueKeys() {
        IUniqueKey[] keys = new IUniqueKey[uniqueKeys.size()];
        uniqueKeys.toArray(keys);
        return keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUniqueKey getUniqueKey(String name) {
        for (IUniqueKey key : uniqueKeys) {
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumOfUniqueKeys() {
        return uniqueKeys.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IUniqueKey newUniqueKey() {
        IUniqueKey newUniqueKey = newUniqueKeyInternal(getNextPartId());
        objectHasChanged();
        return newUniqueKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] moveUniqueKeys(int[] indexes, boolean up) {
        ListElementMover<IUniqueKey> mover = new ListElementMover<IUniqueKey>(uniqueKeys);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IUniqueKey newUniqueKeyInternal(String id) {
        IUniqueKey newUniqueKey = new UniqueKey(this, id);
        uniqueKeys.add(newUniqueKey);
        return newUniqueKey;
    }

    void removeUniqueKey(IUniqueKey key) {
        uniqueKeys.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IForeignKey[] getForeignKeys() {
        IForeignKey[] keys = new IForeignKey[foreignKeys.size()];
        foreignKeys.toArray(keys);
        return keys;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IForeignKey getForeignKey(String name) {
        for (IForeignKey key : foreignKeys) {
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumOfForeignKeys() {
        return foreignKeys.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IForeignKey newForeignKey() {
        IForeignKey newForeignKey = newForeignKeyInternal(getNextPartId());
        objectHasChanged();
        return newForeignKey;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] moveForeignKeys(int[] indexes, boolean up) {
        ListElementMover<IForeignKey> mover = new ListElementMover<IForeignKey>(foreignKeys);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_STRUCTURE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasRange(String name) {
        return getRange(name) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasColumn(String name) {
        return getColumn(name) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ITableAccessFunction[] getAccessFunctions() {
        if (getUniqueKeys().length == 0) {
            return new ITableAccessFunction[0];
        }
        List<ITableAccessFunction> functions = new ArrayList<ITableAccessFunction>();
        IUniqueKey[] keys = getUniqueKeys();
        // add functions for each key and column which is not in the key
        for (IUniqueKey key : keys) {
            IColumn[] columns = getColumnsNotInKey(key);
            for (int j = 0; j < columns.length; j++) {
                // add function for each column which is not included in the key
                functions.add(createFunction("" + j, key, columns[j]));
            }
        }
        return functions.toArray(new ITableAccessFunction[functions.size()]);
    }

    private ITableAccessFunction createFunction(String id, IUniqueKey key, IColumn column) {
        TableAccessFunction fct = new TableAccessFunction(this, id);
        fct.setAccessedColumn(column.getName());
        fct.setType(column.getDatatype());
        StringBuffer description = new StringBuffer(Messages.TableStructure_descriptionStart);
        IKeyItem[] items = key.getKeyItems();
        String[] argTypes = new String[items.length];
        for (int i = 0; i < items.length; i++) {
            argTypes[i] = items[i].getDatatype();
            if (i > 0) {
                description.append(", "); //$NON-NLS-1$
            }
            description.append(items[i].getAccessParameterName());
        }
        fct.setArgTypes(argTypes);
        description.append(Messages.TableStructure_descriptionEnd + column.getName());
        fct.setDescription(description.toString());
        return fct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IColumn[] getColumnsNotInKey(IKey key) {
        ArgumentCheck.notNull(key);
        List<IColumn> columnsNotInKey = new ArrayList<IColumn>(columns);
        IKeyItem[] items = key.getKeyItems();
        for (IKeyItem item : items) {
            IColumn[] columnsInItem = item.getColumns();
            for (IColumn element : columnsInItem) {
                columnsNotInKey.remove(element);
            }
        }
        return columnsNotInKey.toArray(new IColumn[columnsNotInKey.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TYPE, type.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);

        String typeId = element.getAttribute(PROPERTY_TYPE);

        if (typeId.length() > 0) {
            type = TableStructureType.getTypeForId(typeId);
        } else {
            // Code for migrating old table structures
            // TODO remove migration code
            if (Boolean.valueOf(element.getAttribute("multipleContentsAllowed")).booleanValue()) { //$NON-NLS-1$
                type = TableStructureType.MULTIPLE_CONTENTS;
            } else {
                type = TableStructureType.SINGLE_CONTENT;
            }
        }
    }

    @Override
    protected void reinitPartCollections() {
        columns.clear();
        ranges.clear();
        uniqueKeys.clear();
        foreignKeys.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IColumn) {
            columns.add((IColumn)part);
            return;
        } else if (part instanceof IColumnRange) {
            ranges.add((IColumnRange)part);
            return;
        } else if (part instanceof IUniqueKey) {
            uniqueKeys.add((IUniqueKey)part);
            return;
        } else if (part instanceof IForeignKey) {
            foreignKeys.add((IForeignKey)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IColumn) {
            columns.remove(part);
            return;
        } else if (part instanceof IColumnRange) {
            ranges.remove(part);
            return;
        } else if (part instanceof IUniqueKey) {
            uniqueKeys.remove(part);
            return;
        } else if (part instanceof IForeignKey) {
            foreignKeys.remove(part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();

        if (xmlTagName.equals(Column.TAG_NAME)) {
            return newColumnInternal(id);
        } else if (xmlTagName.equals(ColumnRange.TAG_NAME)) {
            return newColumnRangeInternal(id);
        } else if (xmlTagName.equals(UniqueKey.TAG_NAME)) {
            return newUniqueKeyInternal(id);
        } else if (xmlTagName.equals(ForeignKey.TAG_NAME)) {
            return newForeignKeyInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        if (partType.equals(IColumn.class)) {
            return newColumnInternal(getNextPartId());
        } else if (partType.equals(IColumnRange.class)) {
            return newColumnRangeInternal(getNextPartId());
        } else if (partType.equals(IUniqueKey.class)) {
            return newUniqueKeyInternal(getNextPartId());
        } else if (partType.equals(IForeignKey.class)) {
            return newForeignKeyInternal(getNextPartId());
        }
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean isModelEnumType() {
        // Ok to use the deprecated constant here as the method itself is deprecated.
        return type == TableStructureType.ENUMTYPE_MODEL;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IUniqueKey[] keys = getUniqueKeys();
        if (keys.length > 1) {
            list.add(new Message(MSGCODE_MORE_THAN_ONE_KEY_NOT_ADVISABLE_IN_FORMULAS,
                    Messages.TableStructure_msgMoreThanOneKeyNotAdvisableInFormulas, Message.WARNING, this));
        }
    }

    @Override
    public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().findReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllTableContentsSrcFiles(this)));
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

}
