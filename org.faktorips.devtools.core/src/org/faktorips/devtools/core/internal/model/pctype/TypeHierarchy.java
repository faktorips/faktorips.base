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

package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.ArgumentCheck;

public class TypeHierarchy implements ITypeHierarchy {

    private IPolicyCmptType pcType;
    private Map<IPolicyCmptType, Node> nodes = new HashMap<IPolicyCmptType, Node>();
    private boolean containsCycle = false;

    /**
     * Creates a new type hierarchy containing all the given type's supertypes. Subtypes are not
     * resolved.
     */
    public final static TypeHierarchy getSupertypeHierarchy(IPolicyCmptType pcType) throws CoreException {
        IIpsProject project = pcType.getIpsProject();
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        IPolicyCmptType[] subtypes = new IPolicyCmptType[0];
        while (pcType != null) {
            String supertypeName = pcType.getSupertype();
            IPolicyCmptType supertype = null;
            if (!StringUtils.isEmpty(supertypeName)) {
                supertype = project.findPolicyCmptType(supertypeName);
                if (hierarchy.contains(supertype)) {
                    hierarchy.containsCycle = true;
                    supertype = null;
                }
            }
            hierarchy.add(new Node(pcType, supertype, subtypes));
            subtypes = new IPolicyCmptType[] { pcType };
            pcType = supertype;
        }
        return hierarchy;
    }

    /**
     * Creates a new type hierachy containing all the given type's subtypes. Supertypes are not
     * resolved.
     */
    public final static TypeHierarchy getSubtypeHierarchy(IPolicyCmptType pcType) throws CoreException {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        addSubtypes(hierarchy, pcType, null);
        return hierarchy;
    }

    private final static void addSubtypes(TypeHierarchy hierarchie, IPolicyCmptType pcType, IPolicyCmptType superType)
            throws CoreException {

        List<IPolicyCmptType> subtypes = findDirectSubtypes(pcType, hierarchie);
        Node node = new Node(pcType, superType, subtypes.toArray(new IPolicyCmptType[subtypes.size()]));
        hierarchie.add(node);
        for (IPolicyCmptType subtype : subtypes) {
            addSubtypes(hierarchie, subtype, pcType);
        }
    }

    private final static List<IPolicyCmptType> findDirectSubtypes(IPolicyCmptType pcType, TypeHierarchy hierarchy)
            throws CoreException {

        List<IPolicyCmptType> subtypes = new ArrayList<IPolicyCmptType>();
        IIpsProject project = pcType.getIpsProject();
        IIpsProject[] projects = pcType.getIpsModel().getIpsProjects();
        List<IIpsProject> searchedProjects = new ArrayList<IIpsProject>();
        for (IIpsProject project2 : projects) {
            if (searchedProjects.contains(project2)) {
                continue;
            }
            if (project2.equals(project) || project2.isReferencing(project)) {
                IIpsSrcFile[] pcTypeSrcFiles = project2.findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
                for (IIpsSrcFile pcTypeSrcFile : pcTypeSrcFiles) {
                    IPolicyCmptType candidate = (IPolicyCmptType)pcTypeSrcFile.getIpsObject();
                    if (!subtypes.contains(candidate) && pcType.equals(candidate.findSupertype(project2))) {
                        if (hierarchy.contains(candidate)) {
                            hierarchy.containsCycle = true;
                        } else {
                            subtypes.add(candidate);
                        }
                    }
                }
                searchedProjects.add(project2);
                IIpsProject[] refProjects = project2.getReferencedIpsProjects();
                for (IIpsProject refProject : refProjects) {
                    searchedProjects.add(refProject);
                }
            }
        }
        return subtypes;
    }

    private TypeHierarchy(IPolicyCmptType pcType) {
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

    private boolean contains(IPolicyCmptType type) {
        return nodes.containsKey(type);
    }

    @Override
    public IPolicyCmptType getType() {
        return pcType;
    }

    @Override
    public IPolicyCmptType getSupertype(IPolicyCmptType type) {
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
    public IPolicyCmptType[] getAllSupertypes(IPolicyCmptType type) {
        List<IPolicyCmptType> result = new ArrayList<IPolicyCmptType>();
        getAllSupertypes(type, result);
        return result.toArray(new IPolicyCmptType[result.size()]);
    }

    @Override
    public IPolicyCmptType[] getAllSupertypesInclSelf(IPolicyCmptType type) {
        List<IPolicyCmptType> result = new ArrayList<IPolicyCmptType>();
        result.add(type);
        getAllSupertypes(type, result);
        return result.toArray(new IPolicyCmptType[result.size()]);
    }

    private void getAllSupertypes(IPolicyCmptType type, List<IPolicyCmptType> result) {
        IPolicyCmptType supertype = getSupertype(type);
        while (supertype != null && !result.contains(supertype)) {
            result.add(supertype);
            supertype = getSupertype(supertype);
        }
    }

    @Override
    public boolean isSupertypeOf(IPolicyCmptType candidate, IPolicyCmptType subtype) {
        IPolicyCmptType currSupertype = getSupertype(subtype);
        if (currSupertype == null) {
            return false;
        }
        if (currSupertype.equals(candidate)) {
            return true;
        }
        return isSupertypeOf(candidate, currSupertype);
    }

    @Override
    public boolean isSubtypeOf(IPolicyCmptType candidate, IPolicyCmptType supertype) {
        IPolicyCmptType[] subtypes = getSubtypes(supertype);
        for (IPolicyCmptType subtype : subtypes) {
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
    public IPolicyCmptType[] getSubtypes(IPolicyCmptType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new IPolicyCmptType[0];
        }
        return node.subtypes;
    }

    @Override
    public IPolicyCmptType[] getAllSubtypes(IPolicyCmptType type) {
        Node node = nodes.get(type);
        if (node == null) {
            return new IPolicyCmptType[0];
        }

        ArrayList<IPolicyCmptType> all = new ArrayList<IPolicyCmptType>();
        addSubtypes(node, all);
        return all.toArray(new IPolicyCmptType[all.size()]);
    }

    private void addSubtypes(Node node, List<IPolicyCmptType> list) {
        if (node == null) {
            return;
        }

        list.addAll(Arrays.asList(node.subtypes));
        for (IPolicyCmptType subtype : node.subtypes) {
            addSubtypes(nodes.get(subtype), list);
        }
    }

    private static class Node {
        IPolicyCmptType type;
        IPolicyCmptType supertype;
        IPolicyCmptType[] subtypes;

        Node(IPolicyCmptType type, IPolicyCmptType supertype, IPolicyCmptType[] subtypes) {
            ArgumentCheck.notNull(type);
            ArgumentCheck.notNull(subtypes);
            this.type = type;
            this.supertype = supertype;
            this.subtypes = subtypes;
        }
    }

    @Override
    public IPolicyCmptTypeAttribute[] getAllAttributes(IPolicyCmptType type) {
        List<IPolicyCmptTypeAttribute> attributes = new ArrayList<IPolicyCmptTypeAttribute>();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (IPolicyCmptType type2 : types) {
            List<?> list = ((PolicyCmptType)type2).getAttributeList();
            for (Object nextAttr : list) {
                attributes.add((IPolicyCmptTypeAttribute)nextAttr);
            }
        }
        return attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
    }

    @Override
    public IPolicyCmptTypeAttribute[] getAllAttributesRespectingOverride(IPolicyCmptType type) {
        List<IPolicyCmptTypeAttribute> attributes = new ArrayList<IPolicyCmptTypeAttribute>();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);

        Map<String, IAttribute> overridden = new HashMap<String, IAttribute>();

        for (IPolicyCmptType type2 : types) {
            IPolicyCmptTypeAttribute[] attrs = type2.getPolicyCmptTypeAttributes();
            for (int j = 0; j < attrs.length; j++) {
                if (!overridden.containsKey(attrs[j].getName())) {
                    attributes.add(attrs[j]);
                    if (attrs[j].isOverwrite()) {
                        overridden.put(attrs[j].getName(), attrs[j]);
                    }
                }
            }
        }
        return attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
    }

    @Override
    public IMethod[] getAllMethods(IPolicyCmptType type) {
        List<IMethod> methods = new ArrayList<IMethod>();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (IPolicyCmptType type2 : types) {
            List<?> typeMethods = ((PolicyCmptType)type2).getMethodList();
            for (Object nextMethod : typeMethods) {
                methods.add((IMethod)nextMethod);
            }
        }
        return methods.toArray(new IMethod[methods.size()]);
    }

    @Override
    public IValidationRule[] getAllRules(IPolicyCmptType type) {
        List<IValidationRule> rules = new ArrayList<IValidationRule>();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (IPolicyCmptType type2 : types) {
            List<?> typeRules = ((PolicyCmptType)type2).getRulesList();
            for (Object nextRule : typeRules) {
                rules.add((IValidationRule)nextRule);
            }
        }
        return rules.toArray(new IValidationRule[rules.size()]);
    }

    @Override
    public IPolicyCmptTypeAttribute findAttribute(IPolicyCmptType type, String attributeName) {
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (IPolicyCmptType type2 : types) {
            IPolicyCmptTypeAttribute a = type2.getPolicyCmptTypeAttribute(attributeName);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

}
