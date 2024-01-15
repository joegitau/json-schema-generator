package joegitau.utils

import cats.data.Validated.{Invalid, Valid}
import cats.data.ValidatedNec
import cats.implicits.{catsSyntaxOptionId, toFoldableOps}
import joegitau.models._
import play.api.libs.json.{JsArray, JsObject, JsString, Json}

trait FormHelper {
  def loadValidatedModelAsJson(form: DynamicFormConfig): JsObject = {
    val validatedForm: ValidatedNec[String, DynamicFormConfig] = DynamicFormValidator.validateDynamicForm(form)

    validatedForm match {
      case Valid(form) => Json.toJsObject(form)
      case Invalid(errors) =>
        JsObject(
          Map("errors" -> JsArray(errors.toList.map(JsString.apply)))
        )
    }
  }

  val _publisherForm: DynamicFormConfig = DynamicFormConfig(
    Seq(
      FormField(
        InputConfig(
          key = "title",
          `type` = FieldType.TEXT,
          props = Props(
            label = "Publication title".some,
            minLength = 3.some,
            description = "This publication title should not be less than 3 characters.".some,
          ),
          validation = Some(
            ValidationConfig(
              messages = Map("min" -> "Title should not be shorter than 3 characters.")
            )
          ),
        )
      ),
      FormField(
        InputConfig(
          key = "language",
          `type` = FieldType.SELECT,
          props = Props(
            label = "Languages".some,
            options = Seq(
              SelectOption("fin", "fin"),
              SelectOption("eng", "eng"),
              SelectOption("swe", "swe"),
            ).some,
            required = true.some,
          ),
          defaultValue = "fin".some,
        )
      ),
      FormField(
        InputConfig(
          key = "country",
          `type` = FieldType.SELECT,
          props = Props(
            label = "Countries".some,
            options = Seq(
              SelectOption("FIN", "FIN"),
              SelectOption("ENG", "ENG"),
              SelectOption("SWE", "SWE"),
            ).some,
            required = true.some,
          ),
          defaultValue = "FIN".some,
        )
      ),
      FormField(
        InputConfig(
          key = "publicationType",
          `type` = FieldType.RADIO,
          props = Props(
            label = "Publication type".some,
            options = Seq(
              SelectOption("WithDescription", "withDesc"),
              SelectOption("WithoutDescription", "withoutDesc"),
            ).some,
            required = true.some,
          ),
          defaultValue = "withoutDesc".some,
        )
      ),
      FormField(
        InputConfig(
          key = "publicationSize",
          `type` = FieldType.SELECT,
          props = Props(
            label = "Publication size".some,
            options = Seq(
              SelectOption("A3", "A3"),
              SelectOption("A4", "A4"),
              SelectOption("A5", "A5"),
              SelectOption("A6", "A6"),
              SelectOption("16-9", "16-9", true.some),
            ).some,
            required = true.some,
          ),
          defaultValue = "A4".some,
        )
      ),
      FormField(
        InputConfig(
          key = "productSelection",
          `type` = FieldType.SELECT,
          props = Props(
            label = "Product selection".some,
            options = Seq(
              SelectOption("Product codes", "prodCodes"),
              SelectOption("From ERP", "erp"),
              SelectOption("Hierarchy tree", "tree"),
            ).some,
          ),
          defaultValue = "prodCodes".some,
        )
      ),
      FormField(
        InputConfig(
          key = "customerNumber",
          `type` = FieldType.TEXT,
          props = Props(
            label = "Customer Number".some
          ),
          expressions = Some(Map("hide" -> "!model.erp")),
        ),
        value = Some(JsString("14000")),
      ),
    )
  )
}
