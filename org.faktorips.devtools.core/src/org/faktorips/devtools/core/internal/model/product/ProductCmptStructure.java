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
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.CycleException;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the product component structure
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptStructure implements IProductCmptStructure {
	private Hashtable elementToNodeMapping;
	private StructureNode root;
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
		
        this.elementToNodeMapping = new Hashtable();
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
	public IProductCmpt getRoot() {
		return (IProductCmpt)root.getWrappedElement();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IStructureNode getRootNode() {
		return root;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void refresh() throws CycleException {
	    this.elementToNodeMapping = new Hashtable();
	    this.root = buildNode(root.getWrappedElement(), null);
	}

	/**
	 * {@inheritDoc}
	 */
	public IStructureNode[] toArray(boolean prodcutCmptOnly) {
		List result = new ArrayList();
		addChildrenToList(root, result, prodcutCmptOnly);
		return (IStructureNode[])result.toArray(new IStructureNode[result.size()]);
	}
	
	private void addChildrenToList(IStructureNode parent, List list, boolean productCmptOnly) {
		IStructureNode[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			if ((productCmptOnly && children[i].getWrappedElement() instanceof IProductCmpt) || !productCmptOnly) {
				list.add(children[i]);
			}
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
    private StructureNode buildNode(IIpsElement element, StructureNode parent) throws CycleException {
    	StructureNode node = new StructureNode(element, parent);
    	node.setChildren(buildChildNodes(element, node));
		this.elementToNodeMapping.put(element, node);

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
    private StructureNode[] buildChildNodes(IProductCmptRelation[] relations, StructureNode parent) throws CycleException {
		ArrayList children = new ArrayList();
        for (int i = 0; i < relations.length; i ++) {
			try {
				IProductCmpt p = (IProductCmpt)relations[i].getIpsProject().findIpsObject(IpsObjectType.PRODUCT_CMPT, relations[i].getTarget());
				if (p != null) {
					children.add(buildNode(p, parent));
				}
			} catch (CoreException e) {
				IpsPlugin.log(e);
			}
        }
		StructureNode[] result = new StructureNode[children.size()];
		return (StructureNode[])children.toArray(result);
    }
    
    /**
     * Creates new child nodes for the given element and parent.
     * 
     * @param element The element the new children can be found in as relation-targets.
     * @param parent The parent node for the new children.
     * @throws CycleException 
     */
	private StructureNode[] buildChildNodes(IIpsElement element, StructureNode parent) throws CycleException {
		ArrayList children = new ArrayList();
		
		if (element instanceof IProductCmpt) {
			IProductCmpt cmpt = ((IProductCmpt)element);
			IProductCmptGeneration activeGeneration = (IProductCmptGeneration) cmpt
					.findGenerationEffectiveOn(workingDate);
			
			if (activeGeneration == null) {
				// no active generation found, so no nodes can be returned.
				return new StructureNode[0];
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
				StructureNode node = new StructureNode(type, parent);
				ArrayList relationsList = (ArrayList)mapping.get(type.getName());
				IProductCmptRelation[] rels = new IProductCmptRelation[relationsList.size()];
				node.setChildren(buildChildNodes((IProductCmptRelation[])relationsList.toArray(rels), node));
				children.add(node);
				this.elementToNodeMapping.put(type, node);
			}
		}
		
		StructureNode[] result = new StructureNode[children.size()];
		return (StructureNode[])children.toArray(result);
    }   
	
	/**
	 * Class to allow the content provider to evaluate the structure of the data to display once and
	 * cache this information using this class.
	 * 
	 * @author Thorsten Guenther
	 */
	public class StructureNode implements IProductCmptStructure.IStructureNode{
		private StructureNode[] children;
		private StructureNode parent;
		private IIpsElement wrapped;
		
		/**
		 * Creates a new Node.
		 */
		StructureNode(IIpsElement wrapped, StructureNode parent) throws CycleException {
			this.parent = parent;
			this.wrapped = wrapped;
			detectCycle(new ArrayList());
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IStructureNode getParent() {
			return parent;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IStructureNode[] getChildren() {
			return children;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public IIpsElement getWrappedElement() {
			return wrapped;
		}		
		
		void setChildren(StructureNode[] children) {
			this.children = children;
		}
		
		private void detectCycle(ArrayList seenElements) throws CycleException {
			if (!(wrapped instanceof IProductCmptTypeRelation) && seenElements.contains(wrapped)) {
				seenElements.add(wrapped);
				throw new CycleException((IIpsElement[])seenElements.toArray(new IIpsElement[seenElements.size()]));
			}
			else {
				seenElements.add(wrapped);
				if (parent != null) {
					parent.detectCycle(seenElements);
				}
			}
		}
		
		/**
		 * {@inheritDoc}
		 */
		public boolean equals(Object o) {
			if (!(o instanceof StructureNode)) {
				return false;
			}
			StructureNode other = (StructureNode)o;
			return ((children == null && other.children == null) || (children != null && children
					.equals(other.children)))
					&& ((parent == null && other.parent == null) || (parent != null && parent
							.equals(other.parent)))
					&& ((wrapped == null && other.wrapped == null) || (wrapped != null && wrapped
							.equals(other.wrapped)));
		}
	}
}
