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
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

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
			IProductCmptType pcType = pc.findOldProductCmptType();
			if (pcType == null) {
                // type can't be found, so extract the relation types from the generation
				return getRelationTypes(generation);
			} else {
			    return getRelationTypes(pcType);
            }
		} catch (CoreException e) {
            throw new RuntimeException("Error getting element ", e); //$NON-NLS-1$
		}
    }
    
    private String[] getRelationTypes(IProductCmptGeneration gen) {
        Set relationTypes = new HashSet();
        IProductCmptRelation[] relations = gen.getRelations();
        for (int i = 0; i < relations.length; i++) {
            relationTypes.add(relations[i].getProductCmptTypeRelation());
        }
        return (String[])relationTypes.toArray(new String[relationTypes.size()]);
    }

    private String[] getRelationTypes(IProductCmptType pcType) throws CoreException {
        Set typesHandled = new HashSet();
        List result = new ArrayList();
        while (pcType != null && !typesHandled.contains(pcType)) {
            IProductCmptTypeRelation[] relations = pcType.getRelations();
            int index = 0;
            for (int i = 0; i < relations.length; i++) {
                if (!relations[i].isAbstract() && !relations[i].isAbstractContainer()) {
                    // to get the relations of the supermost product component type
                    // put in the list as first, but with unchanged order for all relations
                    // found in one type...
                    result.add(index, relations[i].getName());
                    index++;
                }
            }
            typesHandled.add(pcType);
            pcType = pcType.findSupertype();
        }
        return (String[])result.toArray(new String[result.size()]);
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
        return generation.getRelations((String)parentElement);
	}

    /**
     * {@inheritDoc}
     */ 
	public Object getParent(Object element) {
		if (element instanceof String) {
			return generation;
		}
		if (element instanceof IProductCmptRelation) {
            IProductCmptRelation relation = (IProductCmptRelation)element; 
            return relation.getProductCmptTypeRelation();
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
}
