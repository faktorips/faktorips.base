/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.pctype;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IPersistenceOptions;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPersistableTypeConverter;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Section to display the persistence properties of the attributes specific to an
 * {@link IPolicyCmptType}).
 * <p>
 * The properties are Attibute Name, Table Column Name, Size amongst others.
 * 
 * @author Roman Grutza
 */
public class PersistentAttributeInfo extends AtomicIpsObjectPart implements IPersistentAttributeInfo {

    private boolean transientAttribute = false;
    private String tableColumnName = ""; //$NON-NLS-1$

    private boolean tableColumnNullable = true;
    private boolean tableColumnUnique;

    private int tableColumnSize = 255;
    private int tableColumnPrecision = 16;
    private int tableColumnScale = 2;

    private String converterQualifiedClassName = ""; //$NON-NLS-1$
    private String sqlColumnDefinition = ""; //$NON-NLS-1$

    private DateTimeMapping temporalMapping = DateTimeMapping.DATE_ONLY;

    private IIpsObjectPart policyComponentTypeAttribute;

    public PersistentAttributeInfo(IIpsObjectPart ipsObject, String id) {
        super(ipsObject, id);
        policyComponentTypeAttribute = ipsObject;
    }

    @Override
    public boolean isTransient() {
        return transientAttribute;
    }

    @Override
    public String getTableColumnName() {
        return tableColumnName;
    }

    @Override
    public boolean getTableColumnNullable() {
        return tableColumnNullable;
    }

    @Override
    public int getTableColumnPrecision() {
        return tableColumnPrecision;
    }

    @Override
    public int getTableColumnScale() {
        return tableColumnScale;
    }

    @Override
    public int getTableColumnSize() {
        return tableColumnSize;
    }

    @Override
    public boolean getTableColumnUnique() {
        return tableColumnUnique;
    }

    @Override
    public String getConverterQualifiedClassName() {
        return converterQualifiedClassName;
    }

    @Override
    public String getSqlColumnDefinition() {
        return sqlColumnDefinition;
    }

    @Override
    public void setTableColumnConverter(IPersistableTypeConverter newConverter) {
        throw new NotImplementedException();
    }

    @Override
    public void setTableColumnName(String newTableColumnName) {
        ArgumentCheck.notNull(newTableColumnName);
        String oldValue = tableColumnName;
        tableColumnName = newTableColumnName;
        valueChanged(oldValue, tableColumnName);
    }

    @Override
    public void setTableColumnNullable(boolean nullable) {
        boolean oldValue = tableColumnNullable;
        tableColumnNullable = nullable;
        valueChanged(oldValue, nullable);
    }

    @Override
    public void setTableColumnPrecision(int precision) {
        int oldValue = tableColumnPrecision;
        tableColumnPrecision = precision;
        valueChanged(oldValue, precision);
    }

    @Override
    public void setTableColumnScale(int scale) {
        int oldValue = tableColumnScale;
        tableColumnScale = scale;
        valueChanged(oldValue, scale);
    }

    @Override
    public void setTableColumnSize(int newTableColumnSize) {
        int oldValue = tableColumnSize;
        tableColumnSize = newTableColumnSize;
        valueChanged(oldValue, newTableColumnSize);
    }

    @Override
    public void setTableColumnUnique(boolean unique) {
        boolean oldValue = tableColumnUnique;
        tableColumnUnique = unique;
        valueChanged(oldValue, unique);
    }

    @Override
    public void setTransient(boolean transientAttribute) {
        boolean oldValue = this.transientAttribute;
        this.transientAttribute = transientAttribute;
        valueChanged(oldValue, transientAttribute);
    }

    @Override
    public void setConverterQualifiedClassName(String converterQualifiedClassName) {
        ArgumentCheck.notNull(converterQualifiedClassName);
        String oldValue = this.converterQualifiedClassName;
        this.converterQualifiedClassName = converterQualifiedClassName;
        valueChanged(oldValue, converterQualifiedClassName);
    }

    @Override
    public void setSqlColumnDefinition(String sqlColumnDefinition) {
        ArgumentCheck.notNull(sqlColumnDefinition);
        String oldValue = this.sqlColumnDefinition;
        this.sqlColumnDefinition = sqlColumnDefinition;
        valueChanged(oldValue, sqlColumnDefinition);
    }

    @Override
    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute() {
        return (IPolicyCmptTypeAttribute)policyComponentTypeAttribute;
    }

    @Override
    public boolean isPersistentAttribute() {
        AttributeType attrType = getPolicyComponentTypeAttribute().getAttributeType();
        return (attrType == AttributeType.CHANGEABLE || attrType == AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL);
    }

    @Override
    public DateTimeMapping getTemporalMapping() {
        return temporalMapping;
    }

    @Override
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

        if (StringUtils.isBlank(tableColumnName) && !getPolicyComponentTypeAttribute().isOverwrite()) {
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_EMPTY_COLNAME,
                    Messages.PersistentAttributeInfo_msgEmptyColumnName, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }

        validateUsingPersistentOptions(msgList, ipsProject);

        /*
         * to get the max length we use the ips project which belongs to this object not the given
         * project, therefore it is not possible to overwrite this settings by using a different
         * project
         */
        int maxColumnNameLenght = getIpsProject().getProperties().getPersistenceOptions().getMaxColumnNameLenght();
        if (StringUtils.isNotBlank(tableColumnName) && tableColumnName.length() > maxColumnNameLenght) {
            msgList.add(new Message(MSGCODE_COLUMN_NAME_EXCEEDS_MAX_LENGTH, NLS.bind(
                    Messages.PersistentAttributeInfo_msgColumnNameLengthExceedsMaximumLength, tableColumnName.length(),
                    maxColumnNameLenght), Message.ERROR, this, IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_NAME));
        }
    }

    private void validateUsingPersistentOptions(MessageList msgList, IIpsProject ipsProject) {
        IPersistenceOptions pOpt = ipsProject.getProperties().getPersistenceOptions();
        int minTableColumnSize = pOpt.getMinTableColumnSize();
        int maxTableColumnSize = pOpt.getMaxTableColumnSize();
        int minTableColumnPrecision = pOpt.getMinTableColumnPrecision();
        int maxTableColumnPrecision = pOpt.getMaxTableColumnPrecision();
        int minTableColumnScale = pOpt.getMinTableColumnScale();
        int maxTableColumnScale = pOpt.getMaxTableColumnScale();

        if (tableColumnSize < minTableColumnSize || tableColumnSize > maxTableColumnSize) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnSizeExceedsTheLimit, new Object[] {
                    minTableColumnSize, maxTableColumnSize });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SIZE));
        }
        if (tableColumnPrecision < minTableColumnPrecision || tableColumnPrecision > maxTableColumnPrecision) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnPrecisionExceedsTheLimit, new Object[] {
                    minTableColumnPrecision, maxTableColumnPrecision });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_PRECISION));
        }
        if (tableColumnScale < minTableColumnScale || tableColumnScale > maxTableColumnScale) {
            String text = NLS.bind(Messages.PersistentAttributeInfo_msgColumnScaleExceedsTheLimit, new Object[] {
                    minTableColumnScale, maxTableColumnScale });
            msgList.add(new Message(MSGCODE_PERSISTENCEATTR_COL_OUT_OF_BOUNDS, text, Message.ERROR, this,
                    IPersistentAttributeInfo.PROPERTY_TABLE_COLUMN_SCALE));
        }
    }
}
