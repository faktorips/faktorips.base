/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.product;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Thorsten Guenther
 */
public class TableContentUsage extends AtomicIpsObjectPart implements ITableContentUsage {

    /**
     * The full quallified name of the table content this usage defines.
     */
    private String tableContentName = ""; //$NON-NLS-1$
    
    /**
     * The role-name of the structure usage this content usage is based on.
     */
    private String structureUsage = ""; //$NON-NLS-1$
    
    public TableContentUsage(IProductCmptGeneration generation, int id) {
        super(generation, id);
    }

    public TableContentUsage() {
        super();
    }
    
    private ProductCmptGeneration getProductCmptGeneration() {
        return (ProductCmptGeneration) getParent();
    }
    
    private IProductCmptType getProductCmptType() throws CoreException {
        return getProductCmptGeneration().getProductCmpt().findProductCmptType();
    }
    
    /**
     * {@inheritDoc}
     */
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableContentsUsage.gif"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void setStructureUsage(String structureUsage) {
        this.structureUsage = structureUsage;
        objectHasChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getStructureUsage() {
        return this.structureUsage;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setTableContentName(String tableContentName) {
        this.tableContentName = tableContentName;
        objectHasChanged();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTableContentName() {
        return this.tableContentName;
    }

    /**
     * {@inheritDoc}
     */
    public ITableContents findTableContents() throws CoreException {
        return (ITableContents)getIpsProject().findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContentName);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        IProductCmptType type = getProductCmptType();
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TYPE, Messages.TableContentUsage_msgNoType, Message.WARNING, this));
            return;
        }
        
        ITableStructureUsage tsu = type.findTableStructureUsageInSupertypeHierarchy(structureUsage);
        if (tsu == null) {
            String text = NLS.bind(Messages.TableContentUsage_msgUnknownStructureUsage, structureUsage);
            list.add(new Message(MSGCODE_UNKNOWN_STRUCTURE_USAGE, text, Message.ERROR, this, PROPERTY_STRUCTURE_USAGE));
            return;
        }
        
        ITableContents content = null;
        if (tableContentName != null) {
            content = findTableContents();
        }
        if ((content == null && StringUtils.isNotEmpty(tableContentName)) || ((content == null && tsu.isMandatoryTableContent()))) {
            String text = NLS.bind(Messages.TableContentUsage_msgUnknownTableContent, tableContentName);
            list.add(new Message(MSGCODE_UNKNOWN_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
            return;
        }
        
        if (content != null){
            String usedStructure = content.getTableStructure();
            String[] structures = tsu.getTableStructures();
            boolean found = false;
            for (int i = 0; i < structures.length && !found; i++) {
                found = structures[i].equals(usedStructure);
            }
            if (!found) {
                String[] params = {tableContentName, usedStructure, structureUsage};
                String text = NLS.bind(Messages.TableContentUsage_msgInvalidTableContent, params);
                list.add(new Message(MSGCODE_INVALID_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        this.structureUsage = element.getAttribute(PROPERTY_STRUCTURE_USAGE);
        this.tableContentName = ValueToXmlHelper.getValueFromElement(element, "TableContentName"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_STRUCTURE_USAGE, this.structureUsage);
        ValueToXmlHelper.addValueToElement(this.tableContentName, element, "TableContentName"); //$NON-NLS-1$
    }

	public org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage findTableStructureUsage() throws CoreException {
		IProductCmptType type = getProductCmptType();
		
		return type.findTableStructureUsageInSupertypeHierarchy(structureUsage);
	}

}
