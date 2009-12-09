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

package org.faktorips.devtools.core.internal.model.productcmpt;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProdDefProperty;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;
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

    public TableContentUsage(IProductCmptGeneration generation, int id, String structureUsage) {
        super(generation, id);
        this.structureUsage = structureUsage;
    }

    public TableContentUsage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return structureUsage;
    }

    /**
     * {@inheritDoc}
     */
    public IProdDefProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findTableStructureUsage(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return ProdDefPropertyType.TABLE_CONTENT_USAGE;
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValue() {
        return tableContentName;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptGeneration getProductCmptGeneration() {
        return (ProductCmptGeneration)getParent();
    }

    private IProductCmptType getProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptGeneration().getProductCmpt().findProductCmptType(ipsProject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        return structureUsage;
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
        return tableContentName;
    }

    /**
     * {@inheritDoc}
     */
    public ITableContents findTableContents(IIpsProject ipsProject) throws CoreException {
        return (ITableContents)ipsProject.findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContentName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TYPE, Messages.TableContentUsage_msgNoType, Message.WARNING, this));
            return;
        }

        ITableStructureUsage tsu = type.findTableStructureUsage(structureUsage, ipsProject);
        if (tsu == null) {
            String text = NLS.bind(Messages.TableContentUsage_msgUnknownStructureUsage, structureUsage);
            list.add(new Message(MSGCODE_UNKNOWN_STRUCTURE_USAGE, text, Message.ERROR, this, PROPERTY_STRUCTURE_USAGE));
            return;
        }

        ITableContents content = null;
        if (tableContentName != null) {
            content = findTableContents(ipsProject);
        }
        if (content == null) {
            if (StringUtils.isNotEmpty(tableContentName) || (tsu.isMandatoryTableContent())) {
                String text = NLS.bind(Messages.TableContentUsage_msgUnknownTableContent, tableContentName);
                list.add(new Message(MSGCODE_UNKNOWN_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
            }
            return;
        }
        String usedStructure = content.getTableStructure();
        if (!tsu.isUsed(usedStructure)) {
            String[] params = { tableContentName, usedStructure, structureUsage };
            String text = NLS.bind(Messages.TableContentUsage_msgInvalidTableContent, params);
            list.add(new Message(MSGCODE_INVALID_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        structureUsage = element.getAttribute(PROPERTY_STRUCTURE_USAGE);
        tableContentName = ValueToXmlHelper.getValueFromElement(element, "TableContentName"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_STRUCTURE_USAGE, structureUsage);
        ValueToXmlHelper.addValueToElement(tableContentName, element, "TableContentName"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public ITableStructureUsage findTableStructureUsage(IIpsProject ipsProject) throws CoreException {
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findTableStructureUsage(structureUsage, ipsProject);
    }

    public RenameRefactoring getRenameRefactoring() {
        return null;
    }

    public boolean isRenameRefactoringSupported() {
        return false;
    }

}
