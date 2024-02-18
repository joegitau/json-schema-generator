package joegitau.models

case class ReferenceField(id: String, description: String, ref: String) extends SchemaElement {
  override def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "$ref" -> ref
      )
    )
  }
}


// ---------------------------------------------------------------------------------------------------------------------
// 2. define form elements (InputField, SelectDropdown, Checkbox, Date) as subclasses of the base schema elements.
//    while at it, adding additional properties and methods to these subclasses as needed.

case class InputField(
  override val id: String,
  override val description: String,
  override val minLength: Option[Int] = None,
  override val maxLength: Option[Int] = None,
  override val pattern: Option[String] = None,
  placeholder: Option[String] = None,
  formlyType: String = "input",
  inputType: String = "text"
) extends StringField(id, description, minLength, maxLength, pattern) {
  override def toJsonSchema: Map[String, Any] = {
    super.toJsonSchema.updated(
      id, super.toJsonSchema(id).asInstanceOf[Map[String, Any]]
        .updated("placeholder", placeholder.getOrElse(""))
        .updated("formlyType", formlyType)
        .updated("inputType", inputType)
    )
  }
}

case class SelectDropdownField(
  id: String,
  description: String,
  options: List[String],
  formlyType: String = "select",
) extends SchemaElement {
  override def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "enum" -> options,
        "formlyType" -> formlyType,
      )
    )
  }
}


// ---------------------------------------------------------------------------------------------------------------------
// 3. define builder functions (inputField, selectDropdown, etc.) that create instances of the form elements.
/*
def inputField(
  id: String,
  label: String,
  placeholder: String = "",
  defaultValue: String = "",
  minLength: Option[Int] = None,
  maxLength: Option[Int] = None,
  pattern: Option[String] = None
): InputField = InputField(id, label, placeholder, defaultValue, minLength, maxLength, pattern)



def booleanField(id: String, description: String): BooleanField =
  BooleanField(id, description)

def nullField(id: String, description: String): NullField =
  NullField(id, description)

def arrayField(
  id: String,
  description: String,
  items: SchemaElement,
  minItems: Option[Int] = None,
  maxItems: Option[Int] = None,
  uniqueItems: Option[Boolean] = None
): ArrayField = ArrayField(id, description, items, minItems, maxItems, uniqueItems)


def objectField(
  id: String,
  description: String,
  properties: (String, SchemaElement)*
)(
  required: List[String] = List(),
  minProperties: Option[Int] = None,
  maxProperties: Option[Int] = None,
  additionalProperties: Option[Either[Boolean, SchemaElement]] = None
): ObjectField = ObjectField(id, description, properties.toMap, required, minProperties, maxProperties, additionalProperties)

// when invoking objectField - use two sets of parentheses:
/*
  val productField = objectField(
  "product",
  "Product details",
  "id" -> inputField("id", "Product ID"),
  "name" -> inputField("name", "Product Name")
)(
  required = List("id", "name")
)

 */






/*
// define a case class for FormSchema
case class FormSchema(
  schema: String,
  id: String,
  title: String,
  description: String,
  `type`: String,
  properties: List[SchemaElement],
  required: List[String]
) {
  def toJsonSchema: Map[String, Any] =
    Map(
      "$schema" -> schema,
      "$id" -> id,
      "title" -> title,
      "description" -> description,
      "type" -> `type`,
      "properties" -> properties.flatMap(_.toJsonSchema).toMap,
      "required" -> required
    )
}

  // Now you can create an instance of FormSchema and call toJsonSchema to get the JsonSchema
  val formSchema = FormSchema(
    "https://json-schema.org/draft/2020-12/schema",
    "https://example.com/product.schema.json",
    "Product",
    "A product from Acme's catalog",
    "object",
    List(


    ),
    List("productId", "productName", "price")
  )

  val jsonSchema = formSchema.toJsonSchema

/*

def addValidationRule(field: Field, rule: ValidationRule): Field =
  field match {
    case f: StringInputField => f.copy(validationRules = f.validationRules :+ rule)
    case f: IntegerInputField => f.copy(validationRules = f.validationRules :+ rule)
    case f: NumberInputField => f.copy(validationRules = f.validationRules :+ rule)
  }

def setRequired(field: Field): Field =
  field match {
    case f: StringInputField => f.copy(isRequired = true)
    case f: IntegerInputField => f.copy(isRequired = true)
    case f: NumberInputField => f.copy(isRequired = true)
  }

 */

/* case class NumericInputField(
  override val id: String,
  label: String,
  override val minimum: Option[Int] = None,
  override val maximum: Option[Int] = None
) extends NumberField(id, label, minimum, maximum)

def numericInputField(
  id: String,
  label: String,
  minimum: Option[Int] = None,
  maximum: Option[Int] = None
): NumericInputField = NumericInputField(id, label, minimum, maximum)
*/
