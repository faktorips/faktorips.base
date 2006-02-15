package org.faktorips.devtools.core.internal.model.product;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.product.IProductCmptStructure;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * {@inheritDoc}
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptStructure implements IProductCmptStructure {
	private Hashtable elementToNodeMapping;
	Node root;
	
	public ProductCmptStructure(IProductCmpt root) {
        this.elementToNodeMapping = new Hashtable();
        this.root = buildNode(root, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmpt[] getTargets(IProductCmptTypeRelation relationType, IProductCmpt cmpt) {
		Node node = (Node)elementToNodeMapping.get(cmpt);
		
		if (node == null) {
			return new IProductCmpt[0];
		}
		
		Node[] children = node.getChildren();
		IProductCmpt result[] = new IProductCmpt[children.length];
		for (int i = 0; i < children.length; i++) {
			result[i] = (IProductCmpt)children[i].getWrappedElement();
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptTypeRelation[] getRelationTypes(IProductCmpt cmpt) {
		Node node = (Node)elementToNodeMapping.get(cmpt);
		
		if (node == null) {
			return new IProductCmptTypeRelation[0];
		}
		
		Node[] children = node.getChildren();
		IProductCmptTypeRelation result[] = new IProductCmptTypeRelation[children.length];
		for (int i = 0; i < children.length; i++) {
			result[i] = (IProductCmptTypeRelation)children[i].getWrappedElement();
		}
		return result;
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
	public IIpsObjectPartContainer[] getChildren(IIpsObjectPartContainer parent) {
		Node node = (Node)elementToNodeMapping.get(parent);
		if (node != null) {
			Node[] children = node.getChildren();
			IIpsObjectPartContainer[] result = new IIpsObjectPartContainer[children.length];
			for (int i = 0; i < children.length; i++) {
				result[i] = (IIpsObjectPartContainer)children[i].getWrappedElement();
			}
			return result;
		}
		return new IIpsObjectPartContainer[0];
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh() {
	    this.elementToNodeMapping = new Hashtable();
	    this.root = buildNode(root.getWrappedElement(), null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPartContainer getParent(IIpsObjectPartContainer child) {
		Node node = (Node)elementToNodeMapping.get(child);
		if (node != null) {
			return (IIpsObjectPartContainer)node.getParent().getWrappedElement();
		}
		return null;
	}

	/**
     * Creates a new node for the given element and links it to the given parent-node.
     * 
     * @param element The IpsElement to be wrapped by the new node.
     * @param parent The parent-node for the new one.
     */
    private Node buildNode(IIpsElement element, Node parent) {
    	Node node = new Node(element, parent);
    	node.setChildren(buildChildNodes(element, node));
    	elementToNodeMapping.put(element, node);
    	return node;
    }

    /**
     * Creates child nodes for the given parent. The children are created out of the 
     * given relations.
     * 
     * @param relations The relations to create nodes for.
     * @param parent The parent for the new nodes
     * @return
     */
    private Node[] buildChildNodes(IProductCmptRelation[] relations, Node parent) {
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
		Node[] result = new Node[children.size()];
		return (Node[])children.toArray(result);
    }
    
    /**
     * Creates new child nodes for the given element and parent.
     * 
     * @param element The element the new children can be found in as relation-targets.
     * @param parent The parent node for the new children.
     */
	private Node[] buildChildNodes(IIpsElement element, Node parent) {
		ArrayList children = new ArrayList();
		
		if (element instanceof IProductCmpt) {
			IProductCmpt cmpt = ((IProductCmpt)element);
			IProductCmptGeneration activeGeneration = (IProductCmptGeneration)cmpt.findGenerationEffectiveOn(IpsPreferences.getWorkingDate());
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
				Node node = new Node(type, parent);
				ArrayList relationsList = (ArrayList)mapping.get(type.getName());
				IProductCmptRelation[] rels = new IProductCmptRelation[relationsList.size()];
				node.setChildren(buildChildNodes((IProductCmptRelation[])relationsList.toArray(rels), node));
				children.add(node);
				elementToNodeMapping.put(type, node);
			}
		}
		
		Node[] result = new Node[children.size()];
		return (Node[])children.toArray(result);
    }   

	/**
	 * Class to allow the content provider to evaluate the structure of the data to display once and
	 * cache this information using this class.
	 * 
	 * @author Thorsten Guenther
	 */
	private  class Node {
		private Node[] children;
		private Node parent;
		private IIpsElement wrapped;
		
		public Node(IIpsElement wrapped, Node parent) {
			this.parent = parent;
			this.wrapped = wrapped;
		}
		
		public Node getParent() {
			return parent;
		}
		
		public Node[] getChildren() {
			return children;
		}
		
		public void setChildren(Node[] children) {
			this.children = children;
		}
		
		public IIpsElement getWrappedElement() {
			return wrapped;
		}		
	}
}
