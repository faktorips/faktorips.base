package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.util.memento.MementoSupport;


/**
 *
 */
public interface IIpsObject extends IIpsElement, IExtensionPropertyAccess, Validatable, XmlSupport, MementoSupport, Described {
    
    /**
     * Returns the object's type.
     */
    public IpsObjectType getIpsObjectType();
    
    /**
     * Returns the IPS source file this object is stored in.
     */
    public IIpsSrcFile getIpsSrcFile();
    
    /**
     * Returns the object's qualified name. The qualified name is the name of the ips package fragment's
     * name followed by a dot followed by the object's unqualified name. So this is basically the
     * same concept as the qualified name of Java classes. 
     * <p>
     * <b>Example:</b><br>
     * The qualified name of an ips object called 'MotorCoverage' in the package fragment 
     * 'mycompany.motor' has the qualified name 'mycompany.motor.MotorCoverage'.
     */
    public String getQualifiedName();
    
    /**
     * Returns the qualified name type for this ips object which is the combination of this ips objects qualified name and
     * its ips object type. I
     */
    public QualifiedNameType getQualifiedNameType();
    
    /**
     * Returns the IPS package fragment the object is contained in.
     */
    public IIpsPackageFragment getIpsPackageFragment();
    
    /**
     * Returns the qualified name type object of the ips objects this one depends on.
     * E.g. a policy component type depends on its supertype.
     * <p>
     * We use the qualified name type instead of ips object references because an 
     * object can depend on another object that does not exist, e.g. because the other
     * object it has been deleted. However, if the deleted object is created again, 
     * we must rebuild this dependant object so that the problem marker is removed.
     * @throws CoreException 
     */
    public QualifiedNameType[] dependsOn() throws CoreException;
    
    /**
     * Returns the Java type that correspond to this IPS object and is of the
     * indicated kind.
     * 
     * @param kind A kind constant identifying the type of compilation unit.
     * @return The corresponding compilation unit. Note that the unit might not
     * exists!
     * 
     * @deprecated
     * @throws IllegalArgumentException if the kind constant is illegal.
     * @throws CoreException if an errors occurs while accessing the Java type.   
     */
    public IType getJavaType(int kind) throws CoreException;
    
    /**
     * Returns all Java types that correspond to this IPS object. Note that
     * none of the returned Java types must exist.
     * 
     * @deprecated
     */
    public IType[] getAllJavaTypes() throws CoreException;
}
