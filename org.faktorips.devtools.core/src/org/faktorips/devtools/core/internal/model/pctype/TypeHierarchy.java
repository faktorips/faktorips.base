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
import java.util.Iterator;
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
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.util.ArgumentCheck;


/**
 */
public class TypeHierarchy implements ITypeHierarchy {

    private IPolicyCmptType pcType;
    private Map nodes = new HashMap();
    private boolean containsCycle = false;

    /**
     * Creates a new type hierachy containing all the given type's supertypes.
     * Subtypes are not resolved.
     */
    public final static TypeHierarchy getSupertypeHierarchy(IPolicyCmptType pcType) throws CoreException {
        IIpsProject project = pcType.getIpsProject();
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        IPolicyCmptType[] subtypes = new IPolicyCmptType[0];
        while (pcType!=null) {
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
            subtypes = new IPolicyCmptType[]{pcType};
            pcType = supertype;
        }
        return hierarchy;
    }
    
    /**
     * Creates a new type hierachy containing all the given type's subtypes.
     * Supertypes are not resolved.
     */
    public final static TypeHierarchy getSubtypeHierarchy(IPolicyCmptType pcType) throws CoreException {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        addSubtypes(hierarchy, pcType, null);
        return hierarchy;
    }
    
    private final static void addSubtypes(TypeHierarchy hierarchie, IPolicyCmptType pcType, IPolicyCmptType superType) throws CoreException {
        List subtypes = findDirectSubtypes(pcType, hierarchie);
        Node node = new Node(pcType, superType, (IPolicyCmptType[])subtypes.toArray(new IPolicyCmptType[subtypes.size()]));
        hierarchie.add(node);
        for (Iterator it=subtypes.iterator(); it.hasNext(); ) {
            addSubtypes(hierarchie, (IPolicyCmptType)it.next(),pcType);
        }
    }
    
    private final static List findDirectSubtypes(IPolicyCmptType pcType, TypeHierarchy hierarchy) throws CoreException {
        List subtypes = new ArrayList();
        IIpsProject project = pcType.getIpsProject();
        IIpsProject[] projects = pcType.getIpsModel().getIpsProjects();
        List searchedProjects = new ArrayList();
        for (int i = 0; i < projects.length; i++) {
            if (searchedProjects.contains(projects[i])) {
                continue;
            }
            if (projects[i].equals(project) || projects[i].isReferencing(project)) {
                IIpsSrcFile[] pcTypeSrcFiles = projects[i].findIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
                for (int j=0; j<pcTypeSrcFiles.length; j++) {
                    IPolicyCmptType candidate = (IPolicyCmptType)pcTypeSrcFiles[j].getIpsObject();
                    if (!subtypes.contains(candidate) && pcType.equals(candidate.findSupertype(projects[i])))  {
                        if (hierarchy.contains(candidate)) {
                            hierarchy.containsCycle = true;
                        } else {
                            subtypes.add(candidate);
                        }
                    }
                }
                searchedProjects.add(projects[i]);
                IIpsProject[] refProjects = projects[i].getReferencedIpsProjects();
                for (int k=0; k<refProjects.length; k++) {
                    searchedProjects.add(refProjects[k]);
                }
            }
        }
        return subtypes;
    }
    
    private TypeHierarchy(IPolicyCmptType pcType) {
        this.pcType = pcType;
    }
    
    /**
     * {@inheritDoc}
     */
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
    
    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType getType() {
        return pcType;
    }
    
    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType getSupertype(IPolicyCmptType type) {
        Node node = (Node)nodes.get(type);
        if (node==null) {
            return null;
        }
        return node.supertype;
    }
    
    /**
     * Returns the type's supertypes by travelling up the hierarchy. The first
     * element in the array (if any) is the given type's direkt supertype.
     */
    public IPolicyCmptType[] getAllSupertypes(IPolicyCmptType type) {
        List result = new ArrayList();
        getAllSupertypes(type, result);
        return (IPolicyCmptType[])result.toArray(new IPolicyCmptType[result.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public IPolicyCmptType[] getAllSupertypesInclSelf(IPolicyCmptType type) {
        List result = new ArrayList();
        result.add(type);
        getAllSupertypes(type, result);
        return (IPolicyCmptType[])result.toArray(new IPolicyCmptType[result.size()]);
    }
    
    private void getAllSupertypes(IPolicyCmptType type, List result) {
        IPolicyCmptType supertype = getSupertype(type);
        while (supertype!=null && !result.contains(supertype)) {
            result.add(supertype);
            supertype = getSupertype(supertype);
        }
    }
    
    /** 
     * {@inheritDoc}
     */
    public boolean isSupertypeOf(IPolicyCmptType candidate, IPolicyCmptType subtype) {
        IPolicyCmptType currSupertype = getSupertype(subtype);
        if (currSupertype==null) {
            return false;
        }
        if (currSupertype.equals(candidate)) {
            return true;
        }
        return isSupertypeOf(candidate, currSupertype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSubtypeOf(IPolicyCmptType candidate, IPolicyCmptType supertype) {
        IPolicyCmptType[] subtypes = getSubtypes(supertype);
        for (int i=0; i<subtypes.length; i++) {
            if (subtypes[i].equals(candidate)) {
                return true;
            }
            if (isSubtypeOf(candidate, subtypes[i])) {
                return true;
            }
        }
        return false;
    }
    
    public IPolicyCmptType[] getSubtypes(IPolicyCmptType type) {
        Node node = (Node)nodes.get(type);
        if (node==null) {
            return new IPolicyCmptType[0];
        }
        return node.subtypes;
    }
    
    public IPolicyCmptType[] getAllSubtypes(IPolicyCmptType type) {
        Node node = (Node) nodes.get(type);
		if (node == null) {
			return new IPolicyCmptType[0];
		}
		
		ArrayList all = new ArrayList();
		addSubtypes(node, all);
		return (IPolicyCmptType[])all.toArray(new IPolicyCmptType[all.size()]);
    }
    
    private void addSubtypes(Node node, List list) {
    	if (node == null) {
    		return;
    	}
    	
    	list.addAll(Arrays.asList(node.subtypes));
    	for (int i = 0; i < node.subtypes.length; i++) {
			addSubtypes((Node)nodes.get(node.subtypes[i]), list);
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

    /** 
     * {@inheritDoc}
     */
    public IPolicyCmptTypeAttribute[] getAllAttributes(IPolicyCmptType type) {
        List attributes = new ArrayList();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            attributes.addAll(((PolicyCmptType)types[i]).getAttributeList());
        }
        return (IPolicyCmptTypeAttribute[])attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
    }
    
    /**
	 * {@inheritDoc}
	 */
	public IPolicyCmptTypeAttribute[] getAllAttributesRespectingOverride(IPolicyCmptType type) {
        List attributes = new ArrayList();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        
        Map overridden = new HashMap();
        
        for (int i=0; i<types.length; i++) {
        	IPolicyCmptTypeAttribute[] attrs = types[i].getPolicyCmptTypeAttributes();
        	for (int j = 0; j < attrs.length; j++) {
				if (!overridden.containsKey(attrs[j].getName())) {
					attributes.add(attrs[j]);
					if (attrs[j].isOverwrite()) {
						overridden.put(attrs[j].getName(), attrs[j]);
					}
				}
			}
        }
        return (IPolicyCmptTypeAttribute[])attributes.toArray(new IPolicyCmptTypeAttribute[attributes.size()]);
	}
	
	/**
     * {@inheritDoc}
	 */
    public IMethod[] getAllMethods(IPolicyCmptType type) {
        List methods = new ArrayList();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            methods.addAll(((PolicyCmptType)types[i]).getMethodList());
        }
        return (IMethod[])methods.toArray(new IMethod[methods.size()]);
    }
    
    /**
     * {@inheritDoc}
     */
	public IValidationRule[] getAllRules(IPolicyCmptType type) {
        List rules = new ArrayList();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            rules.addAll(((PolicyCmptType)types[i]).getRulesList());
        }
        return (IValidationRule[])rules.toArray(new IValidationRule[rules.size()]);
    }

    /** 
     * {@inheritDoc}
	 */
    public IPolicyCmptTypeAttribute findAttribute(IPolicyCmptType type, String attributeName) {
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            IPolicyCmptTypeAttribute a = types[i].getPolicyCmptTypeAttribute(attributeName);
            if (a!=null) {
                return a;
            }
        }
        return null;
    }

}
