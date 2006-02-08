package org.faktorips.devtools.core.builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IParameterIdentifierResolver;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.IdentifierResolver;

public class TestIpsArtefactBuilderSet implements IIpsArtefactBuilderSet {

    public final static String ID = "testbuilderset";
    
    private IIpsArtefactBuilder[] artefactBuilders;
    
    public TestIpsArtefactBuilderSet(IIpsArtefactBuilder[] builders){
        artefactBuilders = builders;
    }
    
    public IIpsArtefactBuilder[] getArtefactBuilders() {
        return artefactBuilders;
    }

    public void setLogger(ILog logger) {
    }

    public void setId(String id) {
    }

    public void setLabel(String label) {
    }

    public String getId() {
        return ID;
    }

    public String getLabel() {
        return getId();
    }

    public void initialize() throws CoreException {
    }

    public boolean isSupportTableAccess() {
        return false;
    }

    public CompilationResult getTableAccessCode(ITableAccessFunction fct, CompilationResult[] argResults) throws CoreException {
        return null;
    }

	public IdentifierResolver getFlIdentifierResolver() {
		return null;
	}

	public boolean isSupportFlIdentifierResolver() {
		return false;
	}

	public IParameterIdentifierResolver getFlParameterIdentifierResolver() {
		return null;
	}

}
