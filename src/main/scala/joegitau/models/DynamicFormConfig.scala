package joegitau.models

import joegitau.models.FieldType.FieldType
import play.api.libs.json.{JsValue, Json, OFormat}

case class DynamicFormConfig(fields: Seq[FormField])

object DynamicFormConfig {
  implicit val dynamicFormat: OFormat[DynamicFormConfig] = Json.format
}

case class FormField(
  fields: InputConfig,
  value: Option[JsValue] = None,
)

object FormField {
  implicit val formFieldFormat: OFormat[FormField] = Json.format
}

/**
 * `key` - links the field value to the model
 *
 * `className`- specify own class that will be applied to the formly-field`
 * component.
 *
 * `expressions` - An object where the key is a property to be set on the main
 * field config and the value is an expression used to assign that property.
 */
case class InputConfig(
  key: String,
  `type`: FieldType,
  props: Props,
  defaultValue: Option[String] = None,
  className: Option[String] = None,
  expressions: Option[Map[String, String]] = None,
  validation: Option[ValidationConfig] = None,
)

object InputConfig {
  implicit val inputConfigFormat: OFormat[InputConfig] = Json.format
}

/**
 *   - `validation.messages`: A map of message names that will be displayed when
 *     the field has errors.
 *
 *   - `validation.show`: A boolean you as the developer can set to force
 *     displaying errors whatever the state of field. This is useful when you're
 *     trying to call the user's attention to some fields for some reason.
 */
case class ValidationConfig(
  messages: Map[String, String] = Map(),
  show: Boolean = false,
)

object ValidationConfig {
  implicit val validationConfigFormat: OFormat[ValidationConfig] = Json.format
}

case class Props(
  label: Option[String] = None,
  placeholder: Option[String] = None,
  options: Seq[SelectOption] = Seq(),
  required: Option[Boolean] = Some(false),
  multiple: Option[Boolean] = None,
  description: Option[String] = None,
  min: Option[Int] = None,
  max: Option[Int] = None,
  minLength: Option[Int] = None,
  maxLength: Option[Int] = None,
  cols: Option[Int] = None,
  readonly: Option[Boolean] = None,
  disabled: Option[Boolean] = None,
  pattern: Option[String] = None,
)

object Props {
  implicit val propsFormat: OFormat[Props] = Json.format
}

case class SelectOption(
  label: String,
  value: String,
  disabled: Option[Boolean] = None,
)

object SelectOption {
  implicit val selectOptFormat: OFormat[SelectOption] = Json.format
}

object FieldType extends Enumeration {
  type FieldType = Value

  val TEXT, TEXTAREA, NUMBER, DATE, CHECKBOX, RADIO, SELECT = Value
}
