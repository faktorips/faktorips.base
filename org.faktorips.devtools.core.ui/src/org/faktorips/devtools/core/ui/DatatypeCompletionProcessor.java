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
    
    private boolean includeVoid = false;
    private boolean valuetypesOnly = false;
    private boolean includePrimitives = true;

    public DatatypeCompletionProcessor() {
    	setComputeProposalForEmptyPrefix(true);
    }
    
    public void setIncludeVoid(boolean value) {
        includeVoid = value;
    }
    
    public boolean isIncludeVoid() {
        return includeVoid;
    }
    
    public void setValueDatatypesOnly(boolean value) {
        valuetypesOnly = value;
    }
    
    public boolean getValueDatatypesOnly() {
        return valuetypesOnly;
    }

	/**
	 * @return Returns the includePrimitives.
	 */
	public boolean isIncludePrimitives() {
		return includePrimitives;
	}

	/**
	 * @param includePrimitives The includePrimitives to set.
	 */
	public void setIncludePrimitives(boolean includePrimitives) {
		this.includePrimitives = includePrimitives;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
		DefaultLabelProvider labelProvider = new DefaultLabelProvider();
        List foundTypes = new ArrayList();
        Datatype[] types = ipsProject.findDatatypes(valuetypesOnly, includeVoid, includePrimitives);
        for (int i=0; i<types.length; i++) {
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
        for (Iterator it = foundTypes.iterator(); it.hasNext();) {
			Datatype datatype = (Datatype) it.next();
			String qName = datatype.getQualifiedName();
			String displayText = datatype.getName();
            Image image = labelProvider.getImage(datatype);
            CompletionProposal proposal = new CompletionProposal(
                    qName, 0, documentOffset, qName.length(),  
                    image, displayText, null, null);
            result.add(proposal);
		}
        labelProvider.dispose();
	}

}
