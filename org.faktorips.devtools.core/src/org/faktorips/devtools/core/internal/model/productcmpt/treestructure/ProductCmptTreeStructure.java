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

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.util.ArgumentCheck;

/**
 * Implementation of the product component structure
 * 
 * @author Thorsten Guenther
 */
public class ProductCmptTreeStructure implements IProductCmptTreeStructure {
    private static final IProductCmptStructureReference[] EMPTY_PRODUCTCMPTSTRUCTUREREFERENCES = new IProductCmptStructureReference[0];
    
    private IIpsProject ipsProject;
    private ProductCmptReference root;
	private GregorianCalendar workingDate;
	
	/**
	 * Creates a new ProductCmptStructure for the given product component and
	 * the user definde working date out of the IpsPreferences.
	 * 
	 * @param root The product component to create a structure for.
	 * @throws CycleInProductStructureException if a cycle is detected.
	 * @throws NullPointerException if the given product component is <code>null</code>.
	 */
	public ProductCmptTreeStructure(IProductCmpt root, IIpsProject project) throws CycleInProductStructureException {
		this(root, IpsPlugin.getDefault().getIpsPreferences().getWorkingDate(), project);
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
     * @param project
     *            The ips project which ips object path is used as search path.
     *            
	 * @throws CycleInProductStructureException
	 *             if a cycle is detected.
	 * @throws NullPointerException
	 *             if the given product component is <code>null</code> or the
	 *             given date is <code>null</code>.
	 */
	public ProductCmptTreeStructure(IProductCmpt root, GregorianCalendar date, IIpsProject project) throws CycleInProductStructureException {
		ArgumentCheck.notNull(root);
		ArgumentCheck.notNull(date);
        ArgumentCheck.notNull(project);
		
        this.workingDate = date;
        this.ipsProject = project;
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
	public void refresh() throws CycleInProductStructureException {
	    this.root = buildNode(root.getProductCmpt(), null);
        this.workingDate = IpsPlugin.getDefault().getIpsPreferences().getWorkingDate();
	}

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptStructureReference[] toArray(boolean productCmptOnly) {
		List result = new ArrayList();
        result.add(root);
		addChildrenToList(root, result, productCmptOnly);
		return (IProductCmptStructureReference[])result.toArray(new IProductCmptStructureReference[result.size()]);
	}
	
	/**
	 * Requests all children from the given parent and add them to the given list.
	 * 
	 * @param parent The parent to get the children from.
	 * @param list The list to add the children to.
	 * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>s.
	 */
	private void addChildrenToList(IProductCmptStructureReference parent, List list, boolean productCmptOnly) {
		if (!productCmptOnly) {
			addChildrenToList(getChildProductCmptTypeRelationReferences(parent), list, productCmptOnly);
		}
		addChildrenToList(getChildProductCmptReferences(parent), list, productCmptOnly);
		addChildrenToList(getChildProductCmptStructureTblUsageReference(parent), list, productCmptOnly);
	}
	
	/**
	 * Adds all given references to the given list and requests the children for the added ones 
	 * revursively.
	 * @param children The array of child-references to add to the list.
	 * @param list The list to add the chlidren to.
	 * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>s.
	 */
	private void addChildrenToList(IProductCmptStructureReference[] children, List list, boolean productCmptOnly) {
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
	 * @throws CycleInProductStructureException 
     */
    private ProductCmptReference buildNode(IProductCmpt cmpt, ProductCmptStructureReference parent) throws CycleInProductStructureException {
    	ProductCmptReference node = new ProductCmptReference(this, parent, cmpt);
    	node.setChildren(buildChildNodes(cmpt, node));
    	return node;
    }

    /**
     * Creates child nodes for the given parent. The children are created out of the 
     * given relations.
     * 
     * @param links The relations to create nodes for.
     * @param parent The parent for the new nodes
     * @return
     * @throws CycleInProductStructureException 
     */
    private ProductCmptStructureReference[] buildChildNodes(
            IProductCmptLink[] links, 
            ProductCmptStructureReference parent, 
            IProductCmptTypeAssociation association) throws CycleInProductStructureException {
        
		ArrayList children = new ArrayList();
        for (int i = 0; i < links.length; i ++) {
			try {
				IProductCmpt p = ipsProject.findProductCmpt(links[i].getTarget());
				if (p != null) {
					if(association.isAssoziation()){
						children.add(new ProductCmptReference(this, parent, p));
					} else {
                        children.add(buildNode(p, parent));
                    }
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
     * @throws CycleInProductStructureException 
     */
	private ProductCmptStructureReference[] buildChildNodes(IIpsElement element, ProductCmptStructureReference parent) throws CycleInProductStructureException {
		ArrayList children = new ArrayList();
		
		if (element instanceof IProductCmpt) {
			IProductCmpt cmpt = ((IProductCmpt)element);
			IProductCmptGeneration activeGeneration = (IProductCmptGeneration) cmpt
					.findGenerationEffectiveOn(workingDate);
			
			if (activeGeneration == null) {
				// no active generation found, so no nodes can be returned.
				return new ProductCmptStructureReference[0];
			}
			
			IProductCmptLink[] links = activeGeneration.getLinks();
			
			// Sort links by association
			Hashtable mapping = new Hashtable();
			ArrayList associations = new ArrayList();
			for (int i = 0; i < links.length; i ++) {
				try {
					IProductCmptTypeAssociation association = links[i].findAssociation(ipsProject);
					if (association == null) {
						// no relation type found - inconsinstent model or product definition - ignore it.
						continue;
					}
					ArrayList linksForAssociation = (ArrayList)mapping.get(association.getName());
					if (linksForAssociation == null) {
						linksForAssociation = new ArrayList();
						mapping.put(association.getName(), linksForAssociation);
						associations.add(association);
					}					
					linksForAssociation.add(links[i]);
				} catch (CoreException e) {
					IpsPlugin.log(e);
				}
			}
			
			for (Iterator iter = associations.iterator(); iter.hasNext();) {
				IProductCmptTypeAssociation association = (IProductCmptTypeAssociation) iter.next();
				ProductCmptStructureReference node = new ProductCmptTypeRelationReference(this, parent, association);
				ArrayList linksList = (ArrayList)mapping.get(association.getName());
				node.setChildren(buildChildNodes((IProductCmptLink[])linksList.toArray(new IProductCmptLink[linksList.size()]), node, association));
				children.add(node);
			}
            
            ITableContentUsage[] tcus = activeGeneration.getTableContentUsages();
            for (int i = 0; i < tcus.length; i++) {
                ProductCmptStructureReference node = new ProductCmptStructureTblUsageReference(this, parent, tcus[i]);
                children.add(node);
            }
		}
		
		ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
		return (ProductCmptStructureReference[])children.toArray(result);
    }

	/**
	 * {@inheritDoc}
	 */
	public IProductCmptReference getParentProductCmptReference(IProductCmptStructureReference child) {
		ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
		IProductCmptStructureReference result = ref.getParent();
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
	public IProductCmptTypeRelationReference getParentProductCmptTypeRelationReference(IProductCmptStructureReference child) {
		ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
		IProductCmptStructureReference result = ref.getParent();
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
	public IProductCmptReference[] getChildProductCmptReferences(IProductCmptStructureReference parent) {
		if (parent instanceof IProductCmptTypeRelationReference) {
            IProductCmptStructureReference[] children = ((ProductCmptTypeRelationReference)parent).getChildren();
            IProductCmptReference[] result = new IProductCmptReference[children.length];
            System.arraycopy(children, 0, result, 0, children.length);
            return result;
        } else if (parent instanceof ProductCmptStructureTblUsageReference) {
            return new IProductCmptReference[0];
        } else {
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
	public IProductCmptTypeRelationReference[] getChildProductCmptTypeRelationReferences(IProductCmptStructureReference parent) {
		if (parent instanceof IProductCmptReference) {
            List relationReferences = new ArrayList();
            IProductCmptStructureReference[] children = ((ProductCmptReference)parent).getChildren();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof IProductCmptTypeRelationReference) {
                    relationReferences.add(children[i]);
                }
            }
            return (IProductCmptTypeRelationReference[])relationReferences
                    .toArray(new IProductCmptTypeRelationReference[relationReferences.size()]);
        } else if (parent instanceof ProductCmptStructureReference) {
            ProductCmptStructureReference children[] = ((ProductCmptStructureReference)parent).getChildren();
            ArrayList result = new ArrayList();
            for (int i = 0; i < children.length; i++) {
                if (children[i] instanceof IProductCmptTypeRelationReference) {
                    result.add(children[i]);
                }
            }
            return (IProductCmptTypeRelationReference[])result.toArray(new IProductCmptTypeRelationReference[result
                    .size()]);
        } else {
            return new IProductCmptTypeRelationReference[0];
        }
	}

    /**
     * {@inheritDoc}
     */
    public IProductCmptStructureTblUsageReference[] getChildProductCmptStructureTblUsageReference(IProductCmptStructureReference parent) {
        List tblUsageReferences = new ArrayList();
        IProductCmptStructureReference[] children = getChildren(parent);
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof ProductCmptStructureTblUsageReference) {
                ProductCmptStructureTblUsageReference tblUsageReference = (ProductCmptStructureTblUsageReference)children[i];
                if (StringUtils.isNotEmpty(tblUsageReference.getTableContentUsage().getTableContentName())){
                    tblUsageReferences.add(children[i]);
                }
            }
        }
        return (IProductCmptStructureTblUsageReference[])tblUsageReferences
                .toArray(new IProductCmptStructureTblUsageReference[tblUsageReferences.size()]);
    }

    private IProductCmptStructureReference[] getChildren(IProductCmptStructureReference parent) {
        if (parent instanceof IProductCmptReference){
            return ((ProductCmptReference)parent).getChildren();
        } else if (parent instanceof IProductCmptStructureReference) {
            return ((ProductCmptStructureReference)parent).getChildren();
        } else {
            return EMPTY_PRODUCTCMPTSTRUCTUREREFERENCES;
        }
    }
}
