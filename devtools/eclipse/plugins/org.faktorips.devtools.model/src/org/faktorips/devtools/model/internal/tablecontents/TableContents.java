/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.tablecontents;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.DependencyType;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModelExtensions;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.ipsobject.BaseIpsObject;
import org.faktorips.devtools.model.internal.ipsobject.DeprecationValidation;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.model.internal.ipsobject.IpsObjectPartCollection;
import org.faktorips.devtools.model.internal.ipsobject.TimedIpsObject;
import org.faktorips.devtools.model.internal.tablestructure.TableStructure;
import org.faktorips.devtools.model.ipsobject.IDescription;
import org.faktorips.devtools.model.ipsobject.IFixDifferencesComposite;
import org.faktorips.devtools.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.faktorips.devtools.model.plugin.IpsStatus;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablecontents.ITableRows;
import org.faktorips.devtools.model.tablestructure.IColumn;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.faktorips.runtime.internal.IpsStringUtils;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

public class TableContents extends BaseIpsObject implements ITableContents {
    // Performance Potential Tabellen: Datentypen der Columns cachen

    public static final String COLUMNREFERENCENAME = "ColumnReference"; //$NON-NLS-1$

    private static final List<String> FIX_REQUIRING_ERROR_CODES = Arrays.asList(
            MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_NAMES_INVALID,
            MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_ORDERING_INVALID,
            MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMNS_COUNT_INVALID);

    private String structure = ""; //$NON-NLS-1$

    private ITableRows tableRows;

    private int numOfColumns = 0;

    private IpsObjectPartCollection<IPartReference> columnReferences;

    public TableContents(IIpsSrcFile file) {
        super(file);
        columnReferences = new IpsObjectPartCollection<>(this, TableColumnReference.class,
                IPartReference.class, TableColumnReference.XML_TAG);
    }

    @Override
    public ITableRows newTableRows() {
        setTableRowsInternal(createNewTableRowsInternal(getNextPartId()));
        partWasAdded(getTableRowsInternal());
        return getTableRowsInternal();
    }

    protected ITableRows createNewTableRowsInternal(String id) {
        TableRows newTableRows = new TableRows(this, id);
        initUniqueKeyValidator(newTableRows);
        return newTableRows;
    }

    /**
     * Creates an unique key validator for the given table contents generation
     */
    private void initUniqueKeyValidator(TableRows tableRows) {
        ITableStructure tableStructure;
        try {
            tableStructure = findTableStructure(getIpsProject());
        } catch (IpsException e) {
            // will be handled as validation error
            IpsLog.log(e);
            return;
        }
        tableRows.initUniqueKeyValidator(tableStructure, new UniqueKeyValidator());
    }

    @Override
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.TABLE_CONTENTS;
    }

    @Override
    public String getTableStructure() {
        return structure;
    }

    @Override
    public void setTableStructure(String tableStructure) {
        String oldStructure = structure;
        setTableStructureInternal(tableStructure);
        valueChanged(oldStructure, structure);
    }

    protected void setTableStructureInternal(String qName) {
        structure = qName;
    }

    @Override
    public ITableStructure findTableStructure(IIpsProject ipsProject) {
        return (ITableStructure)getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, structure);
    }

    @Override
    public int getNumOfColumns() {
        return numOfColumns;
    }

    public void setNumOfColumnsInternal(int numOfColumns) {
        this.numOfColumns = numOfColumns;
    }

    @Override
    public int newColumn(String defaultValue, String name) {
        newColumnAt(numOfColumns, defaultValue, name);
        return numOfColumns;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Method is overwritten in TableContents to make it visible for
     * {@link TableContentsSaxHandler}.
     */
    @Override
    protected IDescription newDescription(String id) {
        return super.newDescription(id);
    }

    @Override
    public void newColumnAt(int index, String defaultValue, String name) {
        ((TableRows)getTableRowsInternal()).newColumn(index, defaultValue);
        addNewColumnReference(name);
        numOfColumns++;
        objectHasChanged();
    }

    @Override
    public void deleteColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= numOfColumns) {
            throw new IllegalArgumentException("Illegal column index " + columnIndex); //$NON-NLS-1$
        }
        ((TableRows)getTableRowsInternal()).removeColumn(columnIndex);
        removeColumnReference(columnIndex);
        numOfColumns--;
        objectHasChanged();
    }

    @Override
    public void migrateColumnReferences() {
        int referenceCount = getColumnReferencesCount();
        if (referenceCount == 0 && numOfColumns != 0) {
            ITableStructure newTableStructure;
            newTableStructure = findTableStructure(getIpsProject());
            if (newTableStructure != null) {
                // Only references of columns that actually exist in the structure will be
                // created
                for (int i = 0; i < newTableStructure.getNumOfColumns(); i++) {
                    IPartReference newReference = new TableColumnReference(this, getNextPartId());
                    newReference.setName(newTableStructure.getColumn(i).getName());
                    columnReferences.addPart(newReference);
                }
            }
        }
    }

    @Override
    protected IDependency[] dependsOn(Map<IDependency, List<IDependencyDetail>> details) {
        if (IpsStringUtils.isEmpty(getTableStructure())) {
            return new IDependency[0];
        }
        return createDependencies(details);
    }

    private IDependency[] createDependencies(Map<IDependency, List<IDependencyDetail>> details) {
        List<IDependency> dependencies = new ArrayList<>();
        dependencies.add(createStructureDependency(details));
        dependencies.addAll(createValidationDependencies());
        return dependencies.toArray(new IDependency[dependencies.size()]);
    }

    private IDependency createStructureDependency(Map<IDependency, List<IDependencyDetail>> details) {
        IDependency dependency = IpsObjectDependency.createInstanceOfDependency(getQualifiedNameType(),
                new QualifiedNameType(getTableStructure(), IpsObjectType.TABLE_STRUCTURE));
        addDetails(details, dependency, this, PROPERTY_TABLESTRUCTURE);
        return dependency;
    }

    private List<IDependency> createValidationDependencies() {
        ITableStructure tableStructure = findTableStructureInternal();
        if (isSingleContentStructure(tableStructure)) {
            return createValidationDependencies(tableStructure);
        }
        return Collections.emptyList();
    }

    private ITableStructure findTableStructureInternal() {
        return findTableStructure(getIpsProject());
    }

    private boolean isSingleContentStructure(ITableStructure tableStructure) {
        return tableStructure != null && !tableStructure.isMultipleContentsAllowed();
    }

    private List<IDependency> createValidationDependencies(ITableStructure tableStructure) {
        List<IDependency> dependencies = new ArrayList<>();
        List<IIpsSrcFile> siblingSrcFiles = getSiblingTableSrcFiles(tableStructure);
        for (IIpsSrcFile other : siblingSrcFiles) {
            IpsObjectDependency validationDependency = IpsObjectDependency.create(getQualifiedNameType(),
                    other.getQualifiedNameType(), DependencyType.VALIDATION);
            dependencies.add(validationDependency);
        }
        return dependencies;
    }

    private List<IIpsSrcFile> getSiblingTableSrcFiles(ITableStructure tableStructure) {
        List<IIpsSrcFile> tableSrcFiles = getIpsProject().findAllTableContentsSrcFiles(tableStructure);
        tableSrcFiles.remove(getIpsSrcFile());
        return tableSrcFiles;
    }

    @Override
    public void initFromInputStream(InputStream is) {
        initTableContentFromStream(is, false);
    }

    private void initTableContentFromStream(InputStream is, boolean readWholeContent) {
        try {
            reinitPartCollections();
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(is), new TableContentsSaxHandler(this, readWholeContent));
        } catch (SAXNotSupportedException | ParserConfigurationException | IOException e) {
            throw new IpsException(new IpsStatus(e));
        } catch (SAXException e) {
            handleSaxException(e);
        }
    }

    /**
     * {@link SAXException} may be thrown by out {@link TableContentsSaxHandler} when we not want to
     * read the whole table contents ({@link TableRows}). This is the only way to stop the SAX
     * parser and not reading the whole file. If there is another exception included in the
     * {@link SAXException} we want to throw this as {@link CoreException}.
     *
     */
    private void handleSaxException(SAXException e) {
        if (e.getCause() != null) {
            throw new IpsException(new IpsStatus(e.getCause()));
        }
    }

    @Override
    protected void propertiesToXml(Element newElement) {
        super.propertiesToXml(newElement);
        newElement.setAttribute(PROPERTY_TABLESTRUCTURE, structure);
        newElement.setAttribute(PROPERTY_NUMOFCOLUMNS, "" + numOfColumns); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     * <p>
     * The reading of XML for {@link TableContents} is normally done by
     * {@link TableContentsSaxHandler}. These methods are only for compatibility to the common XML
     * DOM parser.
     *
     * @see #initFromInputStream(InputStream)
     */
    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        structure = element.getAttribute(PROPERTY_TABLESTRUCTURE);
        numOfColumns = Integer.parseInt(element.getAttribute(PROPERTY_NUMOFCOLUMNS));
    }

    /**
     * {@inheritDoc}
     * <p>
     * The reading of XML for {@link TableContents} is normally done by
     * {@link TableContentsSaxHandler}. These methods are only for compatibility to the common XML
     * DOM parser.
     * <p>
     * Up to version 3.12 the {@link TableContents} was derived from {@link TimedIpsObject} and
     * hence the {@link TableRows} were table generations derived from {@link IpsObjectGeneration}.
     * Because of these circumstances the old XML format had the tag name "Generations". We still
     * need to read the old format in this method.
     *
     * @see #initFromInputStream(InputStream)
     */
    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (TableRows.TAG_NAME.equals(xmlTagName) || IIpsObjectGeneration.TAG_NAME.equals(xmlTagName)) {
            return createNewTableRowsInternal(id);
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        if (ITableRows.class.isAssignableFrom(partType)) {
            return createNewTableRowsInternal(getNextPartId());
        } else {
            return null;
        }
    }

    @Override
    protected IIpsElement[] getChildrenThis() {

        ArrayList<IIpsElement> children = new ArrayList<>();
        for (IPartReference reference : getColumnReferences()) {
            children.add(reference);
        }
        children.add(getTableRowsInternal());

        return children.toArray(new IIpsElement[children.size()]);

    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        return switch (part) {
            case ITableRows tableRows -> {
                if (!isRowsInitialized()) {
                    setTableRowsInternal(tableRows);
                    yield true;
                } else {
                    throw new IllegalStateException("TableRows object already set for " + this); //$NON-NLS-1$
                }
            }
            case TableColumnReference tableColumnReference -> columnReferences.addPart(tableColumnReference);
            default -> false;
        };
    }

    @Override
    protected void reinitPartCollectionsThis() {
        setTableRowsInternal(null);
        columnReferences.clear();
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof ITableRows) {
            setTableRowsInternal(null);
            return true;
        }
        return false;
    }

    @Override
    protected void validateChildren(MessageList result, IIpsProject ipsProject) {
        if (isRowsInitialized() || IIpsModelExtensions.get().getModelPreferences().isAutoValidateTables()) {
            super.validateChildren(result, ipsProject);
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) {
        super.validateThis(list, ipsProject);
        ITableStructure tableStructure = findTableStructure(ipsProject);
        if (tableStructure == null) {
            String text = MessageFormat.format(Messages.TableContents_msgMissingTablestructure, structure);
            list.add(new Message(MSGCODE_UNKNWON_STRUCTURE, text, Message.ERROR, this, PROPERTY_TABLESTRUCTURE));
            return;
        }
        if (tableStructure.getNumOfColumns() != getNumOfColumns()) {
            String text = MessageFormat.format(Messages.TableContents_NumberOfColumnsInvalid, getNumOfColumns(),
                    tableStructure.getNumOfColumns());
            Message validationMessage = new Message(MSGCODE_INVALID_NUM_OF_COLUMNS, text, Message.ERROR, this);
            list.add(validationMessage);
        }
        DeprecationValidation.validateTableStructureIsNotDeprecated(this, getQualifiedName(), tableStructure,
                ipsProject, list);
        validateColumnReferences(list, tableStructure);
        SingleTableContentsValidator singleTableContentsValidator = SingleTableContentsValidator.createFor(this);
        list.add(singleTableContentsValidator.validateIfPossible());
    }

    /** Validates the {@link IPartReference IPartReferences}. */
    private void validateColumnReferences(MessageList validationMessageList, ITableStructure tableStructure) {
        validateColumnReferencesCount(validationMessageList, tableStructure);
        if (validationMessageList.containsErrorMsg()) {
            return;
        }

        List<IColumn> columns = Arrays.asList(tableStructure.getColumns());

        validateColumnReferenceNames(validationMessageList, tableStructure, columns);
        if (validationMessageList.containsErrorMsg()) {
            return;
        }

        validateColumnReferenceOrdering(validationMessageList, tableStructure, columns);
    }

    /**
     * The number of {@link IPartReference IPartReferences} must match the number of {@link IColumn
     * IColumns} defined in the base {@link ITableStructure}.
     */
    private void validateColumnReferencesCount(MessageList validationMessageList, ITableStructure tableStructure) {
        if (tableStructure.getNumOfColumns() != getColumnReferencesCount()) {
            String text = MessageFormat.format(Messages.TableContents_ReferencedColumnCountInvalid,
                    tableStructure.getQualifiedName());
            Message validationMessage = new Message(
                    ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMNS_COUNT_INVALID, text, Message.ERROR, this);
            validationMessageList.add(validationMessage);
        }
    }

    /**
     * The names of the {@link IPartReference IPartReferences} must match the names of the
     * {@link IColumn IColumns} in the base {@link ITableStructure}.
     */
    private void validateColumnReferenceNames(MessageList validationMessageList,
            ITableStructure tableStructure,
            List<IColumn> columns) {

        for (IColumn currentColumn : columns) {
            if (!(containsEnumAttributeReference(currentColumn.getName()))) {
                String text = MessageFormat.format(Messages.TableContents_ReferencedColumnNamesInvalid,
                        tableStructure.getQualifiedName());
                Message validationMessage = new Message(
                        ITableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_NAMES_INVALID, text, Message.ERROR,
                        this);
                validationMessageList.add(validationMessage);
            }
        }
    }

    /**
     * The ordering of the {@link IPartReference IPartReferences} must match the ordering of the
     * {@link IColumn IColumns} in the base {@link ITableStructure}.
     */
    private void validateColumnReferenceOrdering(MessageList validationMessageList,
            ITableStructure tableStructure,
            List<IColumn> columns) {
        for (int i = 0; i < columns.size(); i++) {
            String currentColumnName = columns.get(i).getName();
            String currentReference = columnReferences.getPart(i).getName();
            if (!(currentColumnName.equals(currentReference))) {
                String text = MessageFormat.format(Messages.TableContents_ReferencedColumnOrderingInvalid,
                        tableStructure.getQualifiedName());
                Message validationMessage = new Message(
                        TableContents.MSGCODE_TABLE_CONTENTS_REFERENCED_COLUMN_ORDERING_INVALID, text, Message.ERROR,
                        this);
                validationMessageList.add(validationMessage);
                break;
            }
        }
    }

    ValueDatatype[] findColumnDatatypes(ITableStructure structure, IIpsProject ipsProject) {
        if (structure == null) {
            return new ValueDatatype[0];
        }
        return ((TableStructure)structure).findColumnDatatypes(ipsProject);
    }

    // only validates the datatypes of the tableStructure that are referenced by this tableContents
    ValueDatatype[] findColumnDatatypesByReferences(ITableStructure structure, IIpsProject ipsProject) {
        if (structure == null) {
            return new ValueDatatype[0];
        }
        ValueDatatype[] valueDatatypes = new ValueDatatype[columnReferences.size()];
        int i = 0;
        for (IPartReference reference : columnReferences) {
            IColumn column = structure.getColumn(reference.getName());
            if (column != null) {
                valueDatatypes[i++] = column.findValueDatatype(ipsProject);
            } else {
                valueDatatypes[i++] = null;
            }
        }
        return valueDatatypes;
    }

    @Override
    public void addExtensionProperty(String propertyId, String extPropertyValue) {
        addExtensionPropertyValue(propertyId, extPropertyValue);
    }

    @Override
    public IIpsSrcFile findMetaClassSrcFile(IIpsProject ipsProject) {
        return ipsProject.findIpsSrcFile(IpsObjectType.TABLE_STRUCTURE, getTableStructure());
    }

    @Override
    public boolean isFixToModelRequired() {
        MessageList messages = validate(getIpsProject());
        for (Message message : messages) {
            if (FIX_REQUIRING_ERROR_CODES.contains(message.getCode())) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method always returns false because differences to model is not supported at the moment
     */
    @Override
    public boolean containsDifferenceToModel(IIpsProject ipsProject) {

        return false;
    }

    /**
     * This method does nothing because there is nothing to do at the moment
     */
    @Override
    public void fixAllDifferencesToModel(IIpsProject ipsProject) {
        // TODO TableContent does not yet support the fix differences framework
    }

    @Override
    public IFixDifferencesComposite computeDeltaToModel(IIpsProject ipsProject) {
        // TODO TableContent does not yet support the fix differences framework
        return null;
    }

    @Override
    public String getMetaClass() {
        return getTableStructure();
    }

    @Override
    public ITableRows getTableRows() {
        return getTableRowsInternal();
    }

    private ITableRows getTableRowsInternal() {
        if (!isRowsInitialized()) {
            readTableRows();
        }
        return tableRows;
    }

    /**
     * Returns whether the {@link TableRows} object has already been initialized. The rows object is
     * lazily initialized when needed. Use this method to check if it is present.
     */
    private boolean isRowsInitialized() {
        return tableRows != null;
    }

    private void readTableRows() {
        if (!getIpsSrcFile().exists()) {
            newTableRows();
            return;
        }
        try (var inputStream = getIpsSrcFile().getContentFromEnclosingResource()) {
            initTableContentFromStream(inputStream, true);
        } catch (IOException e) {
            throw new IpsException(e.getMessage(), e);
        }
    }

    protected void setTableRowsInternal(ITableRows tableRows) {
        this.tableRows = tableRows;
    }

    private void addNewColumnReference(String name) {
        ITableStructure newTableStructure = findTableStructure(getIpsProject());
        if (newTableStructure != null) {
            IPartReference newReference = new TableColumnReference(this, getNextPartId());
            newReference.setName(name);
            columnReferences.addPart(newReference);
        }
    }

    @Override
    public void createColumnReferenceSaxHandler(String referenceName) {
        IPartReference reference = new TableColumnReference(this, getNextPartId());
        reference.setName(referenceName);
        columnReferences.addPart(reference);
    }

    /**
     * Removes the corresponding ColumnReference based on the given index
     *
     * @param columnIndex index of the ColumnReference to be deleted
     */
    private void removeColumnReference(int columnIndex) {
        ITableStructure newTableStructure = findTableStructure(getIpsProject());
        if (newTableStructure != null) {
            columnReferences.removePart(columnReferences.getPart(columnIndex));
        }
    }

    /**
     * Internal function to sort all {@link TableColumnReference TableColumnReferences} in the right
     * order corresponding to given columnList. columnReferences size has to be the same as the
     * columnList
     *
     * @param columnList List of columns of the referenced TableStructure
     */
    private void sortColumnReferencesInternal(IColumn[] columnList) {
        if (columnList.length == columnReferences.size()) {
            IPartReference[] newOrdering = new IPartReference[columnList.length];
            int i = 0;
            for (IColumn column : columnList) {
                for (IPartReference reference : columnReferences) {
                    if (column.getName().equals(reference.getName())) {
                        newOrdering[i++] = reference;
                        break;
                    }
                }
            }
            columnReferences.clear();
            for (IPartReference reference : newOrdering) {
                columnReferences.addPart(reference);
            }
        }
        objectHasChanged(new PropertyChangeEvent(columnReferences, COLUMNREFERENCENAME, null, columnReferences));
    }

    @Override
    public void fixColumnReferences() {
        ITableStructure newTableStructure = findTableStructure(getIpsProject());
        if (newTableStructure != null) {
            updateReferences(newTableStructure);
            sortColumnReferencesInternal(newTableStructure.getColumns());
        }

    }

    /**
     * Updates the references with the columns of the referenced structure
     */
    private void updateReferences(ITableStructure structure) {
        IColumn[] columns = structure.getColumns();
        columnReferences.clear();
        for (IColumn column : columns) {
            IPartReference newReference = new TableColumnReference(this, getNextPartId());
            newReference.setName(column.getName());
            columnReferences.addPart(newReference);
        }
    }

    /**
     * Returns {@code true} if an {@link IPartReference} with the given name exists in this
     * {@link ITableContents}, {@code false} otherwise.
     */
    private boolean containsEnumAttributeReference(String name) {
        for (IPartReference currentReference : columnReferences) {
            if (currentReference.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IPartReference> getColumnReferences() {
        return columnReferences.getBackingList();
    }

    @Override
    public int getColumnReferencesCount() {
        return columnReferences.size();
    }

}
