/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import org.eclipse.core.runtime.PlatformObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.ui.actions.IpsAction;
import org.faktorips.devtools.core.ui.views.IpsElementDragListener;

/**
 * This abstract class is used to wrap an <code>IpsSrcFile</code> in another object. It provides
 * methods for getting the internal <code>IpsSrcFile</code>. You can use such a wrapper object to
 * put some additional information for viewing an source file object in an TreeViewer or
 * TableViewer. The components {@link IpsElementDragListener} and {@link IpsAction} are able to
 * handle instances of this wrapper as <code>IpsSrcFile</code>-Objects by calling the getIpsSrcFile
 * method.
 * <p>
 * IpsSrcFileViewItems are adaptable to IIpsSrcFile
 * 
 * @author dirmeier
 */
public abstract class IpsSrcFileViewItem extends PlatformObject implements IIpsSrcFileViewItem {

    private IIpsSrcFile ipsSrcFile;

    public IpsSrcFileViewItem(IIpsSrcFile ipsSrcFile) {
        this.ipsSrcFile = ipsSrcFile;
    }

    /**
     * To get the internal <code>IpsSrcFile</code> of this wrapper
     * 
     * @return the internal <code>IpsSrcFile</code>
     */
    @Override
    public IIpsSrcFile getIpsSrcFile() {
        return ipsSrcFile;
    }

    @Override
    public IIpsSrcFile getWrappedIpsSrcFile() {
        return getIpsSrcFile();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipsSrcFile == null) ? 0 : ipsSrcFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IpsSrcFileViewItem other = (IpsSrcFileViewItem)obj;
        if (ipsSrcFile == null) {
            if (other.ipsSrcFile != null) {
                return false;
            }
        } else if (!ipsSrcFile.equals(other.ipsSrcFile)) {
            return false;
        }
        return true;
    }

}
