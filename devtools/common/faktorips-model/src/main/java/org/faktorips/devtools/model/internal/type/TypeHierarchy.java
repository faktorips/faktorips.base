/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IValidationRule;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.type.IMethod;
import org.faktorips.devtools.model.type.IType;
import org.faktorips.devtools.model.type.ITypeHierarchy;
import org.faktorips.util.ArgumentCheck;

public class TypeHierarchy implements ITypeHierarchy {

    private IType pcType;
    private Map<IType, Node> nodes = new HashMap<>();
    private boolean containsCycle = false;

    private TypeHierarchy(IType pcType) {
        this.pcType = pcType;
    }

    /**
     * Creates a new type hierarchy containing all the given type's supertypes. Subtypes are not
     * resolved.
     */
    public static final TypeHierarchy getSupertypeHierarchy(IType pcType) {
        IIpsProject project = pcType.getIpsProject();
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        List<IType> subtypes = new ArrayList<>();
        IType type = pcType;
        while (type != null) {
            IType supertype = type.findSupertype(project);
            if (hierarchy.contains(supertype)) {
                hierarchy.containsCycle = true;
                supertype = null;
            }
            hierarchy.add(new Node(type, supertype, subtypes));
            subtypes = new ArrayList<>();
            subtypes.add(type);
            type = supertype;
        }
        return hierarchy;
    }

    /**
     * Creates a new type hierarchy containing all the given type's subtypes. Supertypes are not
     * resolved.
     */
    public static final TypeHierarchy getSubtypeHierarchy(IType pcType) {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        hierarchy.addSubtypes(pcType, null, true, pcType.getIpsProject());
        return hierarchy;
    }

    /**
     * Creates a new type hierarchy containing all the given type's subtypes. Supertypes are not
     * resolved.
     */
    public static final TypeHierarchy getSubtypeHierarchy(IType pcType, IIpsProject searchProject) {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        hierarchy.addSubtypes(pcType, null, false, searchProject);
        return hierarchy;
    }

    /**
     * Creates a new type hierarchy containing all the given type's subtypes and supertypes.
     */
    public static final TypeHierarchy getTypeHierarchy(IType pcType) {
        TypeHierarchy hierarchy = getSupertypeHierarchy(pcType);
        Node pcTypeNode = hierarchy.nodes.get(pcType);
        List<IType> subtypes = hierarchy.searchDirectSubtypes(pcType, true, pcType.getIpsProject());
        for (IType subtype : subtypes) {
            hierarchy.addSubtypes(subtype, pcType, true, pcType.getIpsProject());
        }
        pcTypeNode.subtypes = subtypes;
        return hierarchy;
    }

    private void addSubtypes(IType pcType, IType superType, boolean searchReferencingProjects, IIpsProject ipsProject) {
        List<IType> subtypes = searchDirectSubtypes(pcType, searchReferencingProjects, ipsProject);
        Node node = new Node(pcType, superType, subtypes);
        add(node);
        for (IType subtype : subtypes) {
            addSubtypes(subtype, pcType, searchReferencingProjects, ipsProject);
        }
    }

    private List<IType> searchDirectSubtypes(IType type, boolean searchReferencingProjects, IIpsProject ipsProject) {
        List<IType> subtypes = new ArrayList<>();
        if (searchReferencingProjects) {
            IIpsProject[] referencingProjects = ipsProject.findReferencingProjectLeavesOrSelf();
            for (IIpsProject referencingProject : referencingProjects) {
                findDirectSubtypes(type, subtypes, referencingProject);
            }
        } else {
            findDirectSubtypes(type, subtypes, ipsProject);
        }
        return subtypes;
    }

    private void findDirectSubtypes(IType type, List<IType> subtypes, IIpsProject project) {
        IIpsSrcFile[] candidateSrcFiles = project.findIpsSrcFiles(type.getIpsObjectType());
        for (IIpsSrcFile candidateSrcFile : candidateSrcFiles) {
            String candidateSuperTypeName = candidateSrcFile.getPropertyValue(IType.PROPERTY_SUPERTYPE);
            if (type.getQualifiedName().equals(candidateSuperTypeName)) {
                IIpsObject candidateObject = candidateSrcFile.getIpsObject();
                if (candidateObject instanceof IType) {
                    IType candidateType = (IType)candidateObject;
                    if (!subtypes.contains(candidateObject)) {
                        if (contains(candidateType)) {
                            containsCycle = true;
                        } else {
                            subtypes.add(candidateType);
                        }
                    }
                }
            }
        }
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
    public List<IType> getAllSupertypes(IType type) {
        List<IType> result = new ArrayList<>();
        getAllSupertypes(type, result);
        return result;
    }

    @Override
    public List<IType> getAllSupertypesInclSelf(IType type) {
        List<IType> result = new ArrayList<>();
        result.add(type);
        getAllSupertypes(type, result);
        return result;
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
        List<IType> subtypes = getSubtypes(supertype);
        for (IType subtype : subtypes) {
            if (subtype.equals(candidate) || isSubtypeOf(candidate, subtype)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IType> getSubtypes(IType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new ArrayList<>();
        }
        return node.subtypes;
    }

    @Override
    public List<IType> getAllSubtypes(IType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new ArrayList<>();
        }

        ArrayList<IType> all = new ArrayList<>();
        addSubtypes(node, all);
        return all;
    }

    private void addSubtypes(Node node, List<IType> list) {
        if (node == null) {
            return;
        }

        list.addAll(node.subtypes);
        for (IType subtype : node.subtypes) {
            addSubtypes(nodes.get(subtype), list);
        }
    }

    @Override
    public List<IAttribute> getAllAttributes(IType type) {
        List<IAttribute> attributes = new ArrayList<>();
        List<IType> types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            List<? extends IAttribute> list = type2.getAttributes();
            for (Object nextAttr : list) {
                attributes.add((IAttribute)nextAttr);
            }
        }
        return attributes;
    }

    @Override
    public List<IAttribute> getAllAttributesRespectingOverride(IType type) {
        List<IAttribute> attributes = new ArrayList<>();
        List<IType> types = getAllSupertypesInclSelf(type);

        Map<String, IAttribute> overridden = new HashMap<>();

        for (IType type2 : types) {
            List<? extends IAttribute> attrs = type2.getAttributes();
            for (IAttribute attribute : attrs) {
                if (!overridden.containsKey(attribute.getName())) {
                    attributes.add(attribute);
                    if (attribute.isOverwrite()) {
                        overridden.put(attribute.getName(), attribute);
                    }
                }
            }
        }
        return attributes;
    }

    @Override
    public List<IMethod> getAllMethods(IType type) {
        List<IMethod> methods = new ArrayList<>();
        List<IType> types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            List<IMethod> typeMethods = type2.getMethods();
            for (IMethod nextMethod : typeMethods) {
                methods.add(nextMethod);
            }
        }
        return methods;
    }

    @Override
    public List<IValidationRule> getAllRules(IType type) {
        List<IValidationRule> rules = new ArrayList<>();
        List<IType> types = getAllSupertypesInclSelf(type);
        for (IType type2 : types) {
            if (type2 instanceof IPolicyCmptType) {
                List<IValidationRule> typeRules = ((IPolicyCmptType)type2).getValidationRules();
                for (Object nextRule : typeRules) {
                    rules.add((IValidationRule)nextRule);
                }
            }
        }
        return rules;
    }

    @Override
    public IAttribute findAttribute(IType type, String attributeName) {
        List<IType> types = getAllSupertypesInclSelf(type);
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

    @Override
    public boolean isSelectedType(String name) {
        return pcType.getQualifiedName().equals(name);
    }

    private static class Node {
        IType type;
        IType supertype;
        List<IType> subtypes;

        Node(IType type, IType supertype, List<IType> subtypes) {
            ArgumentCheck.notNull(type);
            ArgumentCheck.notNull(subtypes);
            this.type = type;
            this.supertype = supertype;
            this.subtypes = subtypes;
        }
    }
}
