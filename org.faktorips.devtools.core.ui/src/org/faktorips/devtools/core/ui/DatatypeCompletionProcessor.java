/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.ui.controls.DatatypeContentProposalProvider;

/**
 * @deprecated since 3.10, use {@link DatatypeContentProposalProvider} instead
 */
@Deprecated
public class DatatypeCompletionProcessor extends AbstractCompletionProcessor {

    private boolean includeVoid;
    private boolean valuetypesOnly;
    private boolean includePrimitives;
    private boolean includeAbstract;
    private List<Datatype> excludedDatatypes;

    public DatatypeCompletionProcessor() {
        includeVoid = false;
        valuetypesOnly = false;
        includePrimitives = true;
        includeAbstract = false;
        excludedDatatypes = null;

        setComputeProposalForEmptyPrefix(true);
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

    public boolean getValueDatatypesOnly() {
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

    @Override
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List<ICompletionProposal> result)
            throws Exception {

        prefix = prefix.toLowerCase();
        DefaultLabelProvider labelProvider = new DefaultLabelProvider();
        List<Datatype> foundTypes = new ArrayList<Datatype>();
        Datatype[] types = ipsProject.findDatatypes(valuetypesOnly, includeVoid, includePrimitives, excludedDatatypes,
                includeAbstract);
        for (Datatype type : types) {
            if (type.getName().toLowerCase().startsWith(prefix)) {
                foundTypes.add(type);
            }
        }

        Collections.sort(foundTypes, new Comparator<Datatype>() {

            @Override
            public int compare(Datatype o1, Datatype o2) {
                Datatype d1 = o1;
                Datatype d2 = o2;
                return d1.getName().toLowerCase().compareTo(d2.getName().toLowerCase());
            }

        });

        for (Datatype datatype : foundTypes) {
            String qName = datatype.getQualifiedName();
            String displayText = datatype.getName();
            Image image = labelProvider.getImage(datatype);
            CompletionProposal proposal = new CompletionProposal(qName, 0, documentOffset, qName.length(), image,
                    displayText, null, null);
            result.add(proposal);
        }

        labelProvider.dispose();
    }

}
