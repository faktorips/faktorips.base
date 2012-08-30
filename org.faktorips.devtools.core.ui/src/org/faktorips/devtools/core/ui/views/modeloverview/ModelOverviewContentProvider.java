/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modeloverview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;

public class ModelOverviewContentProvider implements ITreeContentProvider {

    // TODO CODE-REVIEW FIPS-1194: Innere Klassen / Enums nach Konvention immer am Ende der Datei
    /*
     * TODO CODE-REVIEW FIPS-1194: Enum kann als static markiert werden (unterscheidet sich nicht
     * pro Instanz von ModelOverviewContentProvider), Effective Java Item 22
     */
    private enum ToChildAssociationType {
        SELF,
        ASSOCIATION,
        SUPERTYPE
    }

    @Override
    public Object[] getElements(Object inputElement) {
        /*
         * TODO CODE-REVIEW FIPS-1194: Scope dieser beiden Variablen minimieren, indem das return
         * noch in den try Block gezogen wird
         */
        Collection<IType> rootComponents;
        IIpsProject iIpsProject;
        try {
            // get all components from the input project
            // TODO CODE-REVIEW FIPS-1194: Scope minimieren
            IpsObjectType[] filter = { IpsObjectType.PRODUCT_CMPT_TYPE, IpsObjectType.POLICY_CMPT_TYPE };
            List<IIpsSrcFile> srcFiles = new ArrayList<IIpsSrcFile>();

            if (inputElement instanceof IType) {
                iIpsProject = ((IType)inputElement).getIpsProject();
            } else {
                iIpsProject = (IIpsProject)inputElement;
            }

            iIpsProject.findAllIpsSrcFiles(srcFiles, filter);
            List<IType> componentsFromSrcFiles = getComponentsFromSrcFiles(srcFiles);

            // get the root elements
            if (inputElement instanceof IType) {
                /*
                 * TODO hier werden entweder nur PolicyCmptTypes oder ProductCmptTypes benötigt, die
                 * anderen können weggelassen werden
                 */
                IType input = (IType)inputElement;
                Collection<IType> rootCandidates = getRootElementsForIType(input, componentsFromSrcFiles,
                        ToChildAssociationType.SELF, new ArrayList<IType>());
                rootComponents = getRootElementsForIType(input, componentsFromSrcFiles, ToChildAssociationType.SELF,
                        rootCandidates);
            } else {
                rootComponents = getRootComponentTypes(componentsFromSrcFiles);
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        return encapsulateComponentTypes(rootComponents, iIpsProject).toArray();
    }

    /**
     * Computes the root-nodes for an {@link IType} element. The root-nodes are exactly those nodes
     * which have an outgoing association to the given element. An association is of Type
     * {@link AssociationType}.COMPOSITION_MASTER_TO_DETAIL or {@link AssociationType}.AGGREGATION
     * and may be contain a subtype hierarchy. A run of this method with an empty rootCandidates
     * parameter will return a list of root-candidates. A second run of this method with the
     * previously obtained rootCandidates as parameter, will clean the list of false candidates.
     * 
     * @param element the starting point
     * @param componentList the list of all concerned elements
     * @param association the {@link ToChildAssociationType} of the parent element to this element
     * @param rootCandidates a {@link Collection} of {@link IType}.
     */
    private Collection<IType> getRootElementsForIType(IType element,
            List<IType> componentList,
            ToChildAssociationType association,
            Collection<IType> rootCandidates) {

        // TODO CODE-REVIEW FIPS-1194: Scope von lokalen Variablen minimieren
        Set<IType> rootElements = new HashSet<IType>();
        List<IType> associatingTypes = getAssociatingTypes(element, componentList);
        IType supertype;

        try {
            supertype = element.findSupertype(element.getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }

        // Breaking condition
        if (associatingTypes.isEmpty() && supertype == null
                && (association == ToChildAssociationType.SELF || association == ToChildAssociationType.ASSOCIATION)) {
            rootElements.add(element);
        }

        // recursive call for all child elements
        for (IType associations : associatingTypes) {
            rootElements.addAll(getRootElementsForIType(associations, componentList,
                    ToChildAssociationType.ASSOCIATION, rootCandidates));
        }

        if (supertype != null) {
            rootElements.addAll(getRootElementsForIType(supertype, componentList, ToChildAssociationType.SUPERTYPE,
                    rootCandidates));
        }

        // If a supertype has been added in the first run, it has to be added now, too
        if (rootElements.isEmpty() && association == ToChildAssociationType.SUPERTYPE
                && rootCandidates.contains(element)) {
            rootElements.add(element);
        }

        // None of the child elements is a root element, therefore check the association type of the
        // current element
        if (rootElements.isEmpty() && association == ToChildAssociationType.ASSOCIATION) {
            rootElements.add(element);
        }

        // If the hierarchy is solely build with supertypes, we must add the source element itself
        if (rootElements.isEmpty() && association == ToChildAssociationType.SELF) {
            rootElements.add(element);
        }

        return rootElements;
    }

    /*
     * TODO CODE-REVIEW FIPS-1194: Das Wort Component verwenden wir im Zusammenhang mit ITypes
     * nicht, besser also getRootTypes
     */
    private List<IType> getRootComponentTypes(List<IType> components) {
        List<IType> rootComponents = new ArrayList<IType>();
        for (IType iType : components) {
            if (!iType.hasSupertype() && !isAssociationTarget(iType, components)) {
                rootComponents.add(iType);
            }
        }
        return rootComponents;
    }

    /**
     * Takes a {@link List} of {@link IIpsSrcFile} and extracts the corresponding {@link List} of
     * {@link IType}. It operates only on {@link List Lists} of {@link IIpsSrcFile} which represent
     * {@link IType}
     * 
     * @param srcFiles a {@link List} of {@link IIpsSrcFile} which represents {@link IType} elements
     * @return a {@link List} of the same size with corresponding {@link IType}, or an empty
     *         {@link List} if the input {@link List} was empty
     */
    private List<IType> getComponentsFromSrcFiles(List<IIpsSrcFile> srcFiles) {
        List<IType> components = new ArrayList<IType>(srcFiles.size());
        for (IIpsSrcFile file : srcFiles) {
            try {
                components.add((IType)file.getIpsObject());
            } catch (CoreException e) {
                throw new CoreRuntimeException(e);
            }
        }
        return components;
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Nothing to do
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        return ((IModelOverviewNode)parentElement).getChildren().toArray();
    }

    @Override
    public Object getParent(Object element) {
        IModelOverviewNode node = (IModelOverviewNode)element;
        return node.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length != 0;
    }

    /*
     * TODO CODE-REVIEW FIPS-1194: Gehören die folgenden beiden statischen Methoden nicht eher an
     * die Klasse ComponentNode?
     */
    /**
     * Encapsulates a {@link List} of {@link IType ITypes} into a {@link List} of
     * {@link ComponentNode ComponentNodes}.
     * 
     * @param components the elements which should be encapsulated
     * @return a {@link List} of {@link ComponentNode ComponenteNodes}
     */
    protected static List<ComponentNode> encapsulateComponentTypes(Collection<IType> components, IIpsProject rootProject) {
        List<ComponentNode> componentNodes = new ArrayList<ComponentNode>();

        for (IType component : components) {
            componentNodes.add(encapsulateComponentType(component, rootProject));
        }

        return componentNodes;
    }

    protected static ComponentNode encapsulateComponentType(IType component, IIpsProject rootProject) {
        return new ComponentNode(component, null, rootProject);
    }

    /**
     * This method checks if an {@link IType} is targeted by an {@link IAssociation}.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     * @return {@code true}, if any association is directed towards the provided target, otherwise
     *         {@code false}
     */
    private boolean isAssociationTarget(IType target, List<IType> components) {
        /*
         * TODO CODE-REVIEW FIPS-1194: Können wir hier vielleicht
         * IType#findAssociationsForTargetAndAssociationType verwenden?
         */
        for (IType component : components) {
            List<IType> associations = getAssociations(component);
            for (IType association : associations) {
                if (association.getQualifiedName().equals(target.getQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method computes a {@link List} of {@link IType ITypes} which targets the indicated
     * target with an {@link IAssociation}, or an empty {@link List} if there are no such
     * associations.
     * 
     * @param target the {@link IType} which should be checked on incoming associations
     * @param components a {@link List} of {@link IType} which define the scope of associations that
     *            will be checked
     */
    private List<IType> getAssociatingTypes(IType target, List<IType> components) {
        /*
         * TODO CODE-REVIEW FIPS-1194: Können wir hier vielleicht
         * IType#findAssociationsForTargetAndAssociationType verwenden und dann auf jeder
         * zurückgegebenen IAssociation nur noch getType() aufrufen?
         */
        List<IType> associatingComponents = new ArrayList<IType>();
        for (IType component : components) {
            List<IType> targets = getAssociations(component);
            for (IType targetComponentType : targets) {
                if (targetComponentType.getQualifiedName().equals(target.getQualifiedName())) {
                    associatingComponents.add(component);
                    break;
                }
            }
        }
        return associatingComponents;
    }

    /*
     * TODO CODE-REVIEW FIPS-1194: Diese Methode sollte direkt vom IType angeboten werden. Analog
     * findAssociationsForTargetAndAssociationType könnte es dort noch
     * findAssociationsForAssociationType geben
     */
    /**
     * Returns a {@link List} of {@link IType component types} which are associated to this
     * {@link IType} component type. The only {@link AssociationType association types} which will
     * be returned are {@link AssociationType}.COMPOSITION_MASTER_TO_DETAIL an
     * {@link AssociationType}.AGGREGATION, that means only associations which are directed away
     * from the argument <tt>object</tt>.
     * 
     * @param rootElement for which the outgoing associations should be returned
     * @return a {@link List} of associated {@link IType}s
     */
    protected static List<IType> getAssociations(IType rootElement) {
        List<IType> associations = new ArrayList<IType>();
        try {
            List<IAssociation> findAssociations = rootElement.getAssociations();
            for (IAssociation association : findAssociations) {
                if (association.getAssociationType().equals(AssociationType.COMPOSITION_MASTER_TO_DETAIL)
                        || association.getAssociationType().equals(AssociationType.AGGREGATION)) {
                    associations.add(association.findTarget(association.getIpsProject()));
                }
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
        return associations;
    }
}
