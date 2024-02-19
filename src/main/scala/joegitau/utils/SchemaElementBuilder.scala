package joegitau.utils

import joegitau.models.FieldFormat.FieldFormat
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
    def withProperties(properties: Map[String, SchemaElement]): ObjectField =
      field.copy(properties = properties)

    def withRequired(required: List[String]): ObjectField =
      field.copy(required = required)

    def withMinProperties(minProperties: Int): ObjectField =
      field.copy(minProperties = Some(minProperties))

    def withMaxProperties(maxProperties: Int): ObjectField =
      field.copy(maxProperties = Some(maxProperties))

    def withAdditionalProperties(additionalProperties: Either[Boolean, SchemaElement]): ObjectField =
      field.copy(additionalProperties = Some(additionalProperties))
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
