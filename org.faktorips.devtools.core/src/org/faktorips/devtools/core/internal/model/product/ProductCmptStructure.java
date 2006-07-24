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

package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.RelationType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.product.IProductCmptSturctureReference;
import org.faktorips.devtools.core.model.product.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the product component structure
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptStructure implements IProductCmptStructure {
	private ProductCmptReference root;
	private GregorianCalendar workingDate;
	
	/**
	 * Creates a new ProductCmptStructure for the given product component and
	 * the user definde working date out of the IpsPreferences.
	 * 
	 * @param root The product component to create a structure for.
	 * @throws CycleException if a cycle is detected.
	 * @throws NullPointerException if the given product component is <code>null</code>.
	 */
	public ProductCmptStructure(IProductCmpt root) throws CycleException {
		this(root, IpsPlugin.getDefault().getIpsPreferences().getWorkingDate());
	}
	
	/**
	 * Creates a new ProductCmptStructure for the given product component and
	 * the given date.
	 * 
	 * @param root
	 *            The product component to create a structure for.
	 * @param date
	 *            The date the structure has to be valid for. That means that
	 *            the relations between the product components represented by
	 *            this structure are valid for the given date.
	 * @throws CycleException
	 *             if a cycle is detected.
	 * @throws NullPointerException
	 *             if the given product component is <code>null</code> or the
	 *             given date is <code>null</code>.
	 */
	public ProductCmptStructure(IProductCmpt root, GregorianCalendar date) throws CycleException {
		ArgumentCheck.notNull(root);
		ArgumentCheck.notNull(date);
		
        this.workingDate = date;

        this.root = buildNode(root, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public GregorianCalendar getValidAt() {
		return workingDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptReference getRoot() {
		return root;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void refresh() throws CycleException {
	    this.root = buildNode(root.getProductCmpt(), null);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptSturctureReference[] toArray(boolean productCmptOnly) {
		List result = new ArrayList();
		addChildrenToList(root, result, productCmptOnly);
		
		if (productCmptOnly) {
			return (IProductCmptReference[])result.toArray(new IProductCmptReference[result.size()]);
		}
		return (IProductCmptSturctureReference[])result.toArray(new IProductCmptSturctureReference[result.size()]);
	}
	
	/**
	 * Requests all children from the given parent and add them to the given list.
	 * 
	 * @param parent The parent to get the children from.
	 * @param list The list to add the children to.
	 * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>s.
	 */
	private void addChildrenToList(IProductCmptSturctureReference parent, List list, boolean productCmptOnly) {
		if (!productCmptOnly) {
			addChildrenToList(getChildProductCmptTypeRelationReferences(parent), list, productCmptOnly);
		}
		addChildrenToList(getChildProductCmptReferences(parent), list, productCmptOnly);
	}
	
	/**
	 * Adds all given references to the given list and requests the children for the added ones 
	 * revursively.
	 * @param children The array of child-references to add to the list.
	 * @param list The list to add the chlidren to.
	 * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>s.
	 */
	private void addChildrenToList(IProductCmptSturctureReference[] children, List list, boolean productCmptOnly) {
		for (int i = 0; i < children.length; i++) {
			list.add(children[i]);
			addChildrenToList(children[i], list, productCmptOnly);
		}
	}

	/**
     * Creates a new node for the given element and links it to the given parent-node.
     * 
     * @param element The IpsElement to be wrapped by the new node.
     * @param parent The parent-node for the new one.
	 * @throws CycleException 
     */
    private ProductCmptReference buildNode(IProductCmpt cmpt, ProductCmptStructureReference parent) throws CycleException {
    	ProductCmptReference node = new ProductCmptReference(this, parent, cmpt);
    	node.setChildren(buildChildNodes(cmpt, node));
    	return node;
    }

    /**
     * Creates child nodes for the given parent. The children are created out of the 
     * given relations.
     * 
     * @param relations The relations to create nodes for.
     * @param parent The parent for the new nodes
     * @return
     * @throws CycleException 
     */
    private ProductCmptStructureReference[] buildChildNodes(IProductCmptRelation[] relations, ProductCmptStructureReference parent, IProductCmptTypeRelation type) throws CycleException {
		ArrayList children = new ArrayList();
        for (int i = 0; i < relations.length; i ++) {
			try {
				IProductCmpt p = (IProductCmpt)relations[i].getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, relations[i].getTarget());
				if (p != null) {
					if(type.getRelationType().equals(RelationType.ASSOZIATION)){
						children.add(new ProductCmptReference(this, parent, p));
						continue;
					}
					children.add(buildNode(p, parent));
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
        }
        ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
		return (ProductCmptStructureReference[])children.toArray(result);
    }
    
    /**
     * Creates new child nodes for the given element and parent.
     * 
     * @param element The element the new children can be found in as relation-targets.
     * @param parent The parent node for the new children.
     * @throws CycleException 
     */
	private ProductCmptStructureReference[] buildChildNodes(IIpsElement element, ProductCmptStructureReference parent) throws CycleException {
		ArrayList children = new ArrayList();
		
		if (element instanceof IProductCmpt) {
			IProductCmpt cmpt = ((IProductCmpt)element);
			IProductCmptGeneration activeGeneration = (IProductCmptGeneration) cmpt
					.findGenerationEffectiveOn(workingDate);
			
			if (activeGeneration == null) {
				// no active generation found, so no nodes can be returned.
				return new ProductCmptStructureReference[0];
			}
			
			IProductCmptRelation[] relations = activeGeneration.getRelations();
			
			// Sort relations by type
			Hashtable mapping = new Hashtable();
			ArrayList typeList = new ArrayList();
			for (int i = 0; i < relations.length; i ++) {
				try {
					IProductCmptTypeRelation relationType = relations[i].findProductCmptTypeRelation();
					ArrayList relationsForType = (ArrayList)mapping.get(relationType.getName());
					
					if (relationsForType == null) {
						relationsForType = new ArrayList();
						mapping.put(relationType.getName(), relationsForType);
						typeList.add(relationType);
					}					
					relationsForType.add(relations[i]);
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			
			for (Iterator iter = typeList.iterator(); iter.hasNext();) {
				IProductCmptTypeRelation type = (IProductCmptTypeRelation) iter.next();
				ProductCmptStructureReference node = new ProductCmptTypeRelationReference(this, parent, type);
				ArrayList relationsList = (ArrayList)mapping.get(type.getName());
				IProductCmptRelation[] rels = new IProductCmptRelation[relationsList.size()];
				node.setChildren(buildChildNodes((IProductCmptRelation[])relationsList.toArray(rels), node, type));
				children.add(node);
			}
		}
		
		ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
		return (ProductCmptStructureReference[])children.toArray(result);
    }

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptReference getParentProductCmptReference(IProductCmptSturctureReference child) {
		ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
		ProductCmptStructureReference result = ref.getParent();
		if (result instanceof IProductCmptReference) {
			return (IProductCmptReference)result;
		}
		else if (result != null) {
			return (IProductCmptReference)result.getParent();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelationReference getParentProductCmptTypeRelationReference(IProductCmptSturctureReference child) {
		ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
		ProductCmptStructureReference result = ref.getParent();
		if (result instanceof IProductCmptTypeRelationReference) {
			return (IProductCmptTypeRelationReference)result;
		}
		else if (result != null) {
			return (IProductCmptTypeRelationReference)result.getParent();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptReference[] getChildProductCmptReferences(IProductCmptSturctureReference parent) {
		if (parent instanceof IProductCmptTypeRelationReference) {
			IProductCmptSturctureReference[] children = ((ProductCmptTypeRelationReference)parent).getChildren();
			IProductCmptReference[] result = new IProductCmptReference[children.length];
			System.arraycopy(children, 0, result, 0, children.length);
			return result;
		}
		else {
			ProductCmptStructureReference children[] = ((ProductCmptReference)parent).getChildren();
			ArrayList result = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				result.addAll(Arrays.asList(children[i].getChildren()));
			}
			return (IProductCmptReference[])result.toArray(new IProductCmptReference[result.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelationReference[] getChildProductCmptTypeRelationReferences(IProductCmptSturctureReference parent) {
		if (parent instanceof IProductCmptReference) {
			IProductCmptSturctureReference[] children = ((ProductCmptReference)parent).getChildren();
			IProductCmptTypeRelationReference[] result = new IProductCmptTypeRelationReference[children.length];
			System.arraycopy(children, 0, result, 0, children.length);
			return result;
		}
		else {
			ProductCmptStructureReference children[] = ((ProductCmptTypeRelationReference)parent).getChildren();
			ArrayList result = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				result.addAll(Arrays.asList(children[i].getChildren()));
			}
			return (IProductCmptTypeRelationReference[])result.toArray(new IProductCmptTypeRelationReference[result.size()]);
		}
	}   
}
