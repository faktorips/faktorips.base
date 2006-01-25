package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * A product component type. Currently the product component type represents a view
 * on the policy component type that gives access to product relevant information.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptType extends IIpsObject {

	/**
	 * Returns the qualified name of the policy component type this is a 
	 * product component type for.
	 */
	public String getPolicyCmptyType();
	
	/**
	 * Returns the policy component type this is a product component type for
	 * or <code>null</code> if the policy component type can't be found.
	 * 
	 * @throws CoreException if an erros occurs while searching for the type.
	 */
	public IPolicyCmptType findPolicyCmptyType() throws CoreException;
}
