package joegitau.models

import joegitau.models.EitherFormat._
import play.api.libs.json._

trait SchemaElement {
  def id: String
  def toJsonSchema: Map[String, Any]
}

object SchemaElement {
  import ArrayField.arrayFieldWrites
  import BooleanField.booleanFieldWrites
  import NullField.nullFieldWrites
  import NumberField.numberFieldWrites
  import ObjectField.objectFieldWrites
  import StringField.stringFieldWrites
  /* implicit val schemaElementFormat: Format[SchemaElement] = new Format[SchemaElement] {
    override def writes(o: SchemaElement): JsValue = o match {
      case s: StringField  => Json.toJson(s)
      case n: NumberField  => Json.toJson(n)
      case b: BooleanField => Json.toJson(b)
      case e: NullField    => Json.toJson(e)
      case a: ArrayField   => Json.toJson(a)
      case o: ObjectField  => Json.toJson(o)
      case _               => Json.obj("error" -> "UnknownSchemaElement")
    }

    override def reads(json: JsValue): JsResult[SchemaElement] = {
      // determine type of the SchemaElement from the "type" property
      (json \ "type").as[String] match {
        case "string"  => json.validate[StringField]
        case "number"  => json.validate[NumberField]
        case "boolean" => json.validate[BooleanField]
        case "null"    => json.validate[NullField]
        case "array"   => json.validate[ArrayField]
        case "object"  => json.validate[ObjectField]
        case _         => JsError("Invalid SchemaElement type")
      }
    }
  } */


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
  id: String,
  description: String,
  minLength: Option[Int] = None,
  maxLength: Option[Int] = None,
  pattern: Option[String] = None
) extends SchemaElement {
  def toJsonSchema: Map[String, Any] =
    Map(
      id -> Map(
        "type" -> "string",
        "description" -> description,
        "minLength" -> minLength.getOrElse(0),
        "maxLength" -> maxLength.getOrElse(Int.MaxValue),
        "pattern" -> pattern.getOrElse("")
      )
        .collect { case (k, Some(v)) => (k, v) } // filter out None values
    )
}

object StringField {
  implicit val stringFieldWrites: OWrites[StringField] = Json.writes
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
        "type" -> "number",
        "description" -> description,
        "multipleOf" -> multipleOf.getOrElse(1),
        "minimum" -> minimum.getOrElse(Int.MinValue),
        "exclusiveMinimum" -> exclusiveMinimum.getOrElse(0),
        "maximum" -> maximum.getOrElse(Int.MaxValue),
        "exclusiveMaximum" -> exclusiveMaximum.getOrElse(Int.MaxValue),
        "allowExclusiveMinMaxAsBool" -> allowExclusiveMinMaxAsBool
      )
        .collect { case (k, Some(v)) => (k, v) }
    )
  }
}

object NumberField {
  implicit val numberFieldWrites: OWrites[NumberField] = Json.writes
}

case class BooleanField(id: String, description: String) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "type" -> "boolean",
        "description" -> description,
      )
    )
  }
}

object BooleanField {
  implicit val booleanFieldWrites: OWrites[BooleanField] = Json.writes
}

case class NullField(id: String, description: String) extends SchemaElement {
  def toJsonSchema: Map[String, Any] = {
    Map(
      id -> Map(
        "type" -> "null",
        "description" -> description,
      )
    )
  }
}

object NullField {
  implicit val nullFieldWrites: OWrites[NullField] = Json.writes
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
        "type" -> "array",
        "description" -> description,
        "items" -> items.toJsonSchema,
        "contains" -> contains.map(_.toJsonSchema),
        "minItems" -> minItems.getOrElse(0),
        "maxItems" -> maxItems.getOrElse(Int.MaxValue),
        "uniqueItems" -> uniqueItems.getOrElse(false)
      )
        .collect { case (k, Some(v)) => (k, v) }
    )
  }
}

object ArrayField {
  implicit val arrayFieldWrites: OWrites[ArrayField] = Json.writes
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
        "type" -> "object",
        "description" -> description,
        "properties" -> properties.view.mapValues(_.toJsonSchema).toMap,
        "required" -> required,
        "minProperties" -> minProperties.getOrElse(0),
        "maxProperties" -> maxProperties.getOrElse(Int.MaxValue),
        "additionalProperties" -> additionalProperties.map {
          case Left(bool)           => bool
          case Right(schemaElement) => schemaElement.toJsonSchema
        }.getOrElse(false)
      )
        .collect { case (k, Some(v)) => (k, v) }
    )
  }
}

object ObjectField {
  implicit val objectFieldWrites: OWrites[ObjectField] = Json.writes
}

object EitherFormat {
  /* implicit val eitherFormat: Format[Either[Boolean, SchemaElement]] = new Format[Either[Boolean, SchemaElement]] {
    override def writes(o: Either[Boolean, SchemaElement]): JsValue = o match {
      case Left(bool)  => Json.toJson(bool)
      case Right(elem) => Json.toJson(elem)
    }

    override def reads(json: JsValue): JsResult[Either[Boolean, SchemaElement]] =
      json.validate[Boolean].map(Left(_))
        .orElse(json.validate[SchemaElement].map(Right(_)))
  } */

  implicit val eitherWrites: Writes[Either[Boolean, SchemaElement]] = {
    case Left(bool)  => Json.toJson(bool)
    case Right(elem) => Json.toJson(elem)
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
  properties: Map[String, SchemaElement],
  required: List[String]
) {
  /* def toJsonSchema: Map[String, Any] =
    Map(
      "$schema" -> schema,
      "$id" -> id,
      "title" -> title.getOrElse(""),
      "description" -> description.getOrElse(""),
      "type" -> `type`,
      "properties" -> properties.flatMap(_.toJsonSchema).toMap,
      "required" -> required
    ) */
}

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
