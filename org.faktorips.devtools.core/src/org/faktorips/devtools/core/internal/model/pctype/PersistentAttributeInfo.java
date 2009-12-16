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

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.ITableColumnNamingStrategy;
import org.faktorips.devtools.core.model.pctype.IPersistableTypeConverter;
import org.faktorips.devtools.core.model.pctype.IPersistentAttributeInfo;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Roman Grutza
 */
public class PersistentAttributeInfo extends AtomicIpsObjectPart implements IPersistentAttributeInfo {

    private String tableColumnName;
    private int tableColumnSize = 255;
    private boolean tableColumnUnique;
    private boolean tableColumnNullable;
    private int tableColumnScale = 16;
    private int tableColumnPrecision = 2;

    private IIpsObjectPart policyComponentTypeAttribute;

    /**
     * @param policyComponentTypeAttribute
     * @throws CoreException
     */
    public PersistentAttributeInfo(IIpsObjectPart ipsObject, int id) {
        super(ipsObject, id);
        policyComponentTypeAttribute = ipsObject;

        ITableColumnNamingStrategy tableColumnNamingStrategy = getIpsProject().getTableColumnNamingStrategy();
        tableColumnName = tableColumnNamingStrategy.getTableColumnName(ipsObject.getName());
    }

    /**
     * {@inheritDoc}
     */
    // FIXME RG: implement
    public IPersistableTypeConverter getTableColumnConverter() {
        throw new NotImplementedException();
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

    public void setTableColumnConverter(IPersistableTypeConverter newConverter) {
        throw new NotImplementedException();
    }

    public void setTableColumnName(String newTableColumnName) {
        String oldValue = tableColumnName;
        tableColumnName = newTableColumnName;

        valueChanged(oldValue, newTableColumnName);
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

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute getPolicyComponentTypeAttribute() {
        return (IPolicyCmptTypeAttribute)policyComponentTypeAttribute;
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        tableColumnName = element.getAttribute(PROPERTY_TABLE_COLUMN_NAME);
        tableColumnSize = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SIZE));
        tableColumnScale = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_SCALE));
        tableColumnPrecision = Integer.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_PRECISION));
        tableColumnUnique = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_UNIQE));
        tableColumnNullable = Boolean.valueOf(element.getAttribute(PROPERTY_TABLE_COLUMN_NULLABLE));
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_TABLE_COLUMN_NAME, "" + tableColumnName);
        element.setAttribute(PROPERTY_TABLE_COLUMN_SIZE, "" + tableColumnSize);
        element.setAttribute(PROPERTY_TABLE_COLUMN_SCALE, "" + tableColumnScale);
        element.setAttribute(PROPERTY_TABLE_COLUMN_PRECISION, "" + tableColumnPrecision);
        element.setAttribute(PROPERTY_TABLE_COLUMN_UNIQE, "" + tableColumnUnique);
        element.setAttribute(PROPERTY_TABLE_COLUMN_NULLABLE, "" + tableColumnUnique);
    }

    public Image getImage() {
        //        IpsPlugin.getDefault().getImage("FIXME.gif"); //$NON-NLS-1$
        return null;
    }

}
