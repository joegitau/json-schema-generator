package joegitau.utils

import joegitau.models.{JsonSchemaForm, SchemaElement}
import joegitau.models.JsonSchemaForm.schemaFormWrites
import play.api.libs.json._

trait SchemaFormHelper extends SchemaElementBuilder {
  def loadSchemaAsJson(schema: JsonSchemaForm): JsObject = {
    Json.toJsObject(schema)
  }

  // properties
  lazy val props: Map[String, SchemaElement] =
    Map(
      "firstName" -> stringField("firstName", "First Name").withMinLength(3),
      "lastName" -> stringField("lastName", "Last Name"),
      "age" -> numberField("age", "Age").withMin(18),
      "address" -> objectField(
        "address",
        "Address",
        Map(
          "city" -> stringField("city", "City"),
          "postalCode" -> numberField("postalCode", "Postal code"),
          "country" -> stringField("country", "Country")
        ),
        required = "address" :: Nil
      )
    )

  lazy val userSchema: JsonSchemaForm = jsonSchemaForm("user", "object")
    .withTitle("User")
    .withDescription("A user in the system")
    .withProperties(props)
    .withRequired("first_name" :: "last_name" :: "age" :: Nil)

  val schemaAsJsObject: JsObject = Json.toJson(userSchema)(schemaFormWrites).as[JsObject]




  /* implicit val schemaElementWrites: Writes[SchemaElement] = (o: SchemaElement) => Json.toJson(o.toJsonSchema)(mapWrites)

  implicit val anyWrites: Writes[Any] = new Writes[Any] {
    def writes(o: Any): JsValue = o match {
      case n: Int              => JsNumber(n)
      case s: String           => JsString(s)
      case b: Boolean          => JsBoolean(b)
      case Some(v)             => writes(v) // Unwrap Option values
      case None                => JsNull
      case list: List[_]       => JsArray(list.map(writes))
      case map: Map[_, _]      => JsObject(map.map { case (k, v) => k.toString -> writes(v) })
      case se: SchemaElement   => schemaElementWrites.writes(se)
      case either: Either[_, _] => either.fold(writes, writes)
      case other => JsString(other.toString) // fallback to string representation
    }
  }

  implicit val mapWrites: Writes[Map[String, Any]] = new Writes[Map[String, Any]] {
    def writes(map: Map[String, Any]): JsValue = {
      JsObject(map.map { case (s, o) =>
        s -> anyWrites.writes(o)
      })
    }
  } */



}
