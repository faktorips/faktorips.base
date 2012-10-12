/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;

/**
 * Control to edit references to product component types.
 */
public class ProductCmptType2RefControl extends IpsObjectRefControl {

    private boolean excludeAbstractTypes;

    public ProductCmptType2RefControl(IIpsProject project, Composite parent, UIToolkit toolkit,
            boolean excludeAbstractTypes) {
        this(Arrays.asList(project), parent, toolkit, excludeAbstractTypes);
    }

    public ProductCmptType2RefControl(List<IIpsProject> projects, Composite parent, UIToolkit toolkit,
            boolean excludeAbstractTypes) {
        super(projects, parent, toolkit, Messages.ProductCmptTypeRefControl_title,
                Messages.ProductCmptTypeRefControl_description);
        this.excludeAbstractTypes = excludeAbstractTypes;
    }

    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {

        IIpsSrcFile[] allProductCmptTypes = findIpsSrcFilesByType(IpsObjectType.PRODUCT_CMPT_TYPE);

        if (!excludeAbstractTypes) {
            return allProductCmptTypes;
        }

        Set<IIpsSrcFile> filteredProductCmptTypes = new HashSet<IIpsSrcFile>();

        for (IIpsSrcFile ipsSrcFile : allProductCmptTypes) {
            if (!Boolean.valueOf(ipsSrcFile.getPropertyValue(IProductCmptType.PROPERTY_ABSTRACT))) {
                filteredProductCmptTypes.add(ipsSrcFile);
            }
        }

        return filteredProductCmptTypes.toArray(new IIpsSrcFile[filteredProductCmptTypes.size()]);
    }

    /**
     * Returns the product component type entered in this control. Returns <code>null</code> if the
     * text in the control does not identify a product component type. If abstract types are set to
     * be exclueded but the type found is abstract it is returned nevertheless!
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType() throws CoreException {
        return (IProductCmptType)findIpsObject(IpsObjectType.PRODUCT_CMPT_TYPE);
    }
}
