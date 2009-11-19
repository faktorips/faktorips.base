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

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptReference;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTreeStructure;
import org.faktorips.devtools.core.internal.model.productcmpt.treestructure.ProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpt.treestructure.CycleInProductStructureException;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTreeStructure;
import org.faktorips.devtools.core.model.productcmpt.treestructure.IProductCmptTypeRelationReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpttype.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.model.type.IAssociation;

/**
 * Provides the content for a generation-based association-tree. The association names are requested
 * from the given generation and all supertypes the type containing this generation is based on.
 * 
 * @author Thorsten Guenther
 */
public class LinksContentProvider implements ITreeContentProvider {

    private IProductCmptGeneration generation;

    /**
     * {@inheritDoc}
     */
    public Object[] getElements(Object inputElement) {
        if (!(inputElement instanceof IProductCmptGeneration)) {
            throw new RuntimeException("Unknown input element type " + inputElement.getClass()); //$NON-NLS-1$
        }
        IProductCmptGeneration generation = (IProductCmptGeneration)inputElement;
        try {
            IProductCmpt pc = generation.getProductCmpt();
            IProductCmptType pcType = pc.findProductCmptType(generation.getIpsProject());
            if (pcType == null) {
                // type can't be found, so extract the association name from the links in the
                // generation
                // this is the reason we return Strings instead of association objects as elements.
                return getAssociationNames(generation);
            } else {
                // find association using the product cmpt's project
                return getAssociationNodes(pcType, generation);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting element ", e); //$NON-NLS-1$
        }
    }

    private static IProductCmptTypeRelationReference[] getAssociationNodes(IProductCmptType type,
            IProductCmptGeneration productCmpGen) throws CoreException, CycleInProductStructureException {
        NoneDerivedAssociationsCollector collector = new NoneDerivedAssociationsCollector(productCmpGen.getIpsProject());
        collector.start(type);
        List<IProductCmptTypeRelationReference> result = new ArrayList<IProductCmptTypeRelationReference>();
        IProductCmptTreeStructure structure = new ProductCmptTreeStructure(productCmpGen.getProductCmpt(),
                productCmpGen.getValidFrom(), productCmpGen.getIpsProject());
        ProductCmptReference parent = new ProductCmptReference(structure, null, productCmpGen.getProductCmpt());
        for (IAssociation association : collector.associations) {
            result
                    .add(new ProductCmptTypeRelationReference(structure, parent,
                            (IProductCmptTypeAssociation)association));
        }
        return result.toArray(new IProductCmptTypeRelationReference[result.size()]);
    }

    private String[] getAssociationNames(IProductCmptGeneration gen) {
        Set associations = new HashSet();
        IProductCmptLink[] links = gen.getLinks();
        for (int i = 0; i < links.length; i++) {
            associations.add(links[i].getAssociation());
        }
        return (String[])associations.toArray(new String[associations.size()]);
    }

    private String[] getAssociationNames(IProductCmptType type, IIpsProject ipsProject) throws CoreException {
        NoneDerivedAssociationsCollector collector = new NoneDerivedAssociationsCollector(ipsProject);
        collector.start(type);
        return collector.associations.toArray(new String[collector.associations.size()]);
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
        } else {
            generation = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object[] getChildren(Object parentElement) {
        if (!(parentElement instanceof IProductCmptTypeRelationReference) || generation == null) {
            return new Object[0];
        }
        IProductCmptTypeRelationReference reference = (IProductCmptTypeRelationReference)parentElement;
        return generation.getLinks(reference.getRelation().getName());
    }

    /**
     * {@inheritDoc}
     */
    public Object getParent(Object element) {
        if (element instanceof IProductCmptTypeRelationReference) {
            return generation;
        }
        if (element instanceof IProductCmptLink) {
            IProductCmptLink link = (IProductCmptLink)element;
            return link.getAssociation();
        }
        throw new RuntimeException("Unknown element type " + element); //$NON-NLS-1$ 
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasChildren(Object element) {
        Object[] children = getChildren(element);
        if (children == null) {
            return false;
        }
        return children.length > 0;
    }

    static class NoneDerivedAssociationsCollector extends ProductCmptTypeHierarchyVisitor {

        private List<IAssociation> associations = new ArrayList<IAssociation>();

        public NoneDerivedAssociationsCollector(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            IAssociation[] typeAssociations = currentType.getAssociations();
            int index = 0;
            for (int i = 0; i < typeAssociations.length; i++) {
                // to get the associations of the root type of the supertype hierarchy first,
                // put in the list at first, but with unchanged order for all associations
                // found in one type...
                if (!typeAssociations[i].isDerived()) {
                    associations.add(index, typeAssociations[i]);
                    index++;
                }
            }
            return true;
        }

    }
}
