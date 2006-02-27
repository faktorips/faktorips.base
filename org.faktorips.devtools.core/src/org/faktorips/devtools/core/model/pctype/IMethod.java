package org.faktorips.devtools.core.model.pctype;

import org.eclipse.core.runtime.CoreException;

/**
 *
 */
public interface IMethod extends IMember {

    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$
    public final static String PROPERTY_MODIFIER = "modifier"; //$NON-NLS-1$
    public final static String PROPERTY_ABSTRACT = "abstract"; //$NON-NLS-1$
    public final static String PROPERTY_PARAMETERS = "parameters"; //$NON-NLS-1$
    public final static String PROPERTY_BODY = "body"; //$NON-NLS-1$
    
    public final static String PROPERTY_PARAM_NAME = "param.name"; //$NON-NLS-1$
    public final static String PROPERTY_PARAM_DATATYPE = "param.datatype"; //$NON-NLS-1$
    
    /**
     * Returns the policy component type this method belongs to.
     */
    public IPolicyCmptType getPolicyCmptType();
    
    public String getDatatype();
    
    public void setDatatype(String newDatatype);
    
    public Modifier getModifier();
    
    public void setModifier(Modifier newModifier);
    
    public boolean isAbstract();
    
    public void setAbstract(boolean newValue);
    
    /**
     * Returns the Java modifier. Determined from the ips modifier and the abstract flag.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier();

    /**
     * Returns the method's parameters. Returns an empty array if the mehthod
     * doeen't have any parameter.
     */
    public Parameter[] getParameters();
    
    /**
     * Returns the parameter names.
     */
    public String[] getParameterNames();
    
    /**
     * Returns the parameter types.
     */
    public String[] getParameterTypes();
    
    /**
     * Returns the paramter type signatures for the method's parameters.
     * 
     * @see org.eclipse.jdt.core.Signature#createTypeSignature(java.lang.String, boolean);
     * 
     * @throws CoreException if an error occurs while resolving the parameters'
     * datatypes.
     */
    public String[] getParameterTypeSignatures() throws CoreException;
    
    /**
     * Returns the number of parameters.
     */
    public int getNumOfParameters();
    
    /**
     * Sets the method's parameters.
     * 
     * @throws NullPointerException if params if <code>null</code>.
     */
    public void setParameters(Parameter[] params);
    
    /**
     * Returns the method's body sourcecode.
     */
    public String getBody();
    
    /**
     * Sets the method's body sourcecode.
     * 
     * @throws IllegalArgumentException if sourcecode is null.
     */
    public void setBody(String sourcecode);
    
    /**
     * Returns <code>true</code> if the other method has the same name, the same numer of parameters
     * and each parameter has the same datatype as the parameter in this method. Returns <code>false</code> otherwise.
     * Note that the return type is not checked. 
     */
    public boolean isSame(IMethod method);
    
    
}
