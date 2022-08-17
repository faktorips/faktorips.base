/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.controls.contentproposal.AbstractPrefixContentProposalProvider;
import org.faktorips.devtools.core.ui.controls.contentproposal.ICachedContentProposalProvider;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * An {@link IContentProposalProvider} for {@link Datatype} proposals.
 */
public class DatatypeContentProposalProvider extends AbstractPrefixContentProposalProvider
        implements ICachedContentProposalProvider {

    private IIpsProject ipsProject;
    private Datatype[] dataType;

    private boolean includeVoid;
    private boolean valuetypesOnly;
    private boolean includePrimitives;
    private boolean includeAbstract;
    private List<Datatype> excludedDatatypes;

    private SearchPattern searchPattern = new SearchPattern();

    public DatatypeContentProposalProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;

        includePrimitives = true;
        excludedDatatypes = null;
    }

    public void setIncludeVoid(boolean value) {
        includeVoid = value;
    }

    public boolean isIncludeVoid() {
        return includeVoid;
    }

    public void setIncludeAbstract(boolean includeAbstract) {
        this.includeAbstract = includeAbstract;
    }

    public boolean isIncludeAbstract() {
        return includeAbstract;
    }

    public void setValueDatatypesOnly(boolean value) {
        valuetypesOnly = value;
    }

    public boolean isValueDatatypesOnly() {
        return valuetypesOnly;
    }

    public boolean isIncludePrimitives() {
        return includePrimitives;
    }

    public void setIncludePrimitives(boolean includePrimitives) {
        this.includePrimitives = includePrimitives;
    }

    public void setExcludedDatatypes(List<Datatype> excludedDatatypes) {
        this.excludedDatatypes = excludedDatatypes;
    }

    public List<Datatype> getExcludedDatatypes() {
        if (excludedDatatypes != null) {
            return Collections.unmodifiableList(excludedDatatypes);
        }
        return null;
    }

    private Datatype[] getDataType() {
        return dataType;
    }

    private void setDataType(Datatype[] dataType) {
        this.dataType = dataType;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        checkDataType();
        return super.getProposals(contents, position);
    }

    @Override
    protected IContentProposal[] getProposals(String prefix) {
        searchPattern.setPattern(prefix);
        return Stream.of(getDataType())
                .filter(datatype -> searchPattern.matches(datatype.getName()))
                .map(DatatypeContentProposal::new)
                .toArray(IContentProposal[]::new);
    }

    private void checkDataType() {
        if (getDataType() == null) {
            setDataType(findDataType());
        }
    }

    private Datatype[] findDataType() {
        return ipsProject.findDatatypes(isValueDatatypesOnly(), isIncludeVoid(), isIncludePrimitives(),
                getExcludedDatatypes(), isIncludeAbstract());
    }

    @Override
    public void clearCache() {
        setDataType(null);
    }

}
