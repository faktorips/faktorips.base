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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;

/**
 * Provides the content for a generation-based relations-tree. The relations-types are
 * requested from the given generation and all supertypes the type containing this generation
 * is based on.
 * 
 * @author Thorsten Guenther
 */
public class RelationsContentProvider implements ITreeContentProvider {

	private IProductCmptGeneration generation;
	
    /**
     * {@inheritDoc}
     */ 
    public Object[] getElements(Object inputElement) {
    	if (!(inputElement instanceof IProductCmptGeneration)) {
            throw new RuntimeException("Unkown input element type " + inputElement.getClass()); //$NON-NLS-1$
        }
		IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
		try {
			IProductCmpt pc = generation.getProductCmpt();
			IProductCmptType pcType = pc.findProductCmptType(generation.getIpsProject());
			if (pcType == null) {
                // type can't be found, so extract the relation types from the generation
				return getAssociationNames(generation);
			} else {
			    return getAssociationNames(pcType);
            }
		} catch (CoreException e) {
            throw new RuntimeException("Error getting element ", e); //$NON-NLS-1$
		}
    }
    
    private String[] getAssociationNames(IProductCmptGeneration gen) {
        Set associations = new HashSet();
        IProductCmptLink[] links = gen.getLinks();
        for (int i = 0; i < links.length; i++) {
            associations.add(links[i].getAssociation());
        }
        return (String[])associations.toArray(new String[associations.size()]);
    }

    private String[] getAssociationNames(IProductCmptType type) throws CoreException {
        NoneDerivedAssociationsCollector collector = new NoneDerivedAssociationsCollector(type.getIpsProject());
        collector.start(type);
        return (String[])collector.associations.toArray(new String[collector.associations.size()]);
    }
    
    /**
     * {@inheritDoc}
     */ 
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */ 
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    	if (newInput instanceof IProductCmptGeneration) {
    		generation = (IProductCmptGeneration)newInput;
    	}
    	else {
    		generation = null;
    	}
    }

    /**
     * {@inheritDoc}
     */ 
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof String) || generation == null) {
            return null;
		}
        return generation.getLinks((String)parentElement);
	}

    /**
     * {@inheritDoc}
     */ 
	public Object getParent(Object element) {
		if (element instanceof String) {
			return generation;
		}
		if (element instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)element; 
            return link.getAssociation();
		}
        throw new RuntimeException("Unknown element type " + element);  //$NON-NLS-1$ 
	}

    /**
     * {@inheritDoc}
     */ 
	public boolean hasChildren(Object element) {
		Object[] children = getChildren(element);
		if (children==null) {
			return false;
		}
		return children.length > 0;
	}
    
    class NoneDerivedAssociationsCollector extends ProductCmptTypeHierarchyVisitor {

        private List associations = new ArrayList();
        
        public NoneDerivedAssociationsCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        protected boolean visit(IProductCmptType currentType) throws CoreException {
            IProductCmptTypeAssociation[] typeAssociations = currentType.getAssociations();
            int index = 0;
            for (int i = 0; i < typeAssociations.length; i++) {
                // to get the assocations of the root type of the supertype hierarchy first,
                // put in the list at first, but with unchanged order for all associations
                // found in one type...
                if (!typeAssociations[i].isDerived()) {
                    associations.add(index, typeAssociations[i].getName());
                    index++;
                }
            }
            return false;
        }
        
    }
}
