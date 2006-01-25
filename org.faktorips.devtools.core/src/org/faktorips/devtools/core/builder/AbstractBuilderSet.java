package org.faktorips.devtools.core.builder;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;

/**
 * Abstract implementation that can be used as a base class for real builder sets. 
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractBuilderSet implements IIpsArtefactBuilderSet {

	private String id;
	private String label;
	
	public AbstractBuilderSet() {
		super();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String toString() {
		return id;
	}
}
