package org.faktorips.devtools.core.ui.editors.pctype;

public interface ParameterListChangeListener {

	/**
	 * Gets fired when the given parameter has changed
	 * @param parameter the parameter that has changed.
	 */
	public void parameterChanged(ParameterInfo parameter);

	/**
	 * Gets fired when the given parameter has been added
	 * @param parameter the parameter that has been added.
	 */
	public void parameterAdded(ParameterInfo parameter);
	
	
	/**
	 * Gets fired if the parameter list got modified by reordering or removing 
	 * parameters (note that adding is handled by <code>parameterAdded</code>))
	 */
	public void parameterListChanged();
}
