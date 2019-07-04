/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.ipsobject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.testcase.ITestCase;

/**
 * Collection of {@link IIpsSrcFile}s - able to answer some questions about single items only useful
 * in relation with all other items of this collection.
 */
public class IpsSrcFileCollection {

    private Map<IIpsSrcFile, CollectionItem> collection = new HashMap<IIpsSrcFile, CollectionItem>();

    /**
     * Create a new collection based on the given {@link IIpsSrcFile}s. Using this constructor is
     * the same as calling {@link #IpsSrcFileCollection(Collection, IIpsMetaClass)} with
     * {@link IIpsMetaClass} as <code>null</code>.
     * 
     * @param srcFiles The {@link IIpsSrcFile}s to use for this collection.
     */
    public IpsSrcFileCollection(Collection<IIpsSrcFile> srcFiles) {
        this(srcFiles, null);
    }

    /**
     * Create a new collection based on the given {@link IIpsSrcFile}s.
     * 
     * @param metaObjectsSrcFiles The {@link IIpsSrcFile}s to use for this collection.
     * @param metaClass The {@link IIpsMetaClass} to check the source files against. Can be
     *            <code>null</code>, in this case the method
     *            {@link #isInstanceOfMetaClass(IIpsSrcFile)} will always return false;
     */
    public IpsSrcFileCollection(Collection<IIpsSrcFile> metaObjectsSrcFiles, IIpsMetaClass metaClass) {
        initItems(metaObjectsSrcFiles, metaClass);
    }

    /**
     * Get the name of defining meta class
     * 
     * @return the name of the meta class, defining the given source file
     * @throws CoreException If the given {@link IIpsSrcFile} is not part of this collection.
     */
    public String getDefiningMetaClass(IIpsSrcFile srcFile) throws CoreException {
        return getItem(srcFile).getDefiningMetaClass();
    }

    /**
     * 
     * @return <code>true</code> if the unqualified name of this src file is at least used by one
     *         other {@link IIpsSrcFile} in this collection.
     * @throws CoreException If the given {@link IIpsSrcFile} is not part of this collection.
     */
    public boolean isDuplicateName(IIpsSrcFile srcFile) throws CoreException {
        return getItem(srcFile).isDuplicateName();
    }

    /**
     * Returns <code>true</code> if the user has searched for instances of a type and the ips object
     * identified by this item is an instance of a sub type of this type. Returns <code>false</code>
     * otherwise.
     * 
     * @throws CoreException If the given {@link IIpsSrcFile} is not part of this collection.
     */
    public boolean isInstanceOfMetaClass(IIpsSrcFile srcFile) throws CoreException {
        return getItem(srcFile).isInstanceOfMetaClass();
    }

    private CollectionItem getItem(IIpsSrcFile srcFile) throws CoreException {
        CollectionItem item = collection.get(srcFile);
        if (item == null) {
            throw new CoreException(new IpsStatus("The given source file " + srcFile //$NON-NLS-1$
                    + " is not part of this collection")); //$NON-NLS-1$
        }

        return item;
    }

    /**
     * Creates an item for each IPS source file and marks the items as duplicate, if two (or more)
     * IPS source files have the same unqualified name.
     */
    private void initItems(Collection<IIpsSrcFile> metaObjectsSrcFiles, IIpsMetaClass baseMetaClass) {

        String metaClassName = null;
        if (baseMetaClass != null) {
            metaClassName = baseMetaClass.getQualifiedName();
        }

        Map<String, CollectionItem> itemsByName = new HashMap<String, CollectionItem>();
        for (IIpsSrcFile file : metaObjectsSrcFiles) {
            CollectionItem item = itemsByName.get(file.getName());
            CollectionItem newItem = new CollectionItem();
            collection.put(file, newItem);
            newItem.setDefiningMetaClass(getMetaClassName(file));
            if (item == null) {
                newItem.setDuplicateName(false);
                itemsByName.put(file.getName(), newItem);
            } else {
                newItem.setDuplicateName(true);
                item.setDuplicateName(true);
            }
            if (metaClassName != null) {
                newItem.setInstanceOfMetaClass(metaClassName.equals(newItem.getDefiningMetaClass()));
            }
        }
    }

    /**
     * To get the name of the meta class defining the internal source file.
     * 
     * @return the meta class name of the internal source file
     */
    private String getMetaClassName(IIpsSrcFile srcFile) {
        if (srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_CMPT)
                || srcFile.getIpsObjectType().equals(IpsObjectType.PRODUCT_TEMPLATE)) {
            return srcFile.getPropertyValue(IProductCmpt.PROPERTY_PRODUCT_CMPT_TYPE);
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.ENUM_CONTENT)) {
            return srcFile.getPropertyValue(IEnumContent.PROPERTY_ENUM_TYPE);
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.TABLE_CONTENTS)) {
            return srcFile.getPropertyValue(ITableContents.PROPERTY_TABLESTRUCTURE);
        } else if (srcFile.getIpsObjectType().equals(IpsObjectType.TEST_CASE_TYPE)) {
            return srcFile.getPropertyValue(ITestCase.PROPERTY_TEST_CASE_TYPE);
        } else {
            return null;
        }
    }

    private class CollectionItem {

        private String definingMetaClass;

        private boolean duplicateName = false;

        private boolean instanceOfMetaClass = false;

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
         * Returns <code>true</code> if the user has searched for instances of a type and the ips
         * object identified by this item is an instance of a subtype of this type. Returns
         * <code>false</code> otherwise.
         */
        public boolean isInstanceOfMetaClass() {
            return instanceOfMetaClass;
        }

        /**
         * @see #isInstanceOfMetaClass()
         */
        public void setInstanceOfMetaClass(boolean instanceOfMetaClass) {
            this.instanceOfMetaClass = instanceOfMetaClass;
        }

    }

}
