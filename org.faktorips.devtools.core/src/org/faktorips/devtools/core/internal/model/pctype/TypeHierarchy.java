package org.faktorips.devtools.core.internal.model.pctype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IMethod;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;
import org.faktorips.devtools.core.model.pctype.ITypeHierarchy;
import org.faktorips.devtools.core.model.product.IProductCmptRelation;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeRelation;
import org.faktorips.util.ArgumentCheck;


/**
 * Represents the hierarchy formed by the subtype/supertype relationship
 * between types. 
 */
public class TypeHierarchy implements ITypeHierarchy {

    private IPolicyCmptType pcType;
    private Map nodes = new HashMap();

    /**
     * Creates a new type hierachy containing all the given type's supertypes.
     * Subtypes are not resolved.
     */
    final static TypeHierarchy getSupertypeHierarchy(IPolicyCmptType pcType) throws CoreException {
        IIpsProject project = pcType.getIpsProject();
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        IPolicyCmptType[] subtypes = new IPolicyCmptType[0];
        while (pcType!=null) {
            String supertypeName = pcType.getSupertype();
            IPolicyCmptType supertype = null;
            if (!StringUtils.isEmpty(supertypeName)) {
                supertype = project.findPolicyCmptType(supertypeName);            
            }
            hierarchy.add(new Node(pcType, supertype, subtypes));
            pcType = supertype;
        }
        return hierarchy;
    }
    
    /**
     * Creates a new type hierachy containing all the given type's subtypes.
     * Supertypes are not resolved.
     */
    final static TypeHierarchy getSubtypeHierarchie(IPolicyCmptType pcType) throws CoreException {
        TypeHierarchy hierarchy = new TypeHierarchy(pcType);
        addSubtypes(hierarchy, pcType, null);
        return hierarchy;
    }
    
    private final static void addSubtypes(TypeHierarchy hierarchie, IPolicyCmptType pcType, IPolicyCmptType superType) throws CoreException {
        List subtypes = findDirectSubtypes(pcType);
        Node node = new Node(pcType, superType, (IPolicyCmptType[])subtypes.toArray(new IPolicyCmptType[subtypes.size()]));
        hierarchie.add(node);
        for (Iterator it=subtypes.iterator(); it.hasNext(); ) {
            addSubtypes(hierarchie, (IPolicyCmptType)it.next(),pcType);
        }
    }
    
    private final static List findDirectSubtypes(IPolicyCmptType pcType) throws CoreException {
        IIpsProject project = pcType.getIpsProject();
        List subtypes = new ArrayList();
        String qName = pcType.getQualifiedName();
        IIpsObject[] pcTypes = project.findIpsObjects(IpsObjectType.POLICY_CMPT_TYPE);
        for (int i=0; i<pcTypes.length; i++) {
            IPolicyCmptType candidate = (IPolicyCmptType)pcTypes[i];
            if (candidate.getSupertype().equals(qName)) {
                subtypes.add(candidate);
            }
        }
        return subtypes;
    }
    
    private TypeHierarchy(IPolicyCmptType pcType) {
        this.pcType = pcType;
    }
    
    private void add(Node node) {
        nodes.put(node.type, node);
    }
    
    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#getType()
     */
    public IPolicyCmptType getType() {
        return pcType;
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#getSupertype(org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#getAllSupertypesInclSelf(org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
     */
    public IPolicyCmptType[] getAllSupertypesInclSelf(IPolicyCmptType type) {
        List result = new ArrayList();
        result.add(type);
        getAllSupertypes(type, result);
        return (IPolicyCmptType[])result.toArray(new IPolicyCmptType[result.size()]);
    }
    
    private void getAllSupertypes(IPolicyCmptType type, List result) {
        IPolicyCmptType supertype = getSupertype(type);
        while (supertype!=null) {
            result.add(supertype);
            supertype = getSupertype(supertype);
        }
    }
    
    /** 
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#isSupertypeOf(org.faktorips.devtools.core.model.pctype.IPolicyCmptType, org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
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
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#isSubtypeOf(org.faktorips.devtools.core.model.pctype.IPolicyCmptType, org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#getAllAttributes(org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
     */
    public IAttribute[] getAllAttributes(IPolicyCmptType type) {
        List attributes = new ArrayList();
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            attributes.addAll(((PolicyCmptType)types[i]).getAttributeList());
        }
        return (IAttribute[])attributes.toArray(new IAttribute[attributes.size()]);
    }
    
    /**
     * Overridden IMethod.
     *
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#getAllMethods(org.faktorips.devtools.core.model.pctype.IPolicyCmptType)
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
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#findAttribute(org.faktorips.devtools.core.model.pctype.IPolicyCmptType, java.lang.String)
     */
    public IAttribute findAttribute(IPolicyCmptType type, String attributeName) {
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            IAttribute a = types[i].getAttribute(attributeName);
            if (a!=null) {
                return a;
            }
        }
        return null;
    }

    /**
     * Overridden method.
     * @see org.faktorips.devtools.core.model.pctype.ITypeHierarchy#findRelation(org.faktorips.devtools.core.model.pctype.IPolicyCmptType, java.lang.String)
     */ 
    public IRelation findRelation(IPolicyCmptType type, String targetRole) {
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
            IRelation r = types[i].getRelation(targetRole);
            if (r!=null) {
                return r;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public IProductCmptTypeRelation findRelationOnProductSide(IPolicyCmptType type, String relationName) {
        IPolicyCmptType[] types = getAllSupertypesInclSelf(type);
        for (int i=0; i<types.length; i++) {
        	IProductCmptTypeRelation relation = (new ProductCmptType(types[i])).getRelation(relationName);
            if (relation!=null) {
                return relation;
            }
        }
        return null;
    }
}
