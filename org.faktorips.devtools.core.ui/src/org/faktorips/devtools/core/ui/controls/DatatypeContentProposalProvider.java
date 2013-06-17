/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controls.contentproposal.ICachedContentProposalProvider;

/**
 * An <tt>IContentProposalProvider</tt> for <tt>Datatype</tt> proposal.
 * 
 * @author hbaagil
 */

public class DatatypeContentProposalProvider implements ICachedContentProposalProvider {

    private IIpsProject ipsProject;
    private Datatype[] dataType;
    private boolean returnType;
    private boolean parameterType;

    private SearchPattern searchPattern = new SearchPattern();

    public DatatypeContentProposalProvider(IIpsProject ipsProject, boolean returnType, boolean parameterType) {
        this.ipsProject = ipsProject;
        this.returnType = returnType;
        this.parameterType = parameterType;
    }

    private Datatype[] getDataType() {
        return dataType;
    }

    private void setDataType(Datatype[] dataType) {
        this.dataType = dataType;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        checkDataType();

        String prefix = StringUtils.left(contents, position);
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        for (Datatype type : getDataType()) {
            String unqualifiedName = type.getName();
            if (searchPattern.matches(unqualifiedName)) {
                DatatypeContentProposal contentProposal = new DatatypeContentProposal(type);
                result.add(contentProposal);
            }
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    private void checkDataType() {
        if (getDataType() == null) {
            setDataType(findDataType());
        }
    }

    private Datatype[] findDataType() {
        try {
            if (returnType) {
                return ipsProject.findDatatypes(true, true, true);
            } else if (parameterType) {
                return ipsProject.findDatatypes(false, false, true, null, true);
            } else {
                return new Datatype[] {};
            }
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public void clearCache() {
        setDataType(null);
    }

}
