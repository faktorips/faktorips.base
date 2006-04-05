/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.views.attrtable;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IConfigElement;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;

public class AttributeContentProvider implements IStructuredContentProvider {

    public Object[] getElements(Object inputElement) {
        ArrayList result = new ArrayList();
        try {
            if (inputElement instanceof IPolicyCmptType) {
                IPolicyCmptType type = (IPolicyCmptType)inputElement;
                IIpsProject[] projects = IpsPlugin.getDefault().getIpsModel().getIpsProjects();
                
                for (int pi = 0; pi < projects.length; pi++) {
                    IProductCmpt[] products = (projects[pi].findProductCmpts(type.getQualifiedName(), true));
                    for (int i = 0; i < products.length; i++) {
                        ArrayList row = new ArrayList();
                        IProductCmptGeneration gen = (IProductCmptGeneration) products[i]
								.findGenerationEffectiveOn(IpsPlugin
										.getDefault().getIpsPreferences()
										.getWorkingDate());
                        
                        if (gen != null) {
                            row.add(products[i]);
                            IConfigElement[] attributes = gen.getConfigElements();
                            for (int j = 0; j < attributes.length; j++) {
                                row.add(attributes[j]);
                            }
                            
                            result.add(row.toArray());
                        }
                    }
                }
            }
            return result.toArray();
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
       
        return new Object[0];
    }

    public void dispose() {
        // nothing to do
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // nothing to do
    }

}
