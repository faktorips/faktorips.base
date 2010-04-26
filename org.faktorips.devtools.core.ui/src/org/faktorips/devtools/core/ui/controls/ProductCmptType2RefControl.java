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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
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
        super(project, parent, toolkit, Messages.ProductCmptTypeRefControl_title,
                Messages.ProductCmptTypeRefControl_description);
        this.excludeAbstractTypes = excludeAbstractTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject() == null) {
            return new IIpsSrcFile[0];
        }
        IIpsSrcFile[] allProductCmptTypes = getIpsProject().findIpsSrcFiles(IpsObjectType.PRODUCT_CMPT_TYPE);
        ArrayList<IIpsSrcFile> result = new ArrayList<IIpsSrcFile>();
        for (int i = 0; i < allProductCmptTypes.length; i++) {
            if (!excludeAbstractTypes
                    || !Boolean.valueOf(allProductCmptTypes[i].getPropertyValue(IProductCmptType.PROPERTY_ABSTRACT))
                            .booleanValue()) {
                result.add(allProductCmptTypes[i]);
            }
        }
        return result.toArray(new IIpsSrcFile[result.size()]);
    }

    /**
     * Returns the product component type entered in this control. Returns <code>null</code> if the
     * text in the control does not identify a product component type. If abstract types are set to
     * be exclueded but the type found is abstract it is returned nevertheless!
     * 
     * @throws CoreException if an exception occurs while searching for the type.
     */
    public IProductCmptType findProductCmptType() throws CoreException {
        IIpsProject project = getIpsProject();
        if (project != null) {
            return (IProductCmptType)project.findProductCmptType(getText());
        }
        return null;
    }
}
