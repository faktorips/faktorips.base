/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Abstract base class for ips completion processors.
 */
public abstract class AbstractCompletionProcessor implements ISubjectControlContentAssistProcessor {

    protected IIpsProject ipsProject;
    private String errorMessage;
    private boolean computeProposalForEmptyPrefix = false;

    public AbstractCompletionProcessor() {
        // default constructor
    }

    public AbstractCompletionProcessor(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    public void setIpsProject(IIpsProject project) {
        ipsProject = project;
    }

    /**
     * If true, the processor proposes all objects if the user has provided no prefix to start with.
     * If false, the processor won't generate a prososal.
     */
    public void setComputeProposalForEmptyPrefix(boolean value) {
        computeProposalForEmptyPrefix = value;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        throw new RuntimeException("ITextViewer not supported."); //$NON-NLS-1$
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return new char[0];
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMsg Set this message as error message.
     */
    public void setErrorMessage(String errorMsg) {
        errorMessage = errorMsg;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null; // no context
    }

    @Override
    public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl,
            int documentOffset) {
        return null;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl,
            int documentOffset) {

        if (contentAssistSubjectControl.getControl() instanceof Text) {
            // special check for text controls
            if (!((Text)contentAssistSubjectControl.getControl()).getEditable()) {
                return null;
            }
        }

        if (documentOffset == 0 && !computeProposalForEmptyPrefix) {
            return null;
        }
        if (ipsProject == null) {
            errorMessage = Messages.AbstractCompletionProcessor_msgNoProject;
            return null;
        }
        String input = contentAssistSubjectControl.getDocument().get();
        String prefix = input.substring(0, documentOffset);

        List<ICompletionProposal> result = new ArrayList<>(100);
        try {
            doComputeCompletionProposals(prefix, documentOffset, result);
        } catch (Exception e) {
            errorMessage = Messages.AbstractCompletionProcessor_msgInternalError;
            IpsPlugin.log(e);
            return null;
        }

        ICompletionProposal[] proposals = new ICompletionProposal[result.size()];
        result.toArray(proposals);
        return proposals;
    }

    /**
     * Creates completion proposals for every package matching the given prefix.
     * 
     * @param packages The array of packages to check
     * @param prefix The prefix to check the packages against
     * @param replacementLength The replacement length given to the new completion proposal
     * @param result The list to add new completion proposals to.
     */
    protected void matchPackages(IIpsPackageFragment[] packages,
            String prefix,
            int replacementLength,
            List<ICompletionProposal> result) {

        String lowerPrefix = prefix.toLowerCase();
        for (IIpsPackageFragment package1 : packages) {
            String name = package1.getName();
            if (name.length() == 0) {
                // don't show default package,
                // the default package could be entered by leaving the edit field empty
                continue;
            }
            if (name.toLowerCase().startsWith(lowerPrefix)) {
                CompletionProposal proposal = new CompletionProposal(name, 0, replacementLength, name.length(),
                        IpsUIPlugin.getImageHandling().getImage(package1), name, null, ""); //$NON-NLS-1$
                result.add(proposal);
            }
        }
    }

    /**
     * Returns the displayed default package name "(default package)" if the given package name is
     * empty, otherwise returns the given package name without change.
     */
    protected String mapDefaultPackageName(String packageName) {
        if (StringUtils.isEmpty(packageName)) {
            packageName = Messages.AbstractCompletionProcessor_labelDefaultPackage;
        }
        return packageName;
    }

    // TODO Throws Exception?
    protected abstract void doComputeCompletionProposals(String prefix,
            int documentOffset,
            List<ICompletionProposal> result) throws Exception;

}
