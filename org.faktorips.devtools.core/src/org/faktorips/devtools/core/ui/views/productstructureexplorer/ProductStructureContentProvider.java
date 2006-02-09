package org.faktorips.devtools.core.ui.views.productstructureexplorer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsPreferences;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;

/**
 * Provides the elements of the FaktorIps-Model for the department.
 * 
 * @author Thorsten Guenther
 */
public class ProductStructureContentProvider implements ITreeContentProvider {
	
	/**
	 * The root-node this content provider starts to evaluate the content.
	 */
	private Node root;
	
	/**
	 * Flag to tell the content provider to show (<code>true</code>) or not to show the
	 * Relation-Type as Node.
	 */
	private boolean fShowRelationType = false;
	
	/**
	 * Creates a new content provider.
	 * 
	 * @param showRelationType <code>true</code> to show the relation types as nodes.
	 */
	public ProductStructureContentProvider(boolean showRelationType) {
		this.fShowRelationType = showRelationType;
	}
	
    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Node) {
        	return ((Node)parentElement).getChildren();
        }
        return new Object[0];
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
    	if (element instanceof Node) {
    		return ((Node)element).getParent();
    	}
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        if (element instanceof Node) {
        	return ((Node)element).getChildren().length > 0;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void dispose() {
    	root = null;
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
    	if (root == null && inputElement instanceof IProductCmpt) {
            root = buildNode((IProductCmpt)inputElement, null);    		
            return new Object[] {root};
    	}
        if (root != null && inputElement.equals(root.getWrappedElement())) {
            return new Object[] {root};
        }
        else {
            return new Object[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null || !(newInput instanceof IProductCmpt)) {
        	root = null;
            return;
        }
        
        root = buildNode((IProductCmpt)newInput, null);
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
				children.add(buildNode(p, parent));
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

	        if (fShowRelationType) {
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
	        	}
	        }
	        else {
	        	return buildChildNodes(relations, parent);
	        }
		}
		
		Node[] result = new Node[children.size()];
		return (Node[])children.toArray(result);
    }   
}
