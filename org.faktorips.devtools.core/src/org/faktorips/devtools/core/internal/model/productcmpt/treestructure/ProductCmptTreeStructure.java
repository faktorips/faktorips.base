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
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
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
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.type.IAssociation;
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
     * Creates a new ProductCmptStructure for the given product component. Instead of a specified
     * working date this constructor uses the latest adjustment (generation).
     * 
     * @param root The product component to create a structure for.
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if the given product component is <code>null</code>.
     */
    public ProductCmptTreeStructure(IProductCmpt root, IIpsProject project) throws CycleInProductStructureException {
        this(root, null, project);
    }

    /**
     * Creates a new ProductCmptStructure for the given product component and the given date.
     * 
     * @param root The product component to create a structure for.
     * @param date The date the structure has to be valid for. That means that the relations between
     *            the product components represented by this structure are valid for the given date.
     * @param project The ips project which ips object path is used as search path.
     * 
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if the given product component is <code>null</code> or the given
     *             date is <code>null</code>.
     */
    public ProductCmptTreeStructure(IProductCmpt root, GregorianCalendar date, IIpsProject project)
            throws CycleInProductStructureException {
        ArgumentCheck.notNull(root);
        ArgumentCheck.notNull(project);

        workingDate = date;
        ipsProject = project;
        this.root = buildNode(root, null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GregorianCalendar getValidAt() {
        return workingDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptReference getRoot() {
        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refresh() throws CycleInProductStructureException {
        root = buildNode(root.getProductCmpt(), null, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptStructureReference[] toArray(boolean productCmptOnly) {
        List<IProductCmptStructureReference> result = new ArrayList<IProductCmptStructureReference>();
        result.add(root);
        addChildrenToList(root, result, productCmptOnly);
        return result.toArray(new IProductCmptStructureReference[result.size()]);
    }

    /**
     * Requests all children from the given parent and add them to the given list.
     * 
     * @param parent The parent to get the children from.
     * @param list The list to add the children to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference parent,
            List<IProductCmptStructureReference> list,
            boolean productCmptOnly) {
        if (!productCmptOnly) {
            addChildrenToList(getChildProductCmptTypeAssociationReferences(parent), list, productCmptOnly);
        }
        addChildrenToList(getChildProductCmptReferences(parent), list, productCmptOnly);
        addChildrenToList(getChildProductCmptStructureTblUsageReference(parent), list, productCmptOnly);
    }

    /**
     * Adds all given references to the given list and requests the children for the added ones
     * revursively.
     * 
     * @param children The array of child-references to add to the list.
     * @param list The list to add the chlidren to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference[] children,
            List<IProductCmptStructureReference> list,
            boolean productCmptOnly) {
        for (IProductCmptStructureReference element : children) {
            list.add(element);
            addChildrenToList(element, list, productCmptOnly);
        }
    }

    /**
     * Creates a new node for the given element and links it to the given parent-node.
     * 
     * @param element The IpsElement to be wrapped by the new node.
     * @param parent The parent-node for the new one.
     * @throws CycleInProductStructureException
     */
    private ProductCmptReference buildNode(IProductCmpt cmpt,
            IProductCmptLink link,
            ProductCmptStructureReference parent) throws CycleInProductStructureException {
        ProductCmptReference node = new ProductCmptReference(this, parent, cmpt, link);
        node.setChildren(buildChildNodes(cmpt, node));
        return node;
    }

    /**
     * Creates child nodes for the given parent. The children are created out of the given
     * relations.
     * 
     * @param links The relations to create nodes for.
     * @param parent The parent for the new nodes
     * @return
     * @throws CycleInProductStructureException
     */
    private ProductCmptStructureReference[] buildChildNodes(IProductCmptLink[] links,
            ProductCmptStructureReference parent,
            IProductCmptTypeAssociation association) throws CycleInProductStructureException {

        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();
        for (IProductCmptLink link : links) {
            try {
                IProductCmpt p = ipsProject.findProductCmpt(link.getTarget());
                if (p != null) {
                    if (association.isAssoziation()) {
                        children.add(new ProductCmptReference(this, parent, p, link));
                    } else {
                        children.add(buildNode(p, link, parent));
                    }
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }
        }
        ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
        return children.toArray(result);
    }

    /**
     * Creates new child nodes for the given element and parent.
     * 
     * @param element The element the new children can be found in as relation-targets.
     * @param parent The parent node for the new children.
     * @throws CycleInProductStructureException
     */
    private ProductCmptStructureReference[] buildChildNodes(IIpsElement element, ProductCmptStructureReference parent)
            throws CycleInProductStructureException {
        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();

        if (element instanceof IProductCmpt) {
            IProductCmpt cmpt = ((IProductCmpt)element);
            IProductCmptGeneration activeGeneration = null;
            if (workingDate != null) {
                activeGeneration = (IProductCmptGeneration)cmpt.findGenerationEffectiveOn(workingDate);
            } else {
                IIpsObjectGeneration[] generations = cmpt.getGenerationsOrderedByValidDate();
                if (generations.length > 0) {
                    activeGeneration = (IProductCmptGeneration)generations[generations.length - 1];
                }
            }
            if (activeGeneration == null) {
                return new ProductCmptReference[0];
            }

            IProductCmptLink[] links = activeGeneration.getLinks();

            // Sort links by association
            Hashtable<String, List<IProductCmptLink>> mapping = new Hashtable<String, List<IProductCmptLink>>();

            List<IAssociation> associations = new ArrayList<IAssociation>();
            // new ArrayList<IProductCmptTypeAssociation>();
            for (IProductCmptLink link : links) {
                try {
                    // TODO check for JAVA5 for iteration
                    IProductCmptTypeAssociation association = link.findAssociation(ipsProject);
                    if (association == null) {
                        // no relation type found - inconsinstent model or product definition -
                        // ignore it.
                        continue;
                    }
                    List<IProductCmptLink> linksForAssociation = mapping.get(association.getName());
                    if (linksForAssociation == null) {
                        linksForAssociation = new ArrayList<IProductCmptLink>();
                        mapping.put(association.getName(), linksForAssociation);
                        // associations.add(association);
                    }
                    linksForAssociation.add(link);
                } catch (CoreException e) {
                    IpsPlugin.log(e);
                }
            }

            try {
                IProductCmptType cmptType = cmpt.findProductCmptType(ipsProject);
                if (cmptType != null) {
                    // get again all associations. in the previous constructed list there are only
                    // not empty relations
                    associations = cmptType.findAllNotDerivedAssociations();
                }
                for (IAssociation iAssociation : associations) {
                    IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)iAssociation;
                    ProductCmptStructureReference node = new ProductCmptTypeAssociationReference(this, parent,
                            association);
                    List<IProductCmptLink> linksList = mapping.get(association.getName());
                    if (linksList == null) {
                        linksList = new ArrayList<IProductCmptLink>();
                    }
                    node.setChildren(buildChildNodes(linksList.toArray(new IProductCmptLink[linksList.size()]), node,
                            association));
                    children.add(node);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e);
            }

            ITableContentUsage[] tcus = activeGeneration.getTableContentUsages();
            for (ITableContentUsage tcu : tcus) {
                ProductCmptStructureReference node = new ProductCmptStructureTblUsageReference(this, parent, tcu);
                children.add(node);
            }
        }

        ProductCmptStructureReference[] result = new ProductCmptStructureReference[children.size()];
        return children.toArray(result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptReference getParentProductCmptReference(IProductCmptStructureReference child) {
        ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
        IProductCmptStructureReference result = ref.getParent();
        if (result instanceof IProductCmptReference) {
            return (IProductCmptReference)result;
        } else if (result != null) {
            return (IProductCmptReference)result.getParent();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptTypeAssociationReference getParentProductCmptTypeRelationReference(IProductCmptStructureReference child) {
        ProductCmptStructureReference ref = (ProductCmptStructureReference)child;
        IProductCmptStructureReference result = ref.getParent();
        if (result instanceof IProductCmptTypeAssociationReference) {
            return (IProductCmptTypeAssociationReference)result;
        } else if (result != null) {
            return (IProductCmptTypeAssociationReference)result.getParent();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptReference[] getChildProductCmptReferences(IProductCmptStructureReference parent) {
        if (parent instanceof IProductCmptTypeAssociationReference) {
            IProductCmptStructureReference[] children = ((ProductCmptTypeAssociationReference)parent).getChildren();
            IProductCmptReference[] result = new IProductCmptReference[children.length];
            System.arraycopy(children, 0, result, 0, children.length);
            return result;
        } else if (parent instanceof ProductCmptStructureTblUsageReference) {
            return new IProductCmptReference[0];
        } else {
            ProductCmptStructureReference children[] = ((ProductCmptReference)parent).getChildren();
            List<IProductCmptReference> result = new ArrayList<IProductCmptReference>();
            for (ProductCmptStructureReference ref : children) {
                for (ProductCmptStructureReference child : ref.getChildren()) {
                    if (child instanceof IProductCmptReference) {
                        result.add((IProductCmptReference)child);
                    }
                }
            }
            return result.toArray(new IProductCmptReference[result.size()]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(IProductCmptStructureReference parent) {
        return getChildProductCmptTypeAssociationReferences(parent, true);
    }

    @Override
    public IProductCmptTypeAssociationReference[] getChildProductCmptTypeAssociationReferences(IProductCmptStructureReference parent,
            boolean includeEmptyAssociations) {
        if (parent instanceof IProductCmptReference) {
            List<IProductCmptTypeAssociationReference> associationReferences = new ArrayList<IProductCmptTypeAssociationReference>();
            IProductCmptStructureReference[] children = ((ProductCmptReference)parent).getChildren();
            for (IProductCmptStructureReference element : children) {
                if (element instanceof IProductCmptTypeAssociationReference) {
                    IProductCmptTypeAssociationReference relationReference = (IProductCmptTypeAssociationReference)element;
                    if (includeEmptyAssociations || getChildProductCmptReferences(relationReference).length > 0) {
                        associationReferences.add(relationReference);
                    }
                }
            }
            return associationReferences
                    .toArray(new IProductCmptTypeAssociationReference[associationReferences.size()]);
        } else if (parent instanceof ProductCmptStructureReference) {
            ProductCmptStructureReference children[] = ((ProductCmptStructureReference)parent).getChildren();
            List<IProductCmptTypeAssociationReference> result = new ArrayList<IProductCmptTypeAssociationReference>();
            for (ProductCmptStructureReference element : children) {
                if (element instanceof IProductCmptTypeAssociationReference) {
                    result.add((IProductCmptTypeAssociationReference)element);
                }
            }
            return result.toArray(new IProductCmptTypeAssociationReference[result.size()]);
        } else {
            return new IProductCmptTypeAssociationReference[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProductCmptStructureTblUsageReference[] getChildProductCmptStructureTblUsageReference(IProductCmptStructureReference parent) {
        List<ProductCmptStructureTblUsageReference> tblUsageReferences = new ArrayList<ProductCmptStructureTblUsageReference>();
        IProductCmptStructureReference[] children = getChildren(parent);
        for (IProductCmptStructureReference element : children) {
            if (element instanceof ProductCmptStructureTblUsageReference) {
                ProductCmptStructureTblUsageReference tblUsageReference = (ProductCmptStructureTblUsageReference)element;
                if (StringUtils.isNotEmpty(tblUsageReference.getTableContentUsage().getTableContentName())) {
                    tblUsageReferences.add(tblUsageReference);
                }
            }
        }
        return tblUsageReferences.toArray(new IProductCmptStructureTblUsageReference[tblUsageReferences.size()]);
    }

    private IProductCmptStructureReference[] getChildren(IProductCmptStructureReference parent) {
        if (parent instanceof IProductCmptReference) {
            return ((ProductCmptReference)parent).getChildren();
        } else if (parent instanceof IProductCmptStructureReference) {
            return ((ProductCmptStructureReference)parent).getChildren();
        } else {
            return EMPTY_PRODUCTCMPTSTRUCTUREREFERENCES;
        }
    }
}
