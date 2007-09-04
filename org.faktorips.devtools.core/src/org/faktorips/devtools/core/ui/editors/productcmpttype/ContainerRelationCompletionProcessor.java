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

package org.faktorips.devtools.core.ui.editors.productcmpttype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype2.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype2.IRelation;
import org.faktorips.devtools.core.model.productcmpttype2.ProductCmptTypeHierarchyVisitor;
import org.faktorips.devtools.core.ui.AbstractCompletionProcessor;
import org.faktorips.util.ArgumentCheck;


/**
 * A completion processor that searchs for abstract read-only container relations for a given relation.
 * The search is done along the supertype hierarchy starting with the type the given relation belongs to.
 */
public class ContainerRelationCompletionProcessor extends AbstractCompletionProcessor {
    
    private IRelation relation;
    
    public ContainerRelationCompletionProcessor(IRelation relation) {
        ArgumentCheck.notNull(relation);
        this.relation = relation; 
        setIpsProject(relation.getIpsProject());
    }

    /** 
     * {@inheritDoc}
     */
    protected void doComputeCompletionProposals(String prefix, int documentOffset, List result) throws Exception {
        prefix = prefix.toLowerCase();
        ContainerRelationCollector collector = new ContainerRelationCollector(ipsProject, prefix, documentOffset, result);
        collector.start(relation.getProductCmptType());
    }
    
    class ContainerRelationCollector extends ProductCmptTypeHierarchyVisitor {

        private List result = new ArrayList();
        private String namePrefix;
        private int docOffet;
        
        public ContainerRelationCollector(
                IIpsProject ipsProject,
                String namePrefix,
                int docOffset,
                List result) {
            super(ipsProject);
            this.namePrefix = namePrefix;
            this.result = result;
            this.docOffet = docOffset;
        }

        /**
         * {@inheritDoc}
         */
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            IRelation[] candidates = currentType.getRelations();
            for (int i = 0; i < candidates.length; i++) {
                if (candidates[i]!=relation && candidates[i].isReadOnlyContainer() && candidates[i].getName().toLowerCase().startsWith(namePrefix)) {
                    addToResult(candidates[i]);
                }
            }
            return true;
        }
        
        private void addToResult(IRelation relation) {
            String name = relation.getName();
            String displayText = name + " - " + relation.getParent().getName(); //$NON-NLS-1$
            CompletionProposal proposal = new CompletionProposal(
                    name, 0, docOffet, name.length(),  
                    relation.getImage(), displayText, null, relation.getDescription());
            result.add(proposal);
        }

    }
}
