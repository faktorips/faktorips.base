package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.documentor.DocumentorConfiguration;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

/**
 * Represents a table with the associations of an {@link IType} as rows and the attributes of the associations as columns
 * @author dicker
 *
 */
public class AssociationTablePageElement extends AbstractSpecificTablePageElement {
	
	protected IType type;
	private DocumentorConfiguration config;
	

	/**
	 * Creates an {@link AssociationTablePageElement} for the specified {@link IType}
	 * @param type
	 */
	public AssociationTablePageElement(IType type, DocumentorConfiguration config) {
		super();
		this.type = type;
		this.config = config;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#addDataRows()
	 */
	@Override
	protected void addDataRows() {
		IAssociation[] associations = type.getAssociations();
		for (IAssociation association : associations) {
			addAssociation(association);
		}
	}

	/**
	 * reads the attributes of the association, creates a {@link TableRowPageElement} and adds it to the table. 
	 * @param association
	 */
	private void addAssociation(IAssociation association) {
		List<String> values = new ArrayList<String>();

		values.add(association.getName());

		// will be replaced with the link
		values.add(""); //$NON-NLS-1$

		values.add(association.getDescription());
		values.add(association.getAssociationType().getName());
		values.add(association.getAggregationKind().getName());
		values.add(association.getTargetRoleSingular());
		values.add(association.getTargetRolePlural());
		values.add(Integer.toString(association.getMinCardinality()));
		values.add(Integer.toString(association.getMaxCardinality()));
		values.add(association.isDerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
		values.add(association.isSubsetOfADerivedUnion() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$
		values.add(association.isQualified() ? "X" : "-"); //$NON-NLS-1$ //$NON-NLS-2$

		PageElement[] elements = PageElementUtils.createTextPageElements(values);

		
		try {
			IIpsObject target = type.getIpsProject().findIpsObject(type.getIpsObjectType(), association.getTarget());
			elements[1] = PageElementUtils.createLinkPageElement(config, target, "content", target.getName(), true); //$NON-NLS-1$
		} catch (CoreException e) {
			elements[1] = new TextPageElement(""); //$NON-NLS-1$
		}

		addSubElement(new TableRowPageElement(elements));
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.types.AbstractSpecificTablePageElement#getHeadline()
	 */
	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add(Messages.AssociationTablePageElement_headlineName);
		headline.add(Messages.AssociationTablePageElement_headlineTarget);
		headline.add(Messages.AssociationTablePageElement_headlineDescription);
		headline.add(Messages.AssociationTablePageElement_headlineAssociationType);
		headline.add(Messages.AssociationTablePageElement_headlineAggregationKind);
		headline.add(Messages.AssociationTablePageElement_headlineTargetRoleSingular);
		headline.add(Messages.AssociationTablePageElement_headlineTargetRolePlural);

		addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineMinCardinality, Style.CENTER);
		addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineMaxCardinality, Style.CENTER);
		addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineDerivedUnion, Style.CENTER);
		addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineSubsettedDerivedUnion, Style.CENTER);
		addHeadlineAndColumnLayout(headline, Messages.AssociationTablePageElement_headlineQualified, Style.CENTER);

		return headline;
	}

	/* (non-Javadoc)
	 * @see org.faktorips.devtools.htmlexport.pages.elements.core.DataPageElement#isEmpty()
	 */
	public boolean isEmpty() {
		return ArrayUtils.isEmpty(type.getAssociations());
	}
}
