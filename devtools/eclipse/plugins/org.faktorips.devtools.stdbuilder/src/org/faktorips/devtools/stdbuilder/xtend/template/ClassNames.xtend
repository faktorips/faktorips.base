package org.faktorips.devtools.stdbuilder.xtend.template

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.io.IOException
import java.io.InvalidObjectException
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.Arrays
import java.util.Calendar
import java.util.Collections
import java.util.HashMap
import java.util.Iterator
import java.util.LinkedHashMap
import java.util.Locale
import java.util.Map
import java.util.NoSuchElementException
import java.util.Objects
import java.util.TimeZone
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode
import org.faktorips.runtime.AssociationChangedEvent
import org.faktorips.runtime.CardinalityRange
import org.faktorips.runtime.DefaultUnresolvedReference
import org.faktorips.runtime.FormulaExecutionException
import org.faktorips.runtime.IConfigurableModelObject
import org.faktorips.runtime.IDeltaComputationOptions
import org.faktorips.runtime.IModelObject
import org.faktorips.runtime.IModelObjectDelta
import org.faktorips.runtime.IModelObjectVisitor
import org.faktorips.runtime.INotificationSupport
import org.faktorips.runtime.IObjectReferenceStore
import org.faktorips.runtime.IProductComponent
import org.faktorips.runtime.IProductComponentGeneration
import org.faktorips.runtime.IProductComponentLink
import org.faktorips.runtime.IRuntimeRepository
import org.faktorips.runtime.IRuntimeRepositoryLookup
import org.faktorips.runtime.IUnresolvedReference
import org.faktorips.runtime.IValidationContext
import org.faktorips.runtime.IllegalRepositoryModificationException
import org.faktorips.runtime.IpsPropertyChangeSupport
import org.faktorips.runtime.Message
import org.faktorips.runtime.MessageList
import org.faktorips.runtime.MsgReplacementParameter
import org.faktorips.runtime.ObjectProperty
import org.faktorips.runtime.internal.AbstractModelObject
import org.faktorips.runtime.internal.DateTime
import org.faktorips.runtime.internal.EnumValues
import org.faktorips.runtime.internal.IXmlPersistenceSupport
import org.faktorips.runtime.internal.ModelObjectDelta
import org.faktorips.runtime.internal.MultiValueXmlHelper
import org.faktorips.runtime.internal.ProductComponentLink
import org.faktorips.runtime.internal.ProductConfiguration
import org.faktorips.runtime.internal.Range
import org.faktorips.runtime.internal.Table
import org.faktorips.runtime.internal.ValueToXmlHelper
import org.faktorips.runtime.internal.XmlCallback
import org.faktorips.runtime.util.MessagesHelper
import org.faktorips.runtime.util.ProductComponentLinks
import org.faktorips.runtime.validation.GenericRelevanceValidation
import org.faktorips.runtime.xml.IToXmlSupport
import org.faktorips.values.DefaultInternationalString
import org.faktorips.values.InternationalString
import org.faktorips.values.ListUtil
import org.faktorips.values.LocalizedString
import org.faktorips.values.ObjectUtil
import org.faktorips.valueset.IntegerRange
import org.faktorips.valueset.OrderedValueSet
import org.faktorips.valueset.StringLengthValueSet
import org.faktorips.valueset.UnrestrictedValueSet
import org.faktorips.valueset.ValueSet
import org.w3c.dom.Element
import org.faktorips.runtime.IpsEnumToXmlWriter
import org.faktorips.runtime.IModifiableRuntimeRepository
import org.faktorips.valueset.DerivedValueSet

@SuppressFBWarnings
class ClassNames {

    def static IpsPropertyChangeSupport(AbstractGeneratorModelNode it) { addImport(typeof(IpsPropertyChangeSupport)) }

    def static IModelObject(AbstractGeneratorModelNode it) { addImport(typeof(IModelObject)) }

    def static IModelObjectDelta(AbstractGeneratorModelNode it) { addImport(typeof(IModelObjectDelta)) }

    def static IModelObjectVisitor(AbstractGeneratorModelNode it) { addImport(typeof(IModelObjectVisitor)) }

    def static IDeltaComputationOptions(AbstractGeneratorModelNode it) { addImport(typeof(IDeltaComputationOptions)) }

    def static ModelObjectDelta(AbstractGeneratorModelNode it) { addImport(typeof(ModelObjectDelta)) }

    def static ObjectProperty(AbstractGeneratorModelNode it) { addImport(typeof(ObjectProperty)) }

    def static Message(AbstractGeneratorModelNode it) { addImport(typeof(Message)) }

    def static MessageList(AbstractGeneratorModelNode it) { addImport(typeof(MessageList)) }

    def static ListUtil(AbstractGeneratorModelNode it) { addImport(typeof(ListUtil)) }

    def static MsgReplacementParameter(AbstractGeneratorModelNode it) { addImport(typeof(MsgReplacementParameter)) }

    def static IConfigurableModelObject(AbstractGeneratorModelNode it) { addImport(typeof(IConfigurableModelObject)) }

    def static AbstractModelObject(AbstractGeneratorModelNode it) { addImport(typeof(AbstractModelObject)) }

    def static ProductConfiguration(AbstractGeneratorModelNode it) { addImport(typeof(ProductConfiguration)) }

    def static IValidationContext(AbstractGeneratorModelNode it) { addImport(typeof(IValidationContext)) }

    def static INotificationSupport(AbstractGeneratorModelNode it) { addImport(typeof(INotificationSupport)) }

    def static PropertyChangeEvent(AbstractGeneratorModelNode it) { addImport(typeof(PropertyChangeEvent)) }

    def static PropertyChangeListener(AbstractGeneratorModelNode it) { addImport(typeof(PropertyChangeListener)) }

    def static AssociationChangedEvent(AbstractGeneratorModelNode it) { addImport(typeof(AssociationChangedEvent)) }

    def static GenericRelevanceValidation(AbstractGeneratorModelNode it) {
        addImport(typeof(GenericRelevanceValidation))
    }

    // Collection is a reserved type. Thats why we have to use the underscore in the name and the string reference in addImport
    def static Collection_(AbstractGeneratorModelNode it, String genericType) {
        addImport("java.util.Collection") + "<" + genericType + ">"
    }

    // List is a reserved type. Thats why we have to use the underscore in the name and the string reference in addImport
    def static List_(AbstractGeneratorModelNode it, String genericType) {
        addImport("java.util.List") + "<" + genericType + ">"
    }

    // ArrayList.name does return java.util.ArrayList. Thats why we have to use the string reference in addImport
    def static ArrayList(AbstractGeneratorModelNode it) {
        addImport("java.util.ArrayList") + "<>"
    }

    def static Arrays(AbstractGeneratorModelNode it) { addImport(typeof(Arrays)) }

    def static Map(AbstractGeneratorModelNode it, String genericKey, String genericValue) {
        addImport(typeof(Map)) + "<" + genericKey + ", " + genericValue + ">"
    }

    def static HashMap(AbstractGeneratorModelNode it) {
        addImport(typeof(HashMap)) + "<>"
    }

    def static LinkedHashMap(AbstractGeneratorModelNode it) {
        addImport(typeof(LinkedHashMap)) + "<>"
    }

    def static Iterator(AbstractGeneratorModelNode it, String genericType) {
        addImport(typeof(Iterator)) + "<" + genericType + ">"
    }

    def static Collections(AbstractGeneratorModelNode it) { addImport(typeof(Collections)) }

    def static Calendar(AbstractGeneratorModelNode it) { addImport(typeof(Calendar)) }

    def static Element(AbstractGeneratorModelNode it) { addImport(typeof(Element)) }

    def static IProductComponent(AbstractGeneratorModelNode it) { addImport(typeof(IProductComponent)) }

    def static IProductComponentGeneration(AbstractGeneratorModelNode it) {
        addImport(typeof(IProductComponentGeneration))
    }

    def static ProductComponentLink(AbstractGeneratorModelNode it) {
        addImport(typeof(ProductComponentLink)) + "<>"
    }

    def static IProductComponentLink(AbstractGeneratorModelNode it, String genericType) {
        addImport(typeof(IProductComponentLink)) + "<" + genericType + ">"
    }

    def static ProductComponentLinks(AbstractGeneratorModelNode it) {
        addImport(typeof(ProductComponentLinks))
    }

    def static CardinalityRange(AbstractGeneratorModelNode it) { addImport(typeof(CardinalityRange)) }

    def static IRuntimeRepository(AbstractGeneratorModelNode it) { addImport(typeof(IRuntimeRepository)) }

    def static IRuntimeRepositoryLookup(AbstractGeneratorModelNode it) { addImport(typeof(IRuntimeRepositoryLookup)) }

    def static IModifiableRuntimeRepository(AbstractGeneratorModelNode it) { addImport(typeof(IModifiableRuntimeRepository)) }

    def static DateTime(AbstractGeneratorModelNode it) { addImport(typeof(DateTime)) }

    def static TimeZone(AbstractGeneratorModelNode it) { addImport(typeof(TimeZone)) }

    def static IXmlPersistenceSupport(AbstractGeneratorModelNode it) { addImport(typeof(IXmlPersistenceSupport)) }

    def static ValueToXmlHelper(AbstractGeneratorModelNode it) { addImport(typeof(ValueToXmlHelper)) }

    def static MultiValueXmlHelper(AbstractGeneratorModelNode it) { addImport(typeof(MultiValueXmlHelper)) }

    def static ValueSet(AbstractGeneratorModelNode it, String genericType) {
        addImport(typeof(ValueSet)) + "<" + genericType + ">"
    }

    def static UnrestrictedValueSet(AbstractGeneratorModelNode it, String genericType) {
        addImport(typeof(UnrestrictedValueSet)) + "<" + genericType + ">"
    }

    def static OrderedValueSet(AbstractGeneratorModelNode it, String genericType) {
        addImport(typeof(OrderedValueSet)) + "<" + genericType + ">"
    }

    def static OrderedValueSet(AbstractGeneratorModelNode it) {
        addImport(typeof(OrderedValueSet))
    }

    def static StringLengthValueSet(AbstractGeneratorModelNode it) {
        addImport(typeof(StringLengthValueSet))
    }

    def static qnameRange(AbstractGeneratorModelNode it, String genericType) {
        "org.faktorips.valueset.Range<" + genericType + ">"
    }

    def static EnumValues(AbstractGeneratorModelNode it) {
        addImport(typeof(EnumValues))
    }
    
    def static DerivedValueSet(AbstractGeneratorModelNode it, String genericType) { 
        addImport(typeof(DerivedValueSet))  + "<" + genericType + ">"
    }

    def static Range(AbstractGeneratorModelNode it) { addImport(typeof(Range)) }

    def static IntegerRange(AbstractGeneratorModelNode it) { addImport(typeof(IntegerRange)) }

    def static Locale(AbstractGeneratorModelNode it) { addImport(typeof(Locale)) }

    def static LocalizedString(AbstractGeneratorModelNode it) { addImport(typeof(LocalizedString)) }

    def static InternationalString(AbstractGeneratorModelNode it) { addImport(typeof(InternationalString)) }

    def static Long(AbstractGeneratorModelNode it) { addImport("java.lang.Long") }

    def static DefaultInternationalString(AbstractGeneratorModelNode it) {
        addImport(typeof(DefaultInternationalString))
    }

    def static Table_(AbstractGeneratorModelNode it, String genericType) {
        addImport(Table.name) + "<" + genericType + ">"
    }

    def static MessagesHelper(AbstractGeneratorModelNode it) { addImport(typeof(MessagesHelper)) }

    // EXECPTIONS
    def static NoSuchElementException(AbstractGeneratorModelNode it) { addImport(typeof(NoSuchElementException)) }

    def static IllegalRepositoryModificationException(AbstractGeneratorModelNode it) {
        addImport(typeof(IllegalRepositoryModificationException))
    }

    def static IUnresolvedReference(AbstractGeneratorModelNode it) { addImport(typeof(IUnresolvedReference)) }

    def static DefaultUnresolvedReference(AbstractGeneratorModelNode it) {
        addImport(typeof(DefaultUnresolvedReference))
    }

    def static FormulaExecutionException(AbstractGeneratorModelNode it) { addImport(typeof(FormulaExecutionException)) }

    def static RuntimeException(AbstractGeneratorModelNode it) { addImport(typeof(RuntimeException)) }

    def static IllegalArgumentException(AbstractGeneratorModelNode it) { addImport(typeof(IllegalArgumentException)) }

    def static IllegalStateException(AbstractGeneratorModelNode it) { addImport(typeof(IllegalStateException)) }

    def static InvalidObjectException(AbstractGeneratorModelNode it) { addImport(typeof(InvalidObjectException)) }

    def static IOException(AbstractGeneratorModelNode it) { addImport(typeof(IOException)) }

    def static ObjectUtil(AbstractGeneratorModelNode it) { addImport(typeof(ObjectUtil)) }

    def static Objects(AbstractGeneratorModelNode it) { addImport(typeof(Objects)) }

    def static ObjectInputStream(AbstractGeneratorModelNode it) { addImport(typeof(ObjectInputStream)) }

    def static IObjectReferenceStore(AbstractGeneratorModelNode it) { addImport(typeof(IObjectReferenceStore)) }

    def static XmlCallback(AbstractGeneratorModelNode it) { addImport(typeof(XmlCallback)) }

    def static BuilderUtil(AbstractGeneratorModelNode it) { addImport("org.faktorips.runtime.builder.BuilderUtil") }

    def static Method(AbstractGeneratorModelNode it) { addImport("java.lang.reflect.Method") }

    def static Constructor(AbstractGeneratorModelNode it) { addImport("java.lang.reflect.Constructor") }

    def static InvocationTargetException(AbstractGeneratorModelNode it) {
        addImport("java.lang.reflect.InvocationTargetException")
    }

    def static InstantiationException(AbstractGeneratorModelNode it) { addImport("java.lang.InstantiationException") }

    def static GregorianCalendar(AbstractGeneratorModelNode it) { addImport("java.util.GregorianCalendar") }

    def static Serializable(AbstractGeneratorModelNode it) { addImport(typeof(Serializable)) }

    def static SuppressWarnings(AbstractGeneratorModelNode it) { addImport(typeof(SuppressWarnings)) }

    def static IToXmlSupport(AbstractGeneratorModelNode it) { addImport(typeof(IToXmlSupport)) }

    def static IpsEnumToXmlWriter(AbstractGeneratorModelNode it) { addImport(typeof(IpsEnumToXmlWriter)) }

}
