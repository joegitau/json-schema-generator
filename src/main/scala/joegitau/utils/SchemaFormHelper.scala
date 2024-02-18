package joegitau.utils

import joegitau.models.{JsonSchemaForm, SchemaElement}
import play.api.libs.json.{JsObject, Json}

trait SchemaFormHelper extends SchemaElementBuilder {
  def loadSchemaAsJson(schema: JsonSchemaForm): JsObject = {
    Json.toJsObject(schema)
  }

  // properties
  lazy val props: List[SchemaElement] =
    stringField("first_name", "First Name").withMinLength(3) ::
    stringField("last_name", "Last Name") ::
    numberField("age", "Age").withMin(18) ::
    objectField(
      "address",
      "Address",
      Map(
        "city" -> stringField("city", "City"),
        "postal_code" -> numberField("code", "Postal code"),
        "country" -> stringField("country", "Country")
      ),
      required = "address" :: Nil
    ) :: Nil

  val userSchema: JsonSchemaForm = jsonSchemaForm("user", "object")
    .withTitle("User")
    .withDescription("A user in the system")
    .withProperties(props)
    .withRequired("first_name" :: "last_name" :: "age" :: Nil)

}
