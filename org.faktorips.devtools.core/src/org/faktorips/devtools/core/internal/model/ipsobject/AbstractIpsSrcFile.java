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

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsElement;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.util.StringUtil;

/**
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractIpsSrcFile extends IpsElement implements IIpsSrcFile {

    /**
     * @param parent
     * @param name
     */
    public AbstractIpsSrcFile(IIpsElement parent, String name) {
        super(parent, name);
    }

    /**
     * {@inheritDoc}
     */
    public IIpsPackageFragment getIpsPackageFragment() {
        return (IIpsPackageFragment)getParent();
    }
    
    /**
     * {@inheritDoc}
     */
    public IpsObjectType getIpsObjectType() {
        return IpsObjectType.getTypeForExtension(StringUtil.getFileExtension(name));
    }
    
    /**
     * {@inheritDoc}
     */
    public String getIpsObjecName() {
        String name = getName();
        int index = name.lastIndexOf('.');
        return name.substring(0, index); // index==-1 can never happen for ipssrcfiles, they have a file extension!
    }

    /** 
     * {@inheritDoc}
     */
    public IResource getCorrespondingResource() {
        return getCorrespondingFile();
    }
    
    /**
     * {@inheritDoc}
     */
    public QualifiedNameType getQualifiedNameType() {
        StringBuffer buf = new StringBuffer();
        String packageFragmentName = getIpsPackageFragment().getName();
        if(!StringUtils.isEmpty(packageFragmentName)){
            buf.append(getIpsPackageFragment().getName());
            buf.append('.');
        }
        
        buf.append(StringUtil.getFilenameWithoutExtension(getName()));
        return new QualifiedNameType(buf.toString(), getIpsObjectType());
    }

    /**
     * {@inheritDoc}
     */
    public IIpsElement[] getChildren() throws CoreException {
        if (isContentParsable()) {
            return new IIpsElement[]{getIpsObject()};
        }
        return new IIpsElement[0]; 
    }
    
    /** 
     * {@inheritDoc}
     */
    public IIpsObject getIpsObject() throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("Can't get ips object because file does not exist." + this)); //$NON-NLS-1$
        }
        return getContent().getIpsObject();
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValue(String name) throws CoreException {
        if (!exists()) {
            throw new CoreException(new IpsStatus("Can't get property value because file does not exist." + this)); //$NON-NLS-1$
        }
        return getContent(false).getRootPropertyValue(name);
    }
    
    protected IpsSrcFileContent getContent() {
        return getContent(true);
    }
    
    private IpsSrcFileContent getContent(boolean loadCompleteContent) {
        return ((IpsModel)getIpsModel()).getIpsSrcFileContent(this, loadCompleteContent);
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isContentParsable() throws CoreException {
        IpsSrcFileContent content = getContent();
        if (content==null) {
            return false;
        }
        return content.isParsable();
    }

    /** 
     * {@inheritDoc}
     */
    public Image getImage() {
        return IpsPlugin.getDefault().getImage("IpsSrcFile.gif"); //$NON-NLS-1$
    }



}
