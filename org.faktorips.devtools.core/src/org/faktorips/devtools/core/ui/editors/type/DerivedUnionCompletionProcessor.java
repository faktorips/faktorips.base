/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.type;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for derived unions for a given associations.
 * The search is done along the supertype hierarchy starting with the type the given association belongs to.
 */
public class DerivedUnionCompletionProcessor extends AbstractCompletionProcessor {
    
    private IType type;
    
    public DerivedUnionCompletionProcessor(IAssociation association) {
        ArgumentCheck.notNull(association);
        this.type = association.getType();
        setIpsProject(type.getIpsProject());
    }

    /** 
     * {@inheritDoc}
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        DerivedUnionCollector collector = new DerivedUnionCollector(ipsProject, prefix.toLowerCase(), documentOffset, result);
        collector.start(type);
    }
    
    class DerivedUnionCollector extends TypeHierarchyVisitor {

        private String prefix;
        private int offset;
        private List result;
        
        public DerivedUnionCollector(IIpsProject ipsProject, String prefix, int offset, List result) {
            super(ipsProject);
            this.prefix = prefix;
            this.offset = offset;
            this.result = result;
        }

        protected boolean visit(IType currentType) throws CoreException {
            IAssociation[] associations = currentType.getAssociations();
            for (int j=0; j<associations.length; j++) {
                if (associations[j].isDerivedUnion() && associations[j].getName().toLowerCase().startsWith(prefix)) {
                    addToResult(result, associations[j], offset);
                }
            }
            return true;
        }
        
        private void addToResult(List result, IAssociation relation, int documentOffset) {
            String name = relation.getName();
            String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
            CompletionProposal proposal = new CompletionProposal(
                    name, 0, documentOffset, name.length(),  
                    relation.getImage(), displayText, null, relation.getDescription());
            result.add(proposal);
        }

    }
}
