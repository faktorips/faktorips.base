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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsSrcFileCollection;
import org.faktorips.devtools.core.ui.IpsSrcFileViewItem;

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
     * @param ipsSrcFile The IpsSrcFile represented by this viewer item
     */
    private InstanceIpsSrcFileViewItem(IIpsSrcFile ipsSrcFile, IpsSrcFileCollection collection) {
        super(ipsSrcFile);
        this.collection = collection;
    }

    /**
     * Get the name of defining meta class
     * 
     * @return the name of the meta class, defining the internal source file
     */
    public String getDefiningMetaClass() {
        try {
            return collection.getDefiningMetaClass(getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @return the true if duplicateName is set
     */
    public boolean isDuplicateName() {
        try {
            return collection.isDuplicateName(getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns <code>true</code> if the user has searched for instances of a type and the ips object
     * identified by this item is an instance of a subtype of this type. Returns <code>false</code>
     * otherwise.
     */
    public boolean isInstanceOfMetaClass() {
        try {
            return collection.isInstanceOfMetaClass(getIpsSrcFile());
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

}
