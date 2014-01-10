/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsArchiveEditorInput extends PlatformObject implements IEditorInput {

    private IIpsSrcFile srcFile;

    public IpsArchiveEditorInput(IIpsSrcFile fileFromArchive) {
        ArgumentCheck.notNull(fileFromArchive);
        srcFile = fileFromArchive;
    }

    /**
     * Returns the ips source file this is an editor input from. The source file comes from an ips
     * archive.
     */
    public IIpsSrcFile getIpsSrcFile() {
        return srcFile;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
        return registry.getImageDescriptor(StringUtil.getFileExtension(srcFile.getName()));
    }

    @Override
    public String getName() {
        return srcFile.getName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return srcFile.getQualifiedNameType().getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((srcFile == null) ? 0 : srcFile.hashCode());
        return result;
    }

    /**
     * returns the equality of the underlying <code>IpsSrcFile</code> resources. Note: the default
     * UI functionality uses this method to decide if a new editor should be opened or an already
     * open editor will be reactivated.
     */
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
        IpsArchiveEditorInput other = (IpsArchiveEditorInput)obj;
        if (srcFile == null) {
            if (other.srcFile != null) {
                return false;
            }
        } else if (!srcFile.equals(other.srcFile)) {
            return false;
        }
        return true;
    }

}
