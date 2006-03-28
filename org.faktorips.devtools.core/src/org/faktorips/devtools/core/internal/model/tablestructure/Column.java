/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.internal.model.ValidationUtils;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class Column extends IpsObjectPart implements IColumn {
    
    final static String TAG_NAME = "Column"; //$NON-NLS-1$
    
    private String datatype = ""; //$NON-NLS-1$

    Column(TableStructure table, int id) {
        super(table, id);
    }

    /**
     * Constructor for testing purposes.
     */
    Column() {
    }
    
    private TableStructure getTable() {
        return (TableStructure)getIpsObject();
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumn#setName(java.lang.String)
     */ 
    public void setName(String newName) {
        this.name = newName;
        updateSrcFile();
    }

    /**
     * Overridden.
     */
    public String getAccessParameterName() {
        return name;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumn#getDatatype()
     */
    public String getDatatype() {
        return datatype;
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.tablestructure.IColumn#setDatatype(java.lang.String)
     */
    public void setDatatype(String newDatatype) {
        datatype = newDatatype;
        updateSrcFile();
    }

    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsObjectPart#delete()
     */
    public void delete() {
        getTable().removeColumn(this);
        deleted = true;
    }

    private boolean deleted = false;

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
    	return deleted;
    }


    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.IIpsElement#getImage()
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableColumn.gif"); //$NON-NLS-1$
    }

    protected void validate(MessageList list) throws CoreException {
        super.validate(list);
        ValidationUtils.checkStringPropertyNotEmpty(name, "name", this, PROPERTY_NAME, "", list); //$NON-NLS-1$ //$NON-NLS-2$
        ValidationUtils.checkDatatypeReference(datatype, false, this, PROPERTY_DATATYPE, "", list); //$NON-NLS-1$
    }

    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#createElement(org.w3c.dom.Document)
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.internal.model.IpsObjectPartContainer#initPropertiesFromXml(org.w3c.dom.Element)
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute("name"); //$NON-NLS-1$
        datatype = element.getAttribute("datatype"); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute("name", name); //$NON-NLS-1$
        element.setAttribute("datatype", datatype); //$NON-NLS-1$
    }

    /**
     * Overridden.
     */
    public IColumn[] getColumns() {
        return new IColumn[]{this};
    }
    
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPart newPart(Class partType) {
		throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
	}
    
}
