/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt.treestructure;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

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
import org.faktorips.devtools.core.model.productcmpt.IValidationRuleConfig;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptStructureTblUsageReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeAssociationReference;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptVRuleReference;
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

    private final IIpsProject ipsProject;

    private ProductCmptReference root;

    private final GregorianCalendar workingDate;

    private Set<IProductCmptStructureReference> setWithProductCmptsOnly;

    private Set<IProductCmptStructureReference> setWithAll;

    /**
     * Creates a new ProductCmptStructure for the given product component. Instead of a specified
     * working date this constructor uses the latest adjustment (generation).
     * <p>
     * Do not use this constructor anymore! This builds a structure with the latest generation of
     * every product component. This could lead to invalid structures containing invalid generation
     * pairs.
     * <p>
     * 
     * @param root The product component to create a structure for.
     * 
     * @throws CycleInProductStructureException if a cycle is detected.
     * @throws NullPointerException if the given product component is <code>null</code>.
     */
    @Deprecated
    public ProductCmptTreeStructure(IProductCmpt root, IIpsProject project) throws CycleInProductStructureException {
        this(root, null, project);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ipsProject == null) ? 0 : ipsProject.hashCode());
        /*
         * We do not use the hash code of root directly but the hash of the wrapped
         * IIpsObjectPartContainer because the hash code of the root uses the hash code of this
         * structure leading to a recursive call.
         */
        if (root == null || root.getWrapped() == null) {
            result = prime * result;
        } else {
            result = prime * result + root.getWrapped().hashCode();
        }
        result = prime * result + ((workingDate == null) ? 0 : workingDate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProductCmptTreeStructure other = (ProductCmptTreeStructure)obj;
        if (ipsProject == null) {
            if (other.ipsProject != null) {
                return false;
            }
        } else if (!ipsProject.equals(other.ipsProject)) {
            return false;
        }
        /*
         * We do not use the equals method of root directly but the equals method the wrapped
         * IIpsObjectPartContainer because the equals method of the root uses the equals method of
         * this structure leading to a recursive call.
         */
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else {
            if (root.getWrapped() == null) {
                if (other.getRoot().getWrapped() != null) {
                    return false;
                }
            } else if (!root.getWrapped().equals(other.root.getWrapped())) {
                return false;
            }
        }
        if (workingDate == null) {
            if (other.workingDate != null) {
                return false;
            }
        } else if (!workingDate.equals(other.workingDate)) {
            return false;
        }
        return true;
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

    @Override
    public GregorianCalendar getValidAt() {
        return workingDate;
    }

    @Override
    public IProductCmptReference getRoot() {
        return root;
    }

    @Override
    public void refresh() throws CycleInProductStructureException {
        setWithAll = null;
        setWithProductCmptsOnly = null;
        root = buildNode(root.getProductCmpt(), null, null);
    }

    @Override
    public Set<IProductCmptStructureReference> toSet(boolean productCmptOnly) {
        if (productCmptOnly) {
            if (setWithProductCmptsOnly != null) {
                return setWithProductCmptsOnly;
            }
        } else if (setWithAll != null) {
            return setWithAll;
        }

        Set<IProductCmptStructureReference> result = new HashSet<IProductCmptStructureReference>();
        result.add(root);
        addChildrenToList(root, result, productCmptOnly);

        if (productCmptOnly) {
            setWithProductCmptsOnly = result;
        } else {
            setWithAll = result;
        }

        return result;
    }

    /**
     * Requests all children from the given parent and add them to the given list.
     * 
     * @param parent The parent to get the children from.
     * @param set The set to add the children to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference parent,
            Set<IProductCmptStructureReference> set,
            boolean productCmptOnly) {

        if (!productCmptOnly) {
            addChildrenToList(getChildProductCmptTypeAssociationReferences(parent), set, productCmptOnly);
        }
        addChildrenToList(getChildProductCmptReferences(parent), set, productCmptOnly);
        addChildrenToList(getChildProductCmptStructureTblUsageReference(parent), set, productCmptOnly);
    }

    /**
     * Adds all given references to the given list and requests the children for the added ones
     * recursively.
     * 
     * @param children The array of child-references to add to the list.
     * @param set The list to add the children to.
     * @param productCmptOnly <code>true</code> to only get references to <code>IProductCmpt</code>
     *            s.
     */
    private void addChildrenToList(IProductCmptStructureReference[] children,
            Set<IProductCmptStructureReference> set,
            boolean productCmptOnly) {
        for (IProductCmptStructureReference element : children) {
            set.add(element);
            addChildrenToList(element, set, productCmptOnly);
        }
    }

    /**
     * Creates a new node for the given element and links it to the given parent-node.
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
     */
    private ProductCmptStructureReference[] buildChildNodes(IProductCmptLink[] links,
            ProductCmptStructureReference parent,
            IProductCmptTypeAssociation association) throws CycleInProductStructureException {

        List<IProductCmptStructureReference> children = new ArrayList<IProductCmptStructureReference>();
        for (IProductCmptLink link : links) {
            try {
                IProductCmpt p = link.findTarget(ipsProject);
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

            List<IValidationRuleConfig> rules = activeGeneration.getValidationRuleConfigs();
            for (IValidationRuleConfig rule : rules) {
                ProductCmptStructureReference node = new ProductCmptVRuleReference(this, parent, cmpt, rule);
                children.add(node);
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
        } else {
            return ((ProductCmptStructureReference)parent).getChildren();
        }
    }

    @Override
    public IProductCmptVRuleReference[] getChildProductCmptVRuleReferences(IProductCmptStructureReference parent) {
        List<ProductCmptVRuleReference> vRuleReferences = new ArrayList<ProductCmptVRuleReference>();
        IProductCmptStructureReference[] children = getChildren(parent);
        for (IProductCmptStructureReference element : children) {
            if (element instanceof ProductCmptVRuleReference) {
                ProductCmptVRuleReference vRuleReference = (ProductCmptVRuleReference)element;
                if (StringUtils.isNotEmpty(vRuleReference.getValidationRuleConfig().getName())) {
                    vRuleReferences.add(vRuleReference);
                }
            }
        }
        return vRuleReferences.toArray(new ProductCmptVRuleReference[vRuleReferences.size()]);
    }

    @Override
    public boolean referencesProductCmptQualifiedName(String prodCmptQualifiedName) {
        return getProductCmptReferenceRecursive(root, prodCmptQualifiedName) != null;
    }

    /**
     * 
     * Returns the given {@link IProductCmptReference} if it contains the searched product
     * component's qualified name. If not it searches all children of the name the same way and
     * returns the result.
     * 
     * @param cmptReference the subtree/substructure that is to be searched
     * @param prodCmptQualifiedName the qualified name of the searched {@link IProductCmpt}
     * @return the {@link IProductCmptReference} referencing the indicated {@link IProductCmpt}, or
     *         <code>null</code> if none was found.
     */
    private IProductCmptReference getProductCmptReferenceRecursive(IProductCmptReference cmptReference,
            String prodCmptQualifiedName) {
        if (cmptReference.getProductCmpt().getQualifiedName().equals(prodCmptQualifiedName)) {
            return cmptReference;
        }
        IProductCmptReference[] childProductCmptReferences = getChildProductCmptReferences(cmptReference);
        for (IProductCmptReference childRef : childProductCmptReferences) {
            IProductCmptReference foundRef = getProductCmptReferenceRecursive(childRef, prodCmptQualifiedName);
            if (foundRef != null) {
                return foundRef;
            }
        }
        return null;
    }

}
