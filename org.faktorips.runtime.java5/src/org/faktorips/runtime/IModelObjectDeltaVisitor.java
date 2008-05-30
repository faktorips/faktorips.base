 package org.faktorips.runtime;

/**
 * Visitor to visit a model object delta. 
 * 
 * <p><strong>
 * The delta support is experimental in this version.
 * The API might change without notice until it is finalized in a future version.
 * </strong>
 * 
 * @author Jan Ortmann
 */
public interface IModelObjectDeltaVisitor {

	/**
	 * Visits the given model object delta.
	 * 
	 * @param delta The delta to visit
     * 
	 * @return <code>true</code> if the delta's children should be visited;
	 * <code>false</code> if they should be skipped.
	 */
	public boolean visit(IModelObjectDelta delta);
}
