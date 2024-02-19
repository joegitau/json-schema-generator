package joegitau.utils

import joegitau.models.FieldFormat.FieldFormat
import joegitau.models.ObjectField.BoolOrSchemaElement
import joegitau.models._

trait SchemaElementBuilder {
  def stringField(description: String): StringField =
    StringField(description)

  def numberField(description: String): NumberField =
    NumberField(description)

  def booleanField(description: String): BooleanField =
    BooleanField( description)

  def nullField(description: String): NullField =
    NullField(description)

  def arrayField(description: String, items: SchemaElement): ArrayField =
    ArrayField(description, items)

  def objectField(
    description: String,
    properties: Map[String, SchemaElement],
    required: List[String]
  ): ObjectField = ObjectField(description, properties, required)

  def jsonSchemaForm(id: String, `type`: String): JsonSchemaForm =
    JsonSchemaForm(
      "https://json-schema.org/draft/2020-12/schema", // latest version
      id,
      `type` = `type`,
      properties = Map.empty[String, SchemaElement],
      required = Nil
    )

  // implicit elementBuilder classes
  implicit class StringFieldOps(field: StringField) {
    def withMinLength(minLength: Int): StringField   = field.copy(minLength = Some(minLength))
    def withMaxLength(maxLength: Int): StringField   = field.copy(maxLength = Some(maxLength))
    def withPattern(pattern: String): StringField    = field.copy(pattern = Some(pattern))
    def withFormat(format: FieldFormat): StringField = field.copy(format = Some(format))
  }

  implicit class NumberFieldOps(field: NumberField) {
    def withMin(min: Int): NumberField               = field.copy(minimum = Some(min))
    def withMax(max: Int): NumberField               = field.copy(maximum = Some(max))
    def withExclusiveMin(exclMin: Int): NumberField  = field.copy(exclusiveMinimum = Some(exclMin))
    def withExclusiveMax(exclMax: Int): NumberField  = field.copy(exclusiveMaximum = Some(exclMax))
    def withMultipleOf(multipleOf: Int): NumberField = field.copy(multipleOf = Some(multipleOf))
    def withFormat(format: FieldFormat): NumberField = field.copy(format = Some(format))
  }

  implicit class ArrayFieldOps(field: ArrayField) {
    def withContains(contains: SchemaElement): ArrayField = field.copy(contains = (Some(contains)))
    def withMinItems(minItems: Int): ArrayField           = field.copy(minItems = Some(minItems))
    def withMaxItems(maxItems: Int): ArrayField           = field.copy(maxItems = Some(maxItems))
    def withUniqueItems(unique: Boolean): ArrayField      = field.copy(uniqueItems = Some(unique))
  }

  implicit class ObjectFieldOps(field: ObjectField) {
    def withProperties(props: Map[String, SchemaElement]): ObjectField =
      field.copy(properties = props)

    def withRequired(required: List[String]): ObjectField =
      field.copy(required = required)

    def withMinProperties(minProps: Int): ObjectField =
      field.copy(minProperties = Some(minProps))

    def withMaxProperties(maxProps: Int): ObjectField =
      field.copy(maxProperties = Some(maxProps))

    def withAdditionalProperties(additionalProps: BoolOrSchemaElement): ObjectField =
      field.copy(additionalProperties = Some(additionalProps))

    def withIfSchema(ifCond: BoolOrSchemaElement): ObjectField =
      field.copy(ifSchema = Some(ifCond))

    def withThenSchema(thenCond: BoolOrSchemaElement): ObjectField =
      field.copy(thenSchema = Some(thenCond))

    def withElseSchema(elseCond: BoolOrSchemaElement): ObjectField =
      field.copy(elseSchema = Some(elseCond))

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
    def withTitle(title: String): JsonSchemaForm =
      schema.copy(title = Some(title))

    def withDescription(description: String): JsonSchemaForm =
      schema.copy(description = Some(description))

    def withProperties(properties: Map[String, SchemaElement]): JsonSchemaForm =
      schema.copy(properties = properties)

    def withRequired(required: List[String]): JsonSchemaForm =
      schema.copy(required = required)

    def withSchemaVersion(version: String): JsonSchemaForm =
      schema.copy(schema = version)
  }

}

object SchemaElementBuilder extends SchemaElementBuilder
