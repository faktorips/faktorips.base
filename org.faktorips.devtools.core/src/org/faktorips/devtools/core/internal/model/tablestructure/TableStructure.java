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
import java.util.Iterator;
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

/**
 *
 */
public class TableStructure extends IpsObject implements ITableStructure {

    private TableStructureType type = TableStructureType.SINGLE_CONTENT;
    private List columns = new ArrayList(2);
    private List ranges = new ArrayList(0);
    private List uniqueKeys = new ArrayList(1);
    private List foreignKeys = new ArrayList(0);

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
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.model.IIpsElement#getChildren()
     */
    @Override
    public IIpsElement[] getChildren() {
        int numOfChildren = getNumOfColumns() + getNumOfRanges() + getNumOfUniqueKeys() + getNumOfForeignKeys();
        IIpsElement[] childrenArray = new IIpsElement[numOfChildren];
        List childrenList = new ArrayList(numOfChildren);
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
    public boolean isMultipleContentsAllowed() {
        return type == TableStructureType.MULTIPLE_CONTENTS;
    }

    public void setTableStructureType(TableStructureType type) {
        if (type == null) {
            return;
        }

        TableStructureType oldType = this.type;
        this.type = type;
        valueChanged(oldType, type);
    }

    public TableStructureType getTableStructureType() {
        return type;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getColumns()
     */
    public IColumn[] getColumns() {
        IColumn[] c = new IColumn[columns.size()];
        columns.toArray(c);
        return c;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getColumn(java.lang.String)
     */
    public IColumn getColumn(String name) {
        for (Iterator it = columns.iterator(); it.hasNext();) {
            IColumn column = (IColumn)it.next();
            if (column.getName().equals(name)) {
                return column;
            }
        }
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getColumn(int)
     */
    public IColumn getColumn(int index) {
        return (IColumn)columns.get(index);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getNumOfColumns()
     */
    public int getNumOfColumns() {
        return columns.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#newColumn()
     */
    public IColumn newColumn() {
        IColumn newColumn = newColumnInternal(getNextPartId());
        objectHasChanged();
        return newColumn;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#moveColumns(int[],
     *      boolean)
     */
    public int[] moveColumns(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(columns);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IColumn newColumnInternal(int id) {
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
    public int getColumnIndex(IColumn column) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == column) {
                return i;
            }
        }
        throw new RuntimeException("Can't get index for column " + column); //$NON-NLS-1$
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getRanges()
     */
    public IColumnRange[] getRanges() {
        IColumnRange[] c = new IColumnRange[ranges.size()];
        ranges.toArray(c);
        return c;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getRange(java.lang.String)
     */
    public IColumnRange getRange(String name) {
        for (Iterator it = ranges.iterator(); it.hasNext();) {
            IColumnRange range = (IColumnRange)it.next();
            if (range.getName().equals(name)) {
                return range;
            }
        }
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getNumOfRanges()
     */
    public int getNumOfRanges() {
        return ranges.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#newColumn()
     */
    public IColumnRange newRange() {
        IColumnRange newRange = newColumnRangeInternal(getNextPartId());
        objectHasChanged();
        return newRange;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#moveRanges(int[],
     *      boolean)
     */
    public int[] moveRanges(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(ranges);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IColumnRange newColumnRangeInternal(int id) {
        IColumnRange newRange = new ColumnRange(this, id);
        ranges.add(newRange);
        return newRange;
    }

    void removeRange(IColumnRange range) {
        ranges.remove(range);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getUniqueKeys()
     */
    public IUniqueKey[] getUniqueKeys() {
        IUniqueKey[] keys = new IUniqueKey[uniqueKeys.size()];
        uniqueKeys.toArray(keys);
        return keys;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getUniqueKey(java.lang.String)
     */
    public IUniqueKey getUniqueKey(String name) {
        for (Iterator it = uniqueKeys.iterator(); it.hasNext();) {
            IUniqueKey key = (IUniqueKey)it.next();
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getNumOfUniqueKeys()
     */
    public int getNumOfUniqueKeys() {
        return uniqueKeys.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#newUniqueKey()
     */
    public IUniqueKey newUniqueKey() {
        IUniqueKey newUniqueKey = newUniqueKeyInternal(getNextPartId());
        objectHasChanged();
        return newUniqueKey;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#moveUniqueKeys(int[],
     *      boolean)
     */
    public int[] moveUniqueKeys(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(uniqueKeys);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IUniqueKey newUniqueKeyInternal(int id) {
        IUniqueKey newUniqueKey = new UniqueKey(this, id);
        uniqueKeys.add(newUniqueKey);
        return newUniqueKey;
    }

    void removeUniqueKey(IUniqueKey key) {
        uniqueKeys.remove(key);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getForeignKeys()
     */
    public IForeignKey[] getForeignKeys() {
        IForeignKey[] keys = new IForeignKey[foreignKeys.size()];
        foreignKeys.toArray(keys);
        return keys;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getForeignKey(java.lang.String)
     */
    public IForeignKey getForeignKey(String name) {
        for (Iterator it = foreignKeys.iterator(); it.hasNext();) {
            IForeignKey key = (IForeignKey)it.next();
            if (key.getName().equals(name)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#getNumOfForeignKeys()
     */
    public int getNumOfForeignKeys() {
        return foreignKeys.size();
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#newUniqueKey()
     */
    public IForeignKey newForeignKey() {
        IForeignKey newForeignKey = newForeignKeyInternal(getNextPartId());
        objectHasChanged();
        return newForeignKey;
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.tablestructure.ITableStructure#moveForeignKeys(int[],
     *      boolean)
     */
    public int[] moveForeignKeys(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(foreignKeys);
        int[] result = mover.move(indexes, up);
        objectHasChanged();
        return result;
    }

    private IForeignKey newForeignKeyInternal(int id) {
        IForeignKey newForeignKey = new ForeignKey(this, id);
        foreignKeys.add(newForeignKey);
        return newForeignKey;
    }

    void removeForeignKey(IForeignKey key) {
        foreignKeys.remove(key);
    }

    /**
     * Overridden method.
     * 
     * @see org.faktorips.devtools.core.model.ipsobject.IIpsObject#getIpsObjectType()
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_STRUCTURE;
    }

    /**
     * Overridden.
     */
    public boolean hasRange(String name) {
        return getRange(name) != null;
    }

    /**
     * Overridden.
     */
    public boolean hasColumn(String name) {
        return getColumn(name) != null;
    }

    /**
     * Overridden IMethod.
     */
    public ITableAccessFunction[] getAccessFunctions() {
        if (getUniqueKeys().length == 0) {
            return new ITableAccessFunction[0];
        }
        List functions = new ArrayList();
        IUniqueKey[] keys = getUniqueKeys();
        // add functions for each key and column which is not in the key
        for (int i = 0; i < keys.length; i++) {
            IUniqueKey key = keys[i];
            IColumn[] columns = getColumnsNotInKey(key);
            for (int j = 0; j < columns.length; j++) {
                // add function for each column which is not included in the key
                functions.add(createFunction(j, key, columns[j]));
            }
        }
        return (ITableAccessFunction[])functions.toArray(new ITableAccessFunction[functions.size()]);
    }

    private ITableAccessFunction createFunction(int id, IUniqueKey key, IColumn column) {
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
     * Overridden.
     */
    public IColumn[] getColumnsNotInKey(IKey key) {
        ArgumentCheck.notNull(key);
        List columnsNotInKey = new ArrayList(columns);
        IKeyItem[] items = key.getKeyItems();
        for (int i = 0; i < items.length; i++) {
            IColumn[] columnsInItem = items[i].getColumns();
            for (int j = 0; j < columnsInItem.length; j++) {
                columnsNotInKey.remove(columnsInItem[j]);
            }
        }
        return (IColumn[])columnsNotInKey.toArray(new IColumn[columnsNotInKey.size()]);
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.ipsobject.IpsObject#propertiesToXml(org.w3c.dom.Element)
     */
    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TYPE, type.getId());
    }

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.ipsobject.IpsObject#initPropertiesFromXml(org.w3c.dom.Element)
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
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

    /**
     * Overridden IMethod.
     * 
     * @see org.faktorips.devtools.core.internal.model.ipsobject.IpsObject#reinitPartCollections()
     */
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
            columns.add(part);
            return;
        } else if (part instanceof IColumnRange) {
            ranges.add(part);
            return;
        } else if (part instanceof IUniqueKey) {
            uniqueKeys.add(part);
            return;
        } else if (part instanceof IForeignKey) {
            foreignKeys.add(part);
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
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
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
    public IIpsObjectPart newPart(Class partType) {
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
    public boolean isModelEnumType() {
        return type == TableStructureType.ENUMTYPE_MODEL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IUniqueKey[] keys = getUniqueKeys();
        if (keys.length > 1) {
            list.add(new Message(MSGCODE_MORE_THAN_ONE_KEY_NOT_ADVISABLE_IN_FORMULAS,
                    Messages.TableStructure_msgMoreThanOneKeyNotAdvisableInFormulas, Message.WARNING, this));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.faktorips.devtools.core.model.IIpsMetaClass#findAllMetaObjects(org.faktorips.devtools
     * .core.model.ipsproject.IIpsProject, boolean)
     */
    /**
     * {@inheritDoc}
     */
    public IIpsSrcFile[] searchMetaObjectSrcFiles(boolean includeSubtypes) throws CoreException {
        TreeSet<IIpsSrcFile> result = TreeSetHelper.newIpsSrcFileTreeSet();
        IIpsProject[] searchProjects = getIpsProject().getReferencingProjectLeavesOrSelf();
        for (IIpsProject project : searchProjects) {
            result.addAll(Arrays.asList(project.findAllTableContentsSrcFiles(this)));
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

}
