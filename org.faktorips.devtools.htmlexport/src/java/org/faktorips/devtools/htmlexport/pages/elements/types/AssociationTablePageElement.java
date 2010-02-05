package org.faktorips.devtools.htmlexport.pages.elements.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.htmlexport.pages.elements.core.LinkPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.PageElementUtils;
import org.faktorips.devtools.htmlexport.pages.elements.core.Style;
import org.faktorips.devtools.htmlexport.pages.elements.core.TextPageElement;
import org.faktorips.devtools.htmlexport.pages.elements.core.table.TableRowPageElement;

public class AssociationTablePageElement extends AbstractSpecificTablePageElement {
	protected IType type;

	public AssociationTablePageElement(IType type) {
		super();
		this.type = type;
	}

	@Override
	protected void addDataRows() {
		IAssociation[] associations = type.getAssociations();
		for (IAssociation association : associations) {
			addAssociation(association);
		}
	}

	private void addAssociation(IAssociation association) {
		List<String> values = new ArrayList<String>();

		values.add(association.getName());

		// wird durch Link ersetzt
		values.add("replacement");

		values.add(association.getDescription());
		values.add(association.getAssociationType().getName());
		values.add(association.getAggregationKind().getName());
		values.add(association.getTargetRoleSingular());
		values.add(association.getTargetRolePlural());
		values.add(Integer.toString(association.getMinCardinality()));
		values.add(Integer.toString(association.getMaxCardinality()));
		values.add(association.isDerivedUnion() ? "X" : "-");
		values.add(association.isSubsetOfADerivedUnion() ? "X" : "-");
		values.add(association.isQualified() ? "X" : "-");

		PageElement[] elements = PageElementUtils.createTextPageElements(values);

		try {
			IIpsObject target = type.getIpsProject().findIpsObject(type.getIpsObjectType(), association.getTarget());
			elements[1] = new LinkPageElement(target, "content", target.getName(), true);
		} catch (CoreException e) {
			elements[1] = new TextPageElement("");
		}
		// TODO Type-spezifisch ableiten

		addSubElement(new TableRowPageElement(elements));
	}

	@Override
	protected List<String> getHeadline() {
		List<String> headline = new ArrayList<String>();

		headline.add(IAssociation.PROPERTY_NAME);
		headline.add(IAssociation.PROPERTY_TARGET);
		headline.add(IAssociation.PROPERTY_DESCRIPTION);
		headline.add(IAssociation.PROPERTY_ASSOCIATION_TYPE);
		headline.add(IAssociation.PROPERTY_AGGREGATION_KIND);
		headline.add(IAssociation.PROPERTY_TARGET_ROLE_SINGULAR);
		headline.add(IAssociation.PROPERTY_TARGET_ROLE_PLURAL);

		addHeadlineAndColumnLayout(headline, IAssociation.PROPERTY_MIN_CARDINALITY, Style.CENTER);
		addHeadlineAndColumnLayout(headline, IAssociation.PROPERTY_MAX_CARDINALITY, Style.CENTER);
		addHeadlineAndColumnLayout(headline, IAssociation.PROPERTY_DERIVED_UNION, Style.CENTER);
		addHeadlineAndColumnLayout(headline, IAssociation.PROPERTY_SUBSETTED_DERIVED_UNION, Style.CENTER);
		addHeadlineAndColumnLayout(headline, IAssociation.PROPERTY_QUALIFIED, Style.CENTER);

		// TODO Type-spezifisch ableiten

		return headline;
	}

	public boolean isEmpty() {
		return ArrayUtils.isEmpty(type.getAssociations());
	}
}
