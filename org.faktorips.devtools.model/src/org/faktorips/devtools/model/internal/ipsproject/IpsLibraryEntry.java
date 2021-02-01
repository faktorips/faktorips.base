/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.ipsproject;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsLibraryEntry;
import org.faktorips.devtools.model.ipsproject.IIpsStorage;
import org.faktorips.devtools.model.plugin.IpsLog;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class IpsLibraryEntry extends IpsObjectPathEntry implements IIpsLibraryEntry {

    private LibraryIpsPackageFragmentRoot packageFragmentRoot;

    public IpsLibraryEntry(IpsObjectPath ipsObjectPath) {
        super(ipsObjectPath);
    }

    protected abstract IIpsStorage getIpsStorage();

    protected abstract IIpsSrcFile getIpsSrcFile(QualifiedNameType qnt) throws CoreException;

    @Override
    public IIpsSrcFile findIpsSrcFile(QualifiedNameType nameType) {
        if (getIpsStorage() == null || nameType == null) {
            return null;
        } else if (getIpsStorage().contains(nameType.toPath())) {
            return getIpsPackageFragmentRoot().getIpsPackageFragment(nameType.getPackageName()).getIpsSrcFile(
                    nameType.getFileName());
        } else {
            return null;
        }
    }

    @Override
    public Element toXml(Document doc) {
        Element element = super.toXml(doc);
        element.setAttribute(XML_ATTRIBUTE_TYPE, getType());
        element.setAttribute(getXmlAttributePathName(), getXmlPathRepresentation());
        return element;
    }

    @Override
    public void initFromXml(Element element, IProject project) {
        super.initFromXml(element, project);
        String path = element.getAttribute(getXmlAttributePathName());
        try {
            if (StringUtils.isEmpty(path)) {
                initStorage(null);
            } else {
                initStorage(new Path(path));
            }
        } catch (IOException e) {
            IpsLog.log(e);
        }
    }

    protected abstract String getXmlPathRepresentation();

    protected abstract String getXmlAttributePathName();

    @Override
    public abstract void initStorage(IPath path) throws IOException;

    @Override
    public LibraryIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return packageFragmentRoot;
    }

    protected void setIpsPackageFragmentRoot(LibraryIpsPackageFragmentRoot packageFragmentRoot) {
        this.packageFragmentRoot = packageFragmentRoot;
    }

}