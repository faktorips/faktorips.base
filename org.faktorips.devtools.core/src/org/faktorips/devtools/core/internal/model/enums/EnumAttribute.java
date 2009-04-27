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

package org.faktorips.devtools.core.internal.model.enums;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of <code>IEnumAttribute</code>, see the corresponding interface for more details.
 * 
 * @see org.faktorips.devtools.core.model.enums.IEnumAttribute
 * 
 * @author Alexander Weickmann
 * 
 * @since 2.3
 */
public class EnumAttribute extends AtomicIpsObjectPart implements IEnumAttribute {

    /** The icon representing an enum attribute. */
    private final static String ICON = "EnumAttribute.gif";

    /** The icon representing an overridden enum attribute. */
    private final static String OVERRIDDEN_ICON = "EnumAttributeOverridden.gif";

    /** The icon representing an enum attribute that is marked as unique literalName. */
    private final static String UNIQUE_IDENTIFIER_ICON = "EnumAttributeUniqueIdentifier.gif";

    /** The icon representing an overridden unique literalName enum attribute. */
    private final static String OVERRIDDEN_UNIQUE_IDENTIFIER_ICON = "EnumAttributeOverriddenUniqueIdentifier.gif";

    /** The datatype of this enum attribute. */
    private String datatype;

    /** Flag indicating whether this enum attribute is used as literal name. */
    private boolean literalName;

    /** Flag indicating whether this enum attribute is inherited from the supertype hierarchy. */
    private boolean inherited;

    /** Flag indicating whether this enum attribute is a unique literalName. */
    private boolean uniqueIdentifier;

    /**
     * Creates a new <code>EnumAttribute</code>.
     * 
     * @param parent The enum type this enum attribute belongs to.
     * @param id A unique id for this enum attribute.
     */
    public EnumAttribute(IEnumType parent, int id) {
        super(parent, id);

        this.datatype = "";
        this.literalName = false;
        this.inherited = false;
        this.uniqueIdentifier = false;
    }

    /**
     * {@inheritDoc}
     */
    public void setName(String name) {
        ArgumentCheck.notNull(name);

        String oldName = this.name;
        this.name = name;
        valueChanged(oldName, name);
    }

    /**
     * {@inheritDoc}
     */
    public void setDatatype(String datatype) {
        ArgumentCheck.notNull(datatype);

        String oldDatatype = this.datatype;
        this.datatype = datatype;
        valueChanged(oldDatatype, datatype);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLiteralName() {
        return literalName;
    }

    /**
     * {@inheritDoc}
     */
    public void setLiteralName(boolean literalName) {
        boolean oldIsIdentifier = this.literalName;
        this.literalName = literalName;
        valueChanged(oldIsIdentifier, literalName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initFromXml(Element element, Integer id) {
        name = element.getAttribute(PROPERTY_NAME);
        datatype = element.getAttribute(PROPERTY_DATATYPE);
        literalName = Boolean.parseBoolean(element.getAttribute(PROPERTY_LITERAL_NAME));
        uniqueIdentifier = Boolean.parseBoolean(element.getAttribute(PROPERTY_UNIQUE_IDENTIFIER));
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));

        super.initFromXml(element, id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_DATATYPE, datatype);
        element.setAttribute(PROPERTY_LITERAL_NAME, String.valueOf(literalName));
        element.setAttribute(PROPERTY_UNIQUE_IDENTIFIER, String.valueOf(uniqueIdentifier));
        element.setAttribute(PROPERTY_INHERITED, String.valueOf(inherited));
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage() {
        try {
            if (inherited && findSuperEnumAttribute() == null) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
            }

            boolean isUniqueIdentifier = findIsUniqueIdentifier();
            if (isUniqueIdentifier && inherited) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_UNIQUE_IDENTIFIER_ICON);
            } else if (isUniqueIdentifier) {
                return IpsPlugin.getDefault().getImage(UNIQUE_IDENTIFIER_ICON);
            } else if (inherited) {
                return IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
            } else {
                return IpsPlugin.getDefault().getImage(ICON);
            }
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);

        validateName(list, ipsProject);
        if (!inherited) {
            validateDatatype(list, ipsProject);
            validateLiteralName(list, ipsProject);
        }
        validateInherited(list, ipsProject);
    }

    /** Validates the <code>name</code> property. */
    private void validateName(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributesThisType = getEnumType().getEnumAttributes();

        // Check for name missing
        if (name.equals("")) {
            text = Messages.EnumAttribute_NameMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING, text, Message.ERROR, this,
                    PROPERTY_NAME);
            list.add(validationMessage);
        }

        // Check for other attributes with the same name
        int numberEnumAttributesThisName = 0;
        for (IEnumAttribute currentEnumAttribute : enumAttributesThisType) {
            if (currentEnumAttribute.getName().equals(name)) {
                numberEnumAttributesThisName++;
            }
            if (numberEnumAttributesThisName > 1) {
                text = NLS.bind(Messages.EnumAttribute_DuplicateName, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME, text, Message.ERROR, this,
                        PROPERTY_NAME);
                list.add(validationMessage);
                break;
            }
        }
    }

    /** Validates the <code>datatype</code> property. */
    private void validateDatatype(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;

        // Check for datatype missing
        if (datatype.equals("")) {
            text = Messages.EnumAttribute_DatatypeMissing;
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // Check for datatype not existing
        Datatype ipsDatatype = getIpsProject().findDatatype(datatype);
        if (ipsDatatype == null) {
            text = NLS.bind(Messages.EnumAttribute_DatatypeDoesNotExist, datatype);
            validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST, text, Message.ERROR, this,
                    PROPERTY_DATATYPE);
            list.add(validationMessage);
            return;
        }

        // Check for literalName datatype = String
        if (literalName) {
            if (!(ipsDatatype.getName().equals("String"))) {
                text = Messages.EnumAttribute_LiteralNameNotOfDatatypeString;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING, text,
                        Message.ERROR, this, PROPERTY_DATATYPE);
                list.add(validationMessage);
            }
        }
    }

    /** Validates the <code>literalName</code> property. */
    private void validateLiteralName(MessageList list, IIpsProject ipsProject) {
        String text;
        Message validationMessage;
        List<IEnumAttribute> enumAttributes = getEnumType().getEnumAttributesIncludeSupertypeCopies();

        if (literalName) {
            // Check for other attributes being marked as literalName
            int numberEnumAttributesIdentifier = 0;
            for (IEnumAttribute currentEnumAttribute : enumAttributes) {
                if (currentEnumAttribute.isLiteralName()) {
                    numberEnumAttributesIdentifier++;
                }
                if (numberEnumAttributesIdentifier > 1) {
                    text = Messages.EnumAttribute_DuplicateLiteralName;
                    validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME, text, Message.ERROR,
                            this, PROPERTY_LITERAL_NAME);
                    list.add(validationMessage);
                    break;
                }
            }

            // A literal name must also be a unique literalName
            if (!uniqueIdentifier) {
                text = Messages.EnumAttribute_LiteralNameButNotUniqueIdentifier;
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER, text,
                        Message.ERROR, this, PROPERTY_LITERAL_NAME);
                list.add(validationMessage);
            }
        }
    }

    /** Validates the <code>inherited</code> property. */
    private void validateInherited(MessageList list, IIpsProject ipsProject) throws CoreException {
        String text;
        Message validationMessage;

        // Check existence in supertype hierarchy if this enum attribute is inherited
        if (inherited) {
            if (findSuperEnumAttribute() == null) {
                text = NLS.bind(Messages.EnumAttribute_NoSuchAttributeInSupertypeHierarchy, name);
                validationMessage = new Message(MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY, text,
                        Message.ERROR, this, PROPERTY_INHERITED);
                list.add(validationMessage);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInherited() {
        return inherited;
    }

    /**
     * {@inheritDoc}
     */
    public void setInherited(boolean isInherited) {
        if (isInherited) {
            setDatatype("");
            setLiteralName(false);
            setUniqueIdentifier(false);
        }

        boolean oldIsInherited = this.inherited;
        this.inherited = isInherited;
        valueChanged(oldIsInherited, isInherited);
    }

    /**
     * {@inheritDoc}
     */
    public IEnumType getEnumType() {
        return (IEnumType)getParent();
    }

    /**
     * {@inheritDoc}
     */
    public String getDatatype() {
        return datatype;
    }

    /**
     * {@inheritDoc}
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException {
        ArgumentCheck.notNull(ipsProject);

        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute();
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.findDatatype(ipsProject);
        }

        return ipsProject.findValueDatatype(datatype);
    }

    /**
     * Returns the original enum attribute this enum attribute is a copy of (if this enum attribute
     * is inherited).
     * <p>
     * Returns <code>null</code> if this enum attribute is not inherited or the super enum attribute
     * cannot be found.
     */
    private IEnumAttribute findSuperEnumAttribute() throws CoreException {
        if (!inherited) {
            return null;
        }

        // TODO aw: do we need this again and again if we want to search super enum types?
        EnumTypeHierachyVisitor collector = new EnumTypeHierachyVisitor(getIpsProject()) {
            protected boolean visit(IEnumType currentType) throws CoreException {
                return true;
            }
        };

        IIpsProject ipsProject = getIpsProject();
        IEnumType currentSuperEnumType = collector.findSupertype(getEnumType(), ipsProject);
        while (currentSuperEnumType != null) {
            IEnumAttribute possibleFoundEnumAttribute = currentSuperEnumType.getEnumAttribute(name);
            if (possibleFoundEnumAttribute != null) {
                return possibleFoundEnumAttribute;
            }
            currentSuperEnumType = collector.findSupertype(currentSuperEnumType, ipsProject);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    public void setUniqueIdentifier(boolean uniqueIdentifier) {
        boolean oldIsUniqueIdentifier = this.uniqueIdentifier;
        this.uniqueIdentifier = uniqueIdentifier;
        valueChanged(oldIsUniqueIdentifier, uniqueIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean findIsLiteralName() throws CoreException {
        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute();
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isLiteralName();
        } else {
            return isLiteralName();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean findIsUniqueIdentifier() throws CoreException {
        if (inherited) {
            IEnumAttribute superEnumAttribute = findSuperEnumAttribute();
            if (superEnumAttribute == null) {
                return null;
            }
            return superEnumAttribute.isUniqueIdentifier();
        } else {
            return isUniqueIdentifier();
        }
    }

    public boolean isUsedAsIdInFaktorIpsUi() {
        // TODO aw: Auto-generated method stub
        return false;
    }

    public boolean isUsedAsNameInFaktorIpsUi() {
        // TODO aw: Auto-generated method stub
        return false;
    }

    public void setUsedAsIdInFaktorIpsUi(boolean useAsIdInFaktorIpsUi) {
        // TODO aw: Auto-generated method stub
        
    }

    public void setUsedAsNameInFaktorIpsUi(boolean useAsNameInFaktorIpsUi) {
        // TODO aw: Auto-generated method stub
        
    }
    
}
