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
package org.faktorips.devtools.core.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.IpsSrcFileProvider;

/**
 * 
 * A <code>IpsSrcFile</code>-Wrapper to add some additional information to the
 * <code>IpsSrcFile</code>
 * 
 * @author dirmeier
 * 
 */
public class IpsSrcFileViewItem extends IpsSrcFileProvider {

    /**
     * Creates an item for each ips source file and marks the itens as duplicate, if two (or more)
     * ips source files have the same unqualified name.
     * 
     * @throws CoreException
     * @throws NullPointerException if files is <code>null</code>
     */
    public static final IpsSrcFileViewItem[] createItems(IIpsSrcFile[] files, IIpsMetaClass baseMetaClass)
            throws CoreException {

        IpsSrcFileViewItem[] items = new IpsSrcFileViewItem[files.length];
        Map<String, IpsSrcFileViewItem> itemsByName = new HashMap<String, IpsSrcFileViewItem>();
        for (int i = 0; i < files.length; i++) {
            IpsSrcFileViewItem item = itemsByName.get(files[i].getName());
            IpsSrcFileViewItem newItem = new IpsSrcFileViewItem(files[i]);
            items[i] = newItem;
            newItem.setDefiningMetaClass(getMetaClassName(files[i]));
            if (item == null) {
                newItem.setDuplicateName(false);
                itemsByName.put(files[i].getName(), newItem);
            } else {
                newItem.setDuplicateName(true);
                item.setDuplicateName(true);
            }
            if (baseMetaClass != null) {
                newItem.setInstanceOfSubtype(!baseMetaClass.getQualifiedName().equals(getMetaClassName(files[i])));
            }
        }
        return items;
    }

    /**
     * To get the name of the meta class defining the internal source file. At the moment this only
     * is implemented for <code>ProductCmpt</code> and <code>EnumContent</code>.
     * 
     * @return the meta class name of the internal source file
     * @throws CoreException
     */
    private static String getMetaClassName(IIpsSrcFile srcFile) throws CoreException {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)) {
            return srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.ENUM_CONTENT)) {
            return srcFile.getPropertyValue(IEnumContent.PROPERTY_ENUM_TYPE);
        } else {
            return null;
        }
    }

    /**
     * @param ipsSrcFile The IpsSrcFile represented by this viewer item
     */
    public IpsSrcFileViewItem(IIpsSrcFile ipsSrcFile) {
        super(ipsSrcFile);
    }

    private String definingMetaClass;

    private boolean duplicateName = false;
    private boolean instanceOfSubtype = false;

    /**
     * Get the name of defining meta class
     * 
     * @return the name of the meta class, defining the internal source file
     */
    public String getDefiningMetaClass() {
        return definingMetaClass;
    }

    /**
     * Set the name of the meta class defining the internal source file
     * 
     * @param definingMetaClass the name of the meta class
     */
    public void setDefiningMetaClass(String definingMetaClass) {
        this.definingMetaClass = definingMetaClass;
    }

    /**
     * 
     * @return the true if duplicateName is set
     */
    public boolean isDuplicateName() {
        return duplicateName;
    }

    /**
     * Set whether this item represents a source file that's name is already present
     * 
     * @param duplicateName the duplicateName to set
     */
    public void setDuplicateName(boolean duplicateName) {
        this.duplicateName = duplicateName;
    }

    /**
     * Returns <code>true</code> if the user has searched for instances of a type and the ips object
     * identified by this item is an instance of a subtype of this type. Returns <code>false</code>
     * otherwise.
     */
    public boolean isInstanceOfSubtype() {
        return instanceOfSubtype;
    }

    /**
     * @see #isInstanceOfSubtype()
     */
    public void setInstanceOfSubtype(boolean instanceOfSubtype) {
        this.instanceOfSubtype = instanceOfSubtype;
    }

}
