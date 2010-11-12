/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.ITypeHierarchy;
import org.faktorips.util.ArgumentCheck;

public class TypeHierarchy implements ITypeHierarchy {

    private IType pcType;
    private Map<IType, Node> nodes = new HashMap<IType, Node>();
    private boolean containsCycle = false;

    /**
     * Creates a new type hierarchy containing all the given type's supertypes. Subtypes are not
     * resolved.
     */
    public final static TypeHierarchy getSupertypeHierarchy(IType pcType) throws CoreException {
        IIpsProject project = pcType.getIpsProject();
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        IType[] subtypes = new IType[0];
        while (pcType != null) {
            IType supertype = pcType.findSupertype(project);
            if (hierarchy.contains(supertype)) {
                hierarchy.containsCycle = true;
                supertype = null;
            }
            hierarchy.add(new Node(pcType, supertype, subtypes));
            subtypes = new IType[] { pcType };
            pcType = supertype;
        }
        return hierarchy;
    }

    /**
     * Creates a new type hierarchy containing all the given type's subtypes. Supertypes are not
     * resolved.
     */
    public final static TypeHierarchy getSubtypeHierarchy(IType pcType) throws CoreException {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        addSubtypes(hierarchy, pcType, null);
        return hierarchy;
    }

    /**
     * Creates a new type hierarchy containing all the given type's subtypes and supertypes.
     */
    public final static TypeHierarchy getTypeHierarchy(IType pcType) throws CoreException {
        TypeHierarchy hierarchy = getSupertypeHierarchy(pcType);
        Node pcTypeNode = hierarchy.nodes.get(pcType);
        List<IType> subtypes = searchDirectSubtypes(pcType, hierarchy);
        for (IType subtype : subtypes) {
            addSubtypes(hierarchy, subtype, pcType);
        }
        pcTypeNode.subtypes = subtypes.toArray(new IType[subtypes.size()]);
        return hierarchy;
    }

    private final static void addSubtypes(TypeHierarchy hierarchie, IType pcType, IType superType) throws CoreException {

        List<IType> subtypes = searchDirectSubtypes(pcType, hierarchie);
        Node node = new Node(pcType, superType, subtypes.toArray(new IType[subtypes.size()]));
        hierarchie.add(node);
        for (IType subtype : subtypes) {
            addSubtypes(hierarchie, subtype, pcType);
        }
    }

    private final static List<IType> searchDirectSubtypes(IType type, TypeHierarchy hierarchy) throws CoreException {
        List<IType> subtypes = new ArrayList<IType>();
        IIpsProject project = type.getIpsProject();
        IIpsProject[] projects = project.findReferencingProjectLeavesOrSelf();
        for (IIpsProject referencingProject : projects) {
            if (referencingProject.equals(project) || referencingProject.isReferencing(project)) {
                IIpsSrcFile[] candidateSrcFiles = referencingProject.findIpsSrcFiles(type.getIpsObjectType());
                for (IIpsSrcFile candidateSrcFile : candidateSrcFiles) {
                    String candidateSuperTypeName = candidateSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
                    if (type.getQualifiedName().equals(candidateSuperTypeName)) {
                        IIpsObject candidateObject = candidateSrcFile.getIpsObject();
                        if (candidateObject instanceof IType) {
                            IType candidateType = (IType)candidateObject;
                            if (!subtypes.contains(candidateObject)) {
                                if (hierarchy.contains(candidateType)) {
                                    hierarchy.containsCycle = true;
                                } else {
                                    subtypes.add(candidateType);
                                }
                            }
                        }
                    }
                }
            }
        }
        return subtypes;
    }

    private TypeHierarchy(IType pcType) {
        this.pcType = pcType;
    }

    @Override
    public boolean containsCycle() {
        return containsCycle;
    }

    private boolean add(Node node) {
        if (nodes.containsKey(node.type)) {
            containsCycle = true;
            return false;
        }
        nodes.put(node.type, node);
        return true;
    }

    private boolean contains(IType type) {
        return nodes.containsKey(type);
    }

    @Override
    public IType getType() {
        return pcType;
    }

    @Override
    public IType getSupertype(IType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return null;
        }
        return node.supertype;
    }

    /**
     * Returns the type's supertypes by travelling up the hierarchy. The first element in the array
     * (if any) is the given type's direkt supertype.
     */
    @Override
    public IType[] getAllSupertypes(IType type) {
        List<IType> result = new ArrayList<IType>();
        getAllSupertypes(type, result);
        return result.toArray(new IType[result.size()]);
    }

    @Override
    public IType[] getAllSupertypesInclSelf(IType type) {
        List<IType> result = new ArrayList<IType>();
        result.add(type);
        getAllSupertypes(type, result);
        return result.toArray(new IType[result.size()]);
    }

    private void getAllSupertypes(IType type, List<IType> result) {
        IType supertype = getSupertype(type);
        while (supertype != null && !result.contains(supertype)) {
            result.add(supertype);
            supertype = getSupertype(supertype);
        }
    }

    @Override
    public boolean isSupertypeOf(IType candidate, IType subtype) {
        IType currSupertype = getSupertype(subtype);
        if (currSupertype == null) {
            return false;
        }
        if (currSupertype.equals(candidate)) {
            return true;
        }
        return isSupertypeOf(candidate, currSupertype);
    }

    @Override
    public boolean isSubtypeOf(IType candidate, IType supertype) {
        IType[] subtypes = getSubtypes(supertype);
        for (IType subtype : subtypes) {
            if (subtype.equals(candidate)) {
                return true;
            }
            if (isSubtypeOf(candidate, subtype)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IType[] getSubtypes(IType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new IType[0];
        }
        return node.subtypes;
    }

    @Override
    public IType[] getAllSubtypes(IType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new IType[0];
        }

        ArrayList<IType> all = new ArrayList<IType>();
        addSubtypes(node, all);
        return all.toArray(new IType[all.size()]);
    }

    private void addSubtypes(Node node, List<IType> list) {
        if (node == null) {
            return;
        }

        list.addAll(Arrays.asList(node.subtypes));
        for (IType subtype : node.subtypes) {
            addSubtypes(nodes.get(subtype), list);
        }
    }

    private static class Node {
        IType type;
        IType supertype;
        IType[] subtypes;

        Node(IType type, IType supertype, IType[] subtypes) {
            ArgumentCheck.notNull(type);
            ArgumentCheck.notNull(subtypes);
            this.type = type;
            this.supertype = supertype;
            this.subtypes = subtypes;
        }
    }

    @Override
    public IAttribute[] getAllAttributes(IType type) {
        List<IAttribute> attributes = new ArrayList<IAttribute>();
        IType[] types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            IAttribute[] list = type2.getAttributes();
            for (Object nextAttr : list) {
                attributes.add((IAttribute)nextAttr);
            }
        }
        return attributes.toArray(new IAttribute[attributes.size()]);
    }

    @Override
    public IAttribute[] getAllAttributesRespectingOverride(IType type) {
        List<IAttribute> attributes = new ArrayList<IAttribute>();
        IType[] types = getAllSupertypesInclSelf(type);

        Map<String, IAttribute> overridden = new HashMap<String, IAttribute>();

        for (IType type2 : types) {
            IAttribute[] attrs = type2.getAttributes();
            for (int j = 0; j < attrs.length; j++) {
                if (!overridden.containsKey(attrs[j].getName())) {
                    attributes.add(attrs[j]);
                    if (attrs[j].isOverwrite()) {
                        overridden.put(attrs[j].getName(), attrs[j]);
                    }
                }
            }
        }
        return attributes.toArray(new IAttribute[attributes.size()]);
    }

    @Override
    public IMethod[] getAllMethods(IType type) {
        List<IMethod> methods = new ArrayList<IMethod>();
        IType[] types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            IMethod[] typeMethods = type2.getMethods();
            for (Object nextMethod : typeMethods) {
                methods.add((IMethod)nextMethod);
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    @Override
    public IValidationRule[] getAllRules(IType type) {
        List<IValidationRule> rules = new ArrayList<IValidationRule>();
        IType[] types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            if (type2 instanceof IPolicyCmptType) {
                IValidationRule[] typeRules = ((IPolicyCmptType)type2).getRules();
                for (Object nextRule : typeRules) {
                    rules.add((IValidationRule)nextRule);
                }
            }
        }
        return rules.toArray(new IValidationRule[rules.size()]);
    }

    @Override
    public IAttribute findAttribute(IType type, String attributeName) {
        IType[] types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            IAttribute a = type2.getAttribute(attributeName);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    @Override
    public boolean isPartOfHierarchy(String name) {
        if (name != null) {
            for (Node node : nodes.values()) {
                String nodeName = node.type.getQualifiedName();
                if (name.equals(nodeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSelectedType(String name) {
        return pcType.getQualifiedName().equals(name);
    }
}
