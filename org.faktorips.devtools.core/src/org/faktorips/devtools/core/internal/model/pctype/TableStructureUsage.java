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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsObjectPart;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.ITableStructureUsage;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Implementation of ITableStructureUsage.
 * 
 * @author Joerg Ortmann
 */
public class TableStructureUsage extends IpsObjectPart implements ITableStructureUsage {
    
    final static String TAG_NAME = "TableStructureUsage"; //$NON-NLS-1$
    
    final static String TAG_TABLE_STRUCTURE = "TableStructure"; //$NON-NLS-1$
    
    private boolean deleted = false;
    
    private String roleName = ""; //$NON-NLS-1$
    
    // Contains the related table structures identified by the full qualified name
    private List tableStructures = new ArrayList();
    
    public TableStructureUsage(IPolicyCmptType pcType, int id) {
        super(pcType, id);
    }

    /**
     * Constructor for testing purposes.
     */
    public TableStructureUsage() {
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
    public void delete() {
        ((PolicyCmptType)getIpsObject()).removeTableStructureUsage(this);
        deleted = true;
        objectHasChanged();

    }

    /**
     * {@inheritDoc}
     */
    public boolean isDeleted() {
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    public IIpsObjectPart newPart(Class partType) {
        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("TableStructure.gif"); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        roleName = element.getAttribute(PROPERTY_ROLENAME);
        NodeList nl = element.getElementsByTagName(TAG_TABLE_STRUCTURE);
        for (int i = 0; i < nl.getLength(); i++) {
            Element child = (Element) nl.item(i);
            tableStructures.add(child.getAttribute(PROPERTY_TABLESTRUCTURE));
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_ROLENAME, roleName);
        for (Iterator iter = tableStructures.iterator(); iter.hasNext();) {
            String tblStructure = (String)iter.next();
            Element child = XmlUtil.addNewChild(element.getOwnerDocument(), element, TAG_TABLE_STRUCTURE);
            child.setAttribute(PROPERTY_TABLESTRUCTURE, tblStructure);
        }
    }

    /** 
     * Overridden.
     */
    public String getName() {
        return roleName;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * {@inheritDoc}
     */
    public void setRoleName(String newRoleName) {
        String oldRoleName = roleName;
        roleName = newRoleName;
        valueChanged(oldRoleName, newRoleName);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getTableStructures() {
        return (String[]) tableStructures.toArray(new String[tableStructures.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public void addTableStructure(String tableStructure) {
        if (tableStructures.contains(tableStructure)){
            return;
        }
        tableStructures.add(tableStructure);
        objectHasChanged();
    }

    /**
     * {@inheritDoc}
     */
    public void removeTableStructure(String tableStructure) {
        if (tableStructures.remove(tableStructure)) {
            objectHasChanged();
        }
    }

    /**
     * {@inheritDoc}
     */
    public int[] moveTableStructure(int[] indexes, boolean up) {
        ListElementMover mover = new ListElementMover(tableStructures);
        return mover.move(indexes, up);
    }

    /**
     * {@inheritDoc}
     */
    protected void validateThis(MessageList list) throws CoreException {
        super.validateThis(list);
        
        // check that each referenced table structure exists
        for (Iterator iter = tableStructures.iterator(); iter.hasNext();) {
            String tblStructure = (String)iter.next();
            if (getIpsProject().findIpsObject(IpsObjectType.TABLE_STRUCTURE, tblStructure) == null){
                String text = NLS.bind(Messages.TableStructureUsage_msgTableStructureNotExists, tblStructure);
                Message msg = new Message(ITableStructureUsage.MSGCODE_TABLE_STRUCTURE_NOT_FOUND, text, Message.ERROR, this);
                list.add(msg);
            }
        }
        
        // check the correct name format
        IStatus status = JavaConventions.validateFieldName(roleName);
        if (!status.isOK()){
            String text = NLS.bind(Messages.TableStructureUsage_msgInvalidRoleName, roleName);
            Message msg = new Message(MSGCODE_INVALID_ROLE_NAME, text, Message.ERROR, this, PROPERTY_NAME);
            list.add(msg);
        }
        
        // check that at least one table structure is referenced
        if (tableStructures.size() == 0){
            String text = Messages.TableStructureUsage_msgAtLeastOneStructureMustBeReferenced;
            Message msg = new Message(MSGCODE_MUST_REFERENCE_AT_LEAST_1_TABLE_STRUCTURE, text, Message.ERROR, this, PROPERTY_NAME);
            list.add(msg);
        }
        
        // check that the role name is not in use by another usage within the supertype hierarchy
        IPolicyCmptType pcType = (IPolicyCmptType)getIpsObject();
        ITypeHierarchy hierarchy = pcType.getSupertypeHierarchy();
        ITableStructureUsage[] tblUsages = hierarchy.getAllTableStructureUsages(pcType);
        for (int i = 0; i < tblUsages.length; i++) {
            if (tblUsages[i] != this && roleName.equals(tblUsages[i].getRoleName())){
                String text = NLS.bind(Messages.TableStructureUsage_msgSameRoleName, roleName);
                Message msg = new Message(MSGCODE_SAME_ROLENAME, text, Message.ERROR, this, PROPERTY_NAME);
                list.add(msg);
                break;
            }
        }
    }
}
