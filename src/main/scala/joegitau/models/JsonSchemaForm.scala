package joegitau.models

trait SchemaElement {
  def toJsonSchema: Map[String, Any]
}

// ---------------------------------------------------------------------------------------------------------------------
// define Base Schema Elements: Define base schema elements that map directly to JSON Schema types.
case class StringField(
  id: String,
  description: String,
  minLength: Option[Int] = None,
  maxLength: Option[Int] = None,
  pattern: Option[String] = None
) extends SchemaElement {
  def toJsonSchema: Map[String, Any] =
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "string",
        "minLength" -> minLength.getOrElse(0),
        "maxLength" -> maxLength.getOrElse(Int.MaxValue),
        "pattern" -> pattern.getOrElse("")
      )
    )
}

/* Number: used for validating integer and float values */
case class NumberField(
  id: String,
  description: String,
  multipleOf: Option[Int] = None,
  minimum: Option[Int] = None,
  exclusiveMinimum: Option[Int] = None,
  maximum: Option[Int] = None,
  exclusiveMaximum: Option[Int] = None, // keyword must be a number (integer or float) or a boolean.
  allowExclusiveMinMaxAsBool: Boolean = false
) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "number",
        "multipleOf" -> multipleOf.getOrElse(1),
        "minimum" -> minimum.getOrElse(Int.MinValue),
        "exclusiveMinimum" -> exclusiveMinimum.getOrElse(0),
        "maximum" -> maximum.getOrElse(Int.MaxValue),
        "exclusiveMaximum" -> exclusiveMaximum.getOrElse(Int.MaxValue),
        "allowExclusiveMinMaxAsBool" -> allowExclusiveMinMaxAsBool
      )
    )
  }
}

case class BooleanField(id: String, description: String) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "boolean"
      )
    )
  }
}

case class NullField(id: String, description: String) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "null"
      )
    )
  }
}

case class ArrayField(
  id: String,
  description: String,
  items: SchemaElement,
  contains: Option[SchemaElement] = None,
  minItems: Option[Int] = None,
  maxItems: Option[Int] = None,
  uniqueItems: Option[Boolean] = None
) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "array",
        "items" -> items.toJsonSchema,
        "contains" -> contains.map(_.toJsonSchema),
        "minItems" -> minItems.getOrElse(0),
        "maxItems" -> maxItems.getOrElse(Int.MaxValue),
        "uniqueItems" -> uniqueItems.getOrElse(false)
      )
    )
  }
}

case class ObjectField(
  id: String,
  description: String,
  properties: Map[String, SchemaElement],
  required: List[String],
  minProperties: Option[Int] = None,
  maxProperties: Option[Int] = None,
  additionalProperties: Option[Either[Boolean, SchemaElement]] = None
) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "description" -> description,
        "type" -> "object",
        "properties" -> properties.view.mapValues(_.toJsonSchema).toMap,
        "required" -> required,
        "minProperties" -> minProperties.getOrElse(0),
        "maxProperties" -> maxProperties.getOrElse(Int.MaxValue),
        "additionalProperties" -> additionalProperties.map {
          case Left(bool)           => bool
          case Right(schemaElement) => schemaElement.toJsonSchema
        }.getOrElse(true)
      )
    )
  }
}

/**
 * $schema: specifies which draft of the JSON Schema standard the schema adheres to.
 * $id: sets a URI for the schema - which is useful for referencing elements of the schema from inside the schema document
 * title & description: useful for stating intent of the schema.
 * `type`: specifies the data type of the document
 *
 */
case class JsonSchemaForm(
  schema: String,
  id: String,
  title: Option[String] = None,
  description: Option[String] = None,
  `type`: String,
  properties: List[SchemaElement],
  required: List[String]
) {
  def toJsonSchema: Map[String, Any] =
    Map(
      "$schema" -> schema,
      "$id" -> id,
      "title" -> title.getOrElse(""),
      "description" -> description.getOrElse(""),
      "type" -> `type`,
      "properties" -> properties.flatMap(_.toJsonSchema).toMap,
      "required" -> required
    )
}
