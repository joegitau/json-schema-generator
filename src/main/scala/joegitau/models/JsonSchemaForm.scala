package joegitau.models

import joegitau.models.FieldFormat.FieldFormat
import play.api.libs.json._

trait SchemaElement {
  def `type`: String
  def description: String
}

object SchemaElement {
  import ArrayField.arrayFieldWrites
  import BooleanField.booleanFieldWrites
  import NullField.nullFieldWrites
  import NumberField.numberFieldWrites
  import ObjectField.objectFieldWrites
  import StringField.stringFieldWrites

  implicit val schemaElementWrites: OWrites[SchemaElement] = OWrites {
    case sf: StringField  => stringFieldWrites.writes(sf)
    case nf: NumberField  => numberFieldWrites.writes(nf)
    case nuf: NullField   => nullFieldWrites.writes(nuf)
    case bf: BooleanField => booleanFieldWrites.writes(bf)
    case af: ArrayField   => arrayFieldWrites.writes(af)
    case of: ObjectField  => objectFieldWrites.writes(of)
    case _                => Json.obj("error" -> "UnknownSchemaElement")
  }
}

// ---------------------------------------------------------------------------------------------------------------------
// define Base Schema Elements: Define base schema elements that map directly to JSON Schema types.
case class StringField(
  description: String,
  `type`: String = "string",
  minLength: Option[Int] = None,
  maxLength: Option[Int] = None,
  pattern: Option[String] = None,
  format: Option[FieldFormat] = None
) extends SchemaElement

object StringField {
  implicit val stringFieldWrites: OWrites[StringField] = Json.writes
}

/* Number: used for validating integer and float values */
case class NumberField(
  description: String,
  `type`: String = "number",
  multipleOf: Option[Int] = None,
  minimum: Option[Int] = None,
  exclusiveMinimum: Option[Int] = None,
  maximum: Option[Int] = None,
  exclusiveMaximum: Option[Int] = None, // keyword must be a number (integer or float) or a boolean.
  allowExclusiveMinMaxAsBool: Boolean = false,
  format: Option[FieldFormat] = None
) extends SchemaElement

object NumberField {
  implicit val numberFieldWrites: OWrites[NumberField] = Json.writes
}

case class BooleanField(description: String, `type`: String = "boolean") extends SchemaElement

object BooleanField {
  implicit val booleanFieldWrites: OWrites[BooleanField] = Json.writes
}

case class NullField(description: String, `type`: String = "null") extends SchemaElement

object NullField {
  implicit val nullFieldWrites: OWrites[NullField] = Json.writes
}

case class ArrayField(
  description: String,
  items: SchemaElement,
  `type`: String = "array",
  contains: Option[SchemaElement] = None,
  minItems: Option[Int] = None,
  maxItems: Option[Int] = None,
  uniqueItems: Option[Boolean] = None
) extends SchemaElement

object ArrayField {
  implicit val arrayFieldWrites: OWrites[ArrayField] = Json.writes
}

case class ObjectField(
  description: String,
  properties: Map[String, SchemaElement],
  required: List[String],
  `type`: String = "object",
  minProperties: Option[Int] = None,
  maxProperties: Option[Int] = None,
  additionalProperties: Option[Either[Boolean, SchemaElement]] = None,
  ifSchema: Option[Either[Boolean, SchemaElement]] = None,
  thenSchema: Option[Either[Boolean, SchemaElement]] = None,
  elseSchema: Option[Either[Boolean, SchemaElement]] = None,
  notSchema: Option[Either[Boolean, SchemaElement]] = None,
  anyOfSchema: Option[List[Either[Boolean, SchemaElement]]] = None,
  oneOfSchema: Option[List[Either[Boolean, SchemaElement]]] = None,
  allOfSchema: Option[List[Either[Boolean, SchemaElement]]] = None
) extends SchemaElement

object ObjectField {
  type BoolOrSchemaElement = Either[Boolean, SchemaElement]

  implicit val eitherWrites: Writes[BoolOrSchemaElement] = {
    case Left(bool)  => Json.toJson(bool)
    case Right(elem) => Json.toJson(elem)
  }

  implicit val listEitherWrites: Writes[List[BoolOrSchemaElement]] = Writes.list(eitherWrites)

  implicit val objectFieldWrites: OWrites[ObjectField] = Json.writes
}

object FieldFormat extends Enumeration {
  type FieldFormat = Value
  val date, time, `date-time`, duration, email, uri, uuid, regex = Value

  implicit val formatWrites: Writes[FieldFormat.Value] = Writes.enumNameWrites
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
  properties: Map[String, SchemaElement],
  required: List[String]
)

object JsonSchemaForm {
  implicit val schemaFormWrites: OWrites[JsonSchemaForm] = Json.writes
}

/**
 * TIP OF THE DAY:
 *
 * OWrites[T] is a subtype of Writes[T] that produces a JsObject instead of a JsValue.
 * OWrites[T] is typically used when you’re converting a case class or other object-like structure to JSON, where the
 * result should be a JSON object (i.e., a JsObject).
 *
 * On the other hand, Writes[T] is more general and can be used to convert any type T to a JsValue,
 * which could be a JsObject, JsArray, JsString, JsNumber, JsBoolean, or JsNull.
 */
