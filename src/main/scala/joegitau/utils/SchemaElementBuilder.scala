package joegitau.utils

import joegitau.models._

trait SchemaElementBuilder {
  def stringField(id: String, description: String): StringField =
    StringField(id, description)

  def numberField(id: String, description: String): NumberField =
    NumberField(id, description)

  def booleanField(id: String, description: String): BooleanField =
    BooleanField(id, description)

  def nullField(id: String, description: String): NullField =
    NullField(id, description)

  def arrayField(id: String, description: String, items: SchemaElement): ArrayField =
    ArrayField(id, description, items)

  def objectField(
    id: String,
    description: String,
    properties: Map[String, SchemaElement],
    required: List[String]
  ): ObjectField = ObjectField(id, description, properties, required)

  def jsonSchemaForm(id: String, `type`: String): JsonSchemaForm =
    JsonSchemaForm(
      "https://json-schema.org/draft/2020-12/schema", // latest version
      id,
      `type` = `type`,
      properties = Nil,
      required = Nil
    )

  // implicit elementBuilder classes
  implicit class StringFieldOps(field: StringField) {
    def withMinLength(minLength: Int): StringField = field.copy(minLength = Some(minLength))
    def withMaxLength(maxLength: Int): StringField = field.copy(maxLength = Some(maxLength))
    def withPattern(pattern: String): StringField  = field.copy(pattern = Some(pattern))
  }

  implicit class NumberFieldOps(field: NumberField) {
    def withMin(min: Int): NumberField = field.copy(minimum = Some(min))
    def withMax(max: Int): NumberField = field.copy(maximum = Some(max))
    def withExclusiveMin(exclMin: Int): NumberField         = field.copy(exclusiveMinimum = Some(exclMin))
    def withExclusiveMax(exclMax: Int): NumberField         = field.copy(exclusiveMaximum = Some(exclMax))
    def multipleOf(multipleOf: Int): NumberField            = field.copy(multipleOf = Some(multipleOf))
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

    def withProperties(properties: List[SchemaElement]): JsonSchemaForm =
      schema.copy(properties = properties)

    def withRequired(required: List[String]): JsonSchemaForm =
      schema.copy(required = required)

    def withSchemaYear(schemaVersion: String): JsonSchemaForm =
      schema.copy(schema = schemaVersion)
  }

}

object SchemaElementBuilder extends SchemaElementBuilder
