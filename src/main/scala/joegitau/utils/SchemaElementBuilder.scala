package joegitau.utils

import joegitau.models.FieldFormat.FieldFormat
import joegitau.models.ObjectField.BoolOrSchemaElement
import joegitau.models._
import play.api.libs.json.{JsArray, JsValue}

trait SchemaElementBuilder {
  def stringField(description: String): StringField = StringField(description)

  def numberField(description: String): NumberField = NumberField(description)

  def booleanField(description: String): BooleanField = BooleanField( description)

  def nullField(description: String): NullField = NullField(description)

  def arrayField(description: String, items: SchemaElement): ArrayField = ArrayField(description, items)

  def objectField(description: String): ObjectField = ObjectField(description)

  def refField(ref: String): RefField = RefField(ref)

  def jsonSchemaForm(id: String, `type`: String): JsonSchemaForm =
    JsonSchemaForm(
      "https://json-schema.org/draft/2020-12/schema", // latest version
      id,
      `type` = `type`,
    )

  // implicit elementBuilder classes
  implicit class StringFieldOps(field: StringField) {
    def withId(id: String): StringField              = field.copy(id = Some(id))
    def withDefault(value: String): StringField      = field.copy(default = Some(value))
    def withMinLength(minLength: Int): StringField   = field.copy(minLength = Some(minLength))
    def withMaxLength(maxLength: Int): StringField   = field.copy(maxLength = Some(maxLength))
    def withPattern(pattern: String): StringField    = field.copy(pattern = Some(pattern))
    def withFormat(format: FieldFormat): StringField = field.copy(format = Some(format))
    def withConst(const: String):StringField         = field.copy(const = Some(const))
    def withEnum(enums: List[String]): StringField   = field.copy(enum = Some(enums))
  }

  implicit class NumberFieldOps(field: NumberField) {
    def withId(id: String): NumberField              = field.copy(id = Some(id))
    def withDefault(value: Int): NumberField         = field.copy(default = Some(value))
    def withMin(min: Int): NumberField               = field.copy(minimum = Some(min))
    def withMax(max: Int): NumberField               = field.copy(maximum = Some(max))
    def withExclusiveMin(exclMin: Int): NumberField  = field.copy(exclusiveMinimum = Some(exclMin))
    def withExclusiveMax(exclMax: Int): NumberField  = field.copy(exclusiveMaximum = Some(exclMax))
    def withMultipleOf(multipleOf: Int): NumberField = field.copy(multipleOf = Some(multipleOf))
    def withFormat(format: FieldFormat): NumberField = field.copy(format = Some(format))
    def withConst(const: Int):NumberField            = field.copy(const = Some(const))
    def withEnum(enums: List[Int]): NumberField      = field.copy(enum = Some(enums))
  }

  implicit class BooleanFieldOps(field: BooleanField) {
    def withId(id: String): BooleanField       = field.copy(id = Some(id))
    def withConst(const: Boolean):BooleanField = field.copy(const = Some(const))
  }

  implicit class ArrayFieldOps(field: ArrayField) {
    def withId(id: String): ArrayField                    = field.copy(id = Some(id))
    def withContains(contains: SchemaElement): ArrayField = field.copy(contains = (Some(contains)))
    def withMinItems(minItems: Int): ArrayField           = field.copy(minItems = Some(minItems))
    def withMaxItems(maxItems: Int): ArrayField           = field.copy(maxItems = Some(maxItems))
    def withUniqueItems(unique: Boolean): ArrayField      = field.copy(uniqueItems = Some(unique))
    def withConst(const: JsArray):ArrayField              = field.copy(const = Some(const))
    def withEnum(enums: List[JsArray]): ArrayField        = field.copy(enum = Some(enums))
  }

  implicit class ObjectFieldOps(field: ObjectField) {
    def withId(id: String): ObjectField                                   = field.copy(id = Some(id))
    def withObjProperties(props: Map[String, SchemaElement]): ObjectField = field.copy(properties = Some(props))
    def withObjRequired(required: List[String]): ObjectField              = field.copy(required = Some(required))
    def withMinProperties(minProps: Int): ObjectField                     = field.copy(minProperties = Some(minProps))
    def withMaxProperties(maxProps: Int): ObjectField                     = field.copy(maxProperties = Some(maxProps))
    def withIfSchema(ifCond: BoolOrSchemaElement): ObjectField            = field.copy(ifSchema = Some(ifCond))
    def withThenSchema(thenCond: BoolOrSchemaElement): ObjectField        = field.copy(thenSchema = Some(thenCond))
    def withElseSchema(elseCond: BoolOrSchemaElement): ObjectField        = field.copy(elseSchema = Some(elseCond))
    def withConst(const: JsValue):ObjectField                             = field.copy(const = Some(const))
    def withEnum(enums: List[JsValue]): ObjectField                       = field.copy(enum = Some(enums))
    def withAdditionalProperties(additionalProps: BoolOrSchemaElement): ObjectField =
      field.copy(additionalProperties = Some(additionalProps))

    /**
     * Used when you want the data to be valid if it does not match the provided schema.
     * e.g, let’s say a username cannot be a number:
     * withNotSchema(
     *   Right(numberField("Username"))
     * )
     *
     * in this case, "username" field will be valid only if it does not match the numberField
     */
    def withNotSchema(notCond: BoolOrSchemaElement): ObjectField =
      field.copy(notSchema = Some(notCond))

    /**
     * Used when you want the data to validate against has at least one of the provided schemas.
     * E.g., let’s say a user can provide their "age" either as a number or a string.
     *
      withAnyOfSchema(
        Right(numberField("Age as number")) ::
        Right(stringField("Age as string")) ::
        Nil
      )
     *
     * in this case, "age" field will be valid if it matches either the numberField or the stringField.
     */
    def withAnyOfSchema(anyOfCond: List[BoolOrSchemaElement]): ObjectField =
      field.copy(anyOfSchema = Some(anyOfCond))

    /**
     *  Used when you want the data to validate against has exactly one of the provided schemas.
     *  e.g., let’s say a user can provide their contact info as either an email or a phone number, but not both!
     *
      withOneOfSchema(
        Right(stringField("Email").withFormat(FieldFormat.email)) ::
        Right(stringField("Phone Number")) ::
        Nil
      )
     *
     * in this case, "contact" field will be valid if it matches either the email or the phone number, but not both.
     */
    def withOneOfSchema(oneOfCond: List[BoolOrSchemaElement]): ObjectField =
      field.copy(oneOfSchema = Some(oneOfCond))

    /**
     * Used when you want the data to validate against has all of the provided schemas.
     * e.g., let’s say a user’s password must meet several criteria:
     *
     withAllOfSchema(
       Right(stringField("Password").withMinLength(8)) ::
       Right(stringField("Password").withPattern("[A-Z]")) :: // contains an uppercase letter
       Right(stringField("Password").withPattern("[a-z]")) :: // contains a lowercase letter
       Right(stringField("Password").withPattern("[0-9]")) :: // contains a digit
       Nil
     )
     *
     * in this case, "password" field will be valid only if it matches all of the provided schemas.
     */
    def withAllOfSchema(allOfCond: List[BoolOrSchemaElement]): ObjectField =
      field.copy(allOfSchema = Some(allOfCond))
  }

  implicit class FormSchemaOps(schema: JsonSchemaForm) {
    def withTitle(title: String): JsonSchemaForm = schema.copy(title = Some(title))
    def withDescription(description: String): JsonSchemaForm =
      schema.copy(description = Some(description))

    def withProperties(properties: Map[String, SchemaElement]): JsonSchemaForm =
      schema.copy(properties = properties)

    def withRequired(required: List[String]): JsonSchemaForm = schema.copy(required = required)

    def withSchemaVersion(version: String): JsonSchemaForm = schema.copy(schema = version)

    def withDef(defs: Map[String, SchemaElement]): JsonSchemaForm = schema.copy(`$defs` = Some(defs))
  }

}

object SchemaElementBuilder extends SchemaElementBuilder
