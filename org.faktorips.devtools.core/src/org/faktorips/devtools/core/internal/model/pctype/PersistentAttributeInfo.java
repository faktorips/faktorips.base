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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistableTypeConverter;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.PolicyCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.pctype.IPersistentTypeInfo.InheritanceStrategy;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Section to display the persistence properties of the attributes specific to an
 * {@link IPolicyCmptType}).
 * <p/>
 * The properties are Attibute Name, Table Column Name, Size amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentAttributeInfo extends AtomicIpsObjectPart implements IPersistentAttributeInfo {

    private boolean transientAttribute = false;
    private String tableColumnName = "";

    private boolean tableColumnNullable = true;
    private boolean tableColumnUnique;

    private int tableColumnSize = 255;
    private int tableColumnPrecision = 16;
    private int tableColumnScale = 2;

    private String converterQualifiedClassName = "";
    private String sqlColumnDefinition = "";

    private DateTimeMapping temporalMapping = DateTimeMapping.DATE_ONLY;

    private IIpsObjectPart policyComponentTypeAttribute;

    /**
     * @param policyComponentTypeAttribute
     * @throws CoreException
     */
    public PersistentAttributeInfo(IIpsObjectPart ipsObject, String id) {
        super(ipsObject, id);
        policyComponentTypeAttribute = ipsObject;
    }

    public boolean isTransient() {
        return transientAttribute;
    }

    public String getTableColumnName() {
        return tableColumnName;
    }

    public boolean getTableColumnNullable() {
        return tableColumnNullable;
    }

    public int getTableColumnPrecision() {
        return tableColumnPrecision;
    }

    public int getTableColumnScale() {
        return tableColumnScale;
    }

    public int getTableColumnSize() {
        return tableColumnSize;
    }

    public boolean getTableColumnUnique() {
        return tableColumnUnique;
    }

    public String getConverterQualifiedClassName() {
        return converterQualifiedClassName;
    }

    public String getSqlColumnDefinition() {
        return sqlColumnDefinition;
    }

    public void setTableColumnConverter(IPersistableTypeConverter newConverter) {
        throw new NotImplementedException();
    }

    public void setTableColumnName(String newTableColumnName) {
        ArgumentCheck.notNull(newTableColumnName);
        String oldValue = tableColumnName;
        tableColumnName = newTableColumnName;

        valueChanged(oldValue, tableColumnName);
    }

    public void setTableColumnNullable(boolean nullable) {
        boolean oldValue = tableColumnNullable;
        tableColumnNullable = nullable;

        valueChanged(oldValue, nullable);
    }

    public void setTableColumnPrecision(int precision) {
        int oldValue = tableColumnPrecision;
        tableColumnPrecision = precision;

        valueChanged(oldValue, precision);
    }

    public void setTableColumnScale(int scale) {
        int oldValue = tableColumnScale;
        tableColumnScale = scale;

        valueChanged(oldValue, scale);
    }

    public void setTableColumnSize(int newTableColumnSize) {
        int oldValue = tableColumnSize;
        tableColumnSize = newTableColumnSize;

        valueChanged(oldValue, newTableColumnSize);
    }

    public void setTableColumnUnique(boolean unique) {
        boolean oldValue = tableColumnUnique;
        tableColumnUnique = unique;

        valueChanged(oldValue, unique);
    }

    public void setTransient(boolean transientAttribute) {
        boolean oldValue = this.transientAttribute;
        this.transientAttribute = transientAttribute;
        valueChanged(oldValue, transientAttribute);
    }

    public void setConverterQualifiedClassName(String converterQualifiedClassName) {
        ArgumentCheck.notNull(converterQualifiedClassName);
        String oldValue = this.converterQualifiedClassName;
        this.converterQualifiedClassName = converterQualifiedClassName;
        valueChanged(oldValue, converterQualifiedClassName);
    }

    public void setSqlColumnDefinition(String sqlColumnDefinition) {
        ArgumentCheck.notNull(sqlColumnDefinition);
        String oldValue = this.sqlColumnDefinition;
        this.sqlColumnDefinition = sqlColumnDefinition;
        valueChanged(oldValue, tableColumnName);
    }

    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute() {
        return (IPolicyCmptTypeAttribute)policyComponentTypeAttribute;
    }

    public boolean isPersistentAttribute() {
        AttributeType attrType = getPolicyComponentTypeAttribute().getAttributeType();
        return (attrType == AttributeType.CHANGEABLE || attrType == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
    }

    public DateTimeMapping getTemporalMapping() {
        return temporalMapping;
    }

    public void setTemporalMapping(DateTimeMapping temporalType) {
        DateTimeMapping oldValue = temporalMapping;
        temporalMapping = temporalType;

        valueChanged(oldValue, temporalMapping);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        transientAttribute = Boolean.valueOf(element.getAttribute(PROPERTY_TRANSIENT));
        tableColumnName = element.getAttribute(PROPERTY_TABLE_COLUMN_NAME);
        tableColumnSize = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SIZE));
        tableColumnScale = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SCALE));
        tableColumnPrecision = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_PRECISION));
        tableColumnUnique = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_UNIQE));
        tableColumnNullable = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_NULLABLE));
        temporalMapping = DateTimeMapping.valueOf(element.getAttribute(PROPERTY_TEMPORAL_MAPPING));
        sqlColumnDefinition = element.getAttribute(PROPERTY_SQL_COLUMN_DEFINITION);
        converterQualifiedClassName = element.getAttribute(PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TRANSIENT, Boolean.toString(transientAttribute));
        element.setAttribute(PROPERTY_TABLE_COLUMN_NAME, "" + tableColumnName); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TABLE_COLUMN_SIZE, "" + tableColumnSize); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TABLE_COLUMN_SCALE, "" + tableColumnScale); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TABLE_COLUMN_PRECISION, "" + tableColumnPrecision); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TABLE_COLUMN_UNIQE, "" + tableColumnUnique); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TABLE_COLUMN_NULLABLE, "" + tableColumnNullable); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TEMPORAL_MAPPING, "" + temporalMapping); //$NON-NLS-1$
        element.setAttribute(PROPERTY_SQL_COLUMN_DEFINITION, "" + sqlColumnDefinition); //$NON-NLS-1$
        element.setAttribute(PROPERTY_CONVERTER_QUALIFIED_CLASS_NAME, "" + converterQualifiedClassName); //$NON-NLS-1$
    }

    @Override
    protected void validateThis(MessageList msgList, IIpsProject ipsProject) throws CoreException {
        if (!isPersistentAttribute() || isTransient()
                || !getPolicyComponentTypeAttribute().getPolicyCmptType().isPersistentEnabled()) {
            return;
        }

        validateUniqueColumnNameInHierarchy(msgList);

        if (tableColumnSize < MIN_TABLE_COLUMN_SIZE || tableColumnSize > MAX_TABLE_COLUMN_SIZE) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, "The column size exceeds the limit ["
                    + MIN_TABLE_COLUMN_SIZE + ".." + MAX_TABLE_COLUMN_SIZE + "]", Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE));
        }
        if (tableColumnPrecision < MIN_TABLE_COLUMN_PRECISION || tableColumnPrecision > MAX_TABLE_COLUMN_PRECISION) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS,
                    "The column precision exceeds the limit [" + MIN_TABLE_COLUMN_PRECISION + ".."
                            + MAX_TABLE_COLUMN_PRECISION + "]", Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION));
        }
        if (tableColumnScale < MIN_TABLE_COLUMN_SCALE || tableColumnScale > MAX_TABLE_COLUMN_SCALE) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, "The column scale exceeds the limit ["
                    + MIN_TABLE_COLUMN_SCALE + ".." + MAX_TABLE_COLUMN_SCALE + "]", Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE));
        }
    }

    private void validateUniqueColumnNameInHierarchy(MessageList msgList) throws CoreException {
        if (StringUtils.isBlank(tableColumnName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME, "Empty column name.", Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
            return;
        }

        IPolicyCmptTypeAttribute pcTypeAttribute = getPolicyComponentTypeAttribute();
        ColumnNameCollector columnNameCollector = new ColumnNameCollector(pcTypeAttribute);
        columnNameCollector.start(pcTypeAttribute.getPolicyCmptType());
        if (columnNameCollector.columnNames.contains(tableColumnName)) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_DUPLICATE_COLNAME, "Duplicate column name "
                    + pcTypeAttribute.getPersistenceAttributeInfo().getTableColumnName(), Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }
    }

    private static class ColumnNameCollector extends PolicyCmptTypeHierarchyVisitor {

        private List<String> columnNames = new ArrayList<String>();
        private final IPolicyCmptTypeAttribute startAttribute;
        private InheritanceStrategy lastVisitedTypeInheritanceStrategy;

        public ColumnNameCollector(IPolicyCmptTypeAttribute attribute) {
            startAttribute = attribute;
            lastVisitedTypeInheritanceStrategy = attribute.getPolicyCmptType().getPersistenceTypeInfo()
                    .getInheritanceStrategy();
        }

        private boolean isPersistentAttribute(IPolicyCmptTypeAttribute attribute) {
            return attribute.getPersistenceAttributeInfo().isPersistentAttribute();
        }

        @Override
        protected boolean visit(IPolicyCmptType currentType) throws CoreException {
            InheritanceStrategy currentInheritanceStrategy = currentType.getPersistenceTypeInfo()
                    .getInheritanceStrategy();
            // if (lastVisitedTypeInheritanceStrategy == InheritanceStrategy.MIXED
            // && currentInheritanceStrategy == InheritanceStrategy.SINGLE_TABLE) {
            // // attributes are persisted to two different tables, abort collecting
            // return false;
            // }

            IPolicyCmptTypeAttribute[] policyCmptTypeAttributes = currentType.getPolicyCmptTypeAttributes();
            for (IPolicyCmptTypeAttribute currentAttribute : policyCmptTypeAttributes) {
                if (isPersistentAttribute(currentAttribute) && startAttribute != currentAttribute) {
                    columnNames.add(currentAttribute.getPersistenceAttributeInfo().getTableColumnName());
                }
            }

            if (currentInheritanceStrategy == InheritanceStrategy.JOINED_SUBCLASS) {
                // do not collect supertype attributes, since each table of a JOINED_SUBCLASS
                // hierarchy can have the same column names
                return false;
            }

            lastVisitedTypeInheritanceStrategy = currentInheritanceStrategy;
            return true;
        }
    }
}
