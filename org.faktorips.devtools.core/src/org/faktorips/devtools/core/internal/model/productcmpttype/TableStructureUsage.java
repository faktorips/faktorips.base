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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.faktorips.util.message.ObjectProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
* View object for the table structure usage.
* 
* @author JOerg Ortmann
*/
public class TableStructureUsage extends IpsObjectPart implements ITableStructureUsage {
    final static String TAG_NAME = "TableStructureUsage"; //$NON-NLS-1$
    
    private org.faktorips.devtools.core.internal.model.pctype.TableStructureUsage tableStructureUsage;
    
    public TableStructureUsage(org.faktorips.devtools.core.model.pctype.ITableStructureUsage tableStructureUsage) {
        ArgumentCheck.notNull(tableStructureUsage);
        this.tableStructureUsage = (org.faktorips.devtools.core.internal.model.pctype.TableStructureUsage) tableStructureUsage;
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
    public boolean isDeleted() {
        return tableStructureUsage.isDeleted();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        return tableStructureUsage.newPart(partType);
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return tableStructureUsage.getImage();
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() {
        return tableStructureUsage.getIpsObject();
    }
    
    /**
     * {@inheritDoc}
     */
    public void setRoleName(String roleName) {
        tableStructureUsage.setRoleName(roleName);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getRoleName() {
        return tableStructureUsage.getRoleName();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getName(){
        return tableStructureUsage.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getTableStructures() {
        return tableStructureUsage.getTableStructures();
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTableStructure(int[] indexes, boolean up) {
        return tableStructureUsage.moveTableStructure(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptType getProductCmptType() {
        IPolicyCmptType pcType = (IPolicyCmptType)tableStructureUsage.getParent();
        return new ProductCmptType(pcType);
    }

    /**
     * {@inheritDoc}
     */
    public void addTableStructure(String tableStructure) {
        tableStructureUsage.addTableStructure(tableStructure);
    }

    /**
     * {@inheritDoc}
     */
    public void removeTableStructure(String tableStructure) {
        tableStructureUsage.removeTableStructure(tableStructure);
    }
    
    /**
     * {@inheritDoc}
     */
    public void delete() {
        // remove the table structure usage by using its parent view object (product cmpt type)
        ((ProductCmptType)getProductCmptType()).removeTableStructureUsage(this);
    }
    
    /**
     * Returns the table structure usage object this view uses. Package private because only the parent view object uses this method.
     */
    org.faktorips.devtools.core.internal.model.pctype.TableStructureUsage getTableStructureUsage() {
        return tableStructureUsage;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsProject getIpsProject() {
        return tableStructureUsage.getIpsProject();
    }

    /**
     * {@inheritDoc}
     */
    public MessageList validate() throws CoreException {
        // validate and replace the to be viewed object, thus the validation messages could be
        // correctly mapped to this object
        MessageList result = new MessageList();
        MessageList ml = tableStructureUsage.validate();
        for (Iterator iter = ml.iterator(); iter.hasNext();) {
            Message msg = (Message)iter.next();
            ObjectProperty[] props = msg.getInvalidObjectProperties();
            ObjectProperty[] newProps = new ObjectProperty[props.length];
            for (int i = 0; i < props.length; i++) {
                if (props[i].getObject() == tableStructureUsage) {
                    newProps[i] = new ObjectProperty(this, props[i].getProperty());
                } else {
                    newProps[i] = props[i];
                }
            }
            result.add(new Message(msg.getCode(), msg.getText(), msg.getSeverity(), newProps));
        }
        return result;
    }
}
