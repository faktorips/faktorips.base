/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A type's method.
 */
public interface IMethod extends IParameterContainer {

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$

    public final static String PROPERTY_PARAMETERS = "parameters"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "METHOD-"; //$NON-NLS-1$

    public final static String MSGCODE_DUBLICATE_SIGNATURE = MSGCODE_PREFIX + "duplicateSignature"; //$NON-NLS-1$

    public final static String MSGCODE_RETURN_TYPE_IS_INCOMPATIBLE = MSGCODE_PREFIX
            + "returnTypeIsIncompatibleWithOverriddenMethod"; //$NON-NLS-1$

    public final static String MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME = MSGCODE_PREFIX
            + "multipleUseOfSameParameterName"; //$NON-NLS-1$

    /**
     * Returns the type this method belongs to.
     */
    public IType getType();

    /**
     * Sets the method's name.
     */
    public void setName(String newName);

    /**
     * Returns the name of the value data type this method returns.
     */
    public String getDatatype();

    /**
     * Sets name of the value data type this method returns.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the method's (return) data type. Returns <code>null</code> if the data type can't be
     * found.
     * 
     * @param ipsProject The project which IPS object path is used to search. This is not
     *            necessarily the project this method belongs to.
     * 
     * @throws CoreException If an error occurs while searching.
     * @throws NullPointerException If <tt>ipsProject</tt> is <code>null</code>.
     */
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the modifier.
     */
    public Modifier getModifier();

    /**
     * Sets the modifier.
     */
    public void setModifier(Modifier newModifier);

    /**
     * Returns <code>true</code> if this is an abstract method, <code>false</code> otherwise.
     */
    public boolean isAbstract();

    /**
     * Sets if this is an abstract method or not.
     */
    public void setAbstract(boolean newValue);

    /**
     * Returns the Java modifier. Determined from the IPS modifier and the abstract flag.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier();

    /**
     * Returns <code>true</code> if this method overrides the <code>otherMethod</code>. This method
     * could override a method of any super type of this method's type, so <code>this.getType</code>
     * must be a sub type of <code>otherMethod.getType</code>. Further the method signature have to
     * be the same. Returns <code>false</code> otherwise. Note that the type of the methods return
     * values are not checked due to this case is not valid.
     * 
     * @param otherMethod The method that overrides this one.
     * 
     * @throws CoreException If there is an error in type hierarchy check.
     */
    public boolean overrides(IMethod otherMethod) throws CoreException;

    /**
     * Checks whether the signature of the <tt>otherMethod</tt> is the same as the signature of this
     * method. The signature is the same if the name of the method is equal, the number of
     * parameters is equal and the data types of the parameters are equal. Note that the return
     * values are not compared.
     * <p>
     * Returns <code>true</code> if the signature of the other method is the same as the signature
     * of this method.
     * 
     * @param otherMethod The other method which this method has to be compared with.
     */
    public boolean isSameSignature(IMethod otherMethod);

    /**
     * Returns the method overriding this one or <code>null</code> if no such method is found. The
     * search starts from the given type up the supertype hierarchy.
     * 
     * @param typeToSearchFrom The type to start the search from, must be a subtype of the type this
     *            method belongs to.
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IMethod findOverridingMethod(IType typeToSearchFrom, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the first method, that is overridden by this method.
     * 
     * @param ipsProject The project which IPS object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching.
     */
    public IMethod findOverriddenMethod(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the method's signature in string format, e.g. <tt>computePremium(base.Coverage,
     * base.Contract, Integer)</tt>.
     */
    public String getSignatureString();

}
