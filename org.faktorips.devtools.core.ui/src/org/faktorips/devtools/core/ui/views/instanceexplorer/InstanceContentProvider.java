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

package org.faktorips.devtools.core.ui.views.instanceexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsMetaClass;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;

/**
 * The content provider for the instance explorer
 * 
 * @author dirmeier
 *
 */
public class InstanceContentProvider implements IStructuredContentProvider {

    protected static final IIpsSrcFile[] EMPTY_ARRAY = new IIpsSrcFile[0];
    
    /*
     * indicates wether to search the subtypes for an instance or not
     */
    private boolean subTypeSearch = true;
    
    protected boolean isSubTypeSearch() {
		return subTypeSearch;
	}

    protected void setSubTypeSearch(boolean subTypeSearch) {
		this.subTypeSearch = subTypeSearch;
	}
    
    /**
     * {@inheritDoc}
     */
	public Object[] getElements(Object inputElement) {
		try {
			if (inputElement instanceof IIpsMetaClass) {
				IIpsMetaClass ipsMetaClass = (IIpsMetaClass) inputElement;
				IIpsSrcFile[] metaObjectsSrcFiles = ipsMetaClass.findAllMetaObjectSrcFiles(ipsMetaClass.getIpsProject(), subTypeSearch);
				InstanceViewerItem[] items = new InstanceViewerItem[metaObjectsSrcFiles.length];
				int i = 0;
				for (IIpsSrcFile srcFile : metaObjectsSrcFiles) {
					try {
						items[i] = new InstanceViewerItem(srcFile);
						if (i > 0) {
							if (items[i-1].getIpsSrcFile().getName().equals(items[i].getIpsSrcFile().getName())) {
								items[i-1].setDuplicateName(true);
								items[i].setDuplicateName(true);
							}
						}
						String defMetaClassName = getMetaClassName(srcFile);
						if (defMetaClassName != null && !ipsMetaClass.getQualifiedName().equals(defMetaClassName)) {
							items[i].setDefiningMetaClass(defMetaClassName);
						}
					} finally {
						i++;
					}
				}
				return items;
			} else {
	            return EMPTY_ARRAY;
			}
		} catch (CoreException e) {
            IpsPlugin.log(e);
            return EMPTY_ARRAY;
		}
	}

	public void dispose() {
		
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}

	/**
	 * To get the name of the meta class defining the internal source file. At the moment this only is implemented for 
	 * <code>ProductCmpt</code> and <code>EnumContent</code>.
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
}
