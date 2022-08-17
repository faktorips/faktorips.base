/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views;

import java.util.Collection;

import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;
import org.faktorips.devtools.model.IIpsMetaClass;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsSrcFileCollection;

/**
 * 
 * A <code>IpsSrcFile</code>-Wrapper to add some additional information to the
 * <code>IpsSrcFile</code>
 * 
 * @author dirmeier
 * 
 */
public class InstanceIpsSrcFileViewItem extends IpsSrcFileViewItem {

    private IpsSrcFileCollection collection;

    /**
     * @param ipsSrcFile The IpsSrcFile represented by this viewer item
     */
    private InstanceIpsSrcFileViewItem(IIpsSrcFile ipsSrcFile, IpsSrcFileCollection collection) {
        super(ipsSrcFile);
        this.collection = collection;
    }

    /**
     * Creates an item for each ips source file and marks the items as duplicate, if two (or more)
     * ips source files have the same unqualified name.
     * 
     * @throws NullPointerException if files is <code>null</code>
     */
    public static final InstanceIpsSrcFileViewItem[] createItems(Collection<IIpsSrcFile> metaObjectsSrcFiles,
            IIpsMetaClass baseMetaClass) {
        IpsSrcFileCollection collection = new IpsSrcFileCollection(metaObjectsSrcFiles, baseMetaClass);
        InstanceIpsSrcFileViewItem[] items = new InstanceIpsSrcFileViewItem[metaObjectsSrcFiles.size()];
        int i = 0;
        for (IIpsSrcFile srcFile : metaObjectsSrcFiles) {
            InstanceIpsSrcFileViewItem newItem = new InstanceIpsSrcFileViewItem(srcFile, collection);
            items[i] = newItem;
            i++;
        }
        return items;
    }

    /**
     * Get the name of defining meta class
     * 
     * @return the name of the meta class, defining the internal source file
     */
    public String getDefiningMetaClass() {
        return collection.getDefiningMetaClass(getIpsSrcFile());
    }

    /**
     * 
     * @return the true if duplicateName is set
     */
    public boolean isDuplicateName() {
        return collection.isDuplicateName(getIpsSrcFile());
    }

    /**
     * Returns <code>true</code> if the user has searched for instances of a type and the ips object
     * identified by this item is an instance of a subtype of this type. Returns <code>false</code>
     * otherwise.
     */
    public boolean isInstanceOfMetaClass() {
        return collection.isInstanceOfMetaClass(getIpsSrcFile());
    }

}
