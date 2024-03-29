package joegitau.utils

import joegitau.models.JsonSchemaForm.schemaFormWrites
import joegitau.models.{FieldFormat, JsonSchemaForm, SchemaElement}
import play.api.libs.json._

trait SchemaFormHelper extends SchemaElementBuilder {
  val schemaAsJsObject: JsObject = Json.toJsObject(userSchema)
  // val schemaAsJsObject: JsObject = Json.toJson(userSchema)(schemaFormWrites).as[JsObject]

  // properties
  lazy val props: Map[String, SchemaElement] =
    Map(
      "firstName" -> stringField("First Name").withMinLength(3),
      "lastName" -> stringField("Last Name"),
      "age" -> numberField("Age").withMin(18),
      "email" -> stringField("Email address").withFormat(FieldFormat.email),
      "address" -> objectField("Address")
        .withObjProperties(
          Map(
            "city" -> stringField("City"),
            "postalCode" -> numberField("Postal code"),
            "country" -> stringField("Country")
          )
        )
        .withObjRequired("address" :: Nil)
        .withAdditionalProperties(Left(false))
    )

  private lazy val userSchema: JsonSchemaForm = jsonSchemaForm("user", "object")
    .withTitle("User")
    .withDescription("A user in the system")
    .withProperties(props)
    .withRequired("firstName" :: "lastName" :: "age" :: Nil)

}
