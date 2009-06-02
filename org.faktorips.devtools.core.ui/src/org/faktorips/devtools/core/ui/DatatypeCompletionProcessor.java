/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;

/**
 *
 */
public class DatatypeCompletionProcessor extends AbstractCompletionProcessor {

    private boolean includeVoid;
    private boolean valuetypesOnly;
    private boolean includePrimitives;
    private boolean includeAbstract;
    private List<Datatype> excludedDatatypes;

    public DatatypeCompletionProcessor() {
        this.includeVoid = false;
        this.valuetypesOnly = false;
        this.includePrimitives = true;
        this.includeAbstract = false;
        this.excludedDatatypes = null;

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

    /**
     * {@inheritDoc}
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
        DefaultLabelProvider labelProvider = new DefaultLabelProvider();
        List<Datatype> foundTypes = new ArrayList<Datatype>();
        Datatype[] types = ipsProject.findDatatypes(valuetypesOnly, includeVoid, includePrimitives, excludedDatatypes,
                includeAbstract);
        for (int i = 0; i < types.length; i++) {
            if (types[i].getName().toLowerCase().startsWith(prefix)) {
                foundTypes.add(types[i]);
            }
        }

        Collections.sort(foundTypes, new Comparator() {

            public int compare(Object o1, Object o2) {
                Datatype d1 = (Datatype)o1;
                Datatype d2 = (Datatype)o2;
                return d1.getName().toLowerCase().compareTo(d2.getName().toLowerCase());
            }

        });

        for (Iterator<Datatype> it = foundTypes.iterator(); it.hasNext();) {
            Datatype datatype = (Datatype)it.next();
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
