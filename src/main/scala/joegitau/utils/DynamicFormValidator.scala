package joegitau.utils

import cats.data._
import cats.implicits._
import joegitau.models.FieldType.FieldType
import play.api.libs.json.{JsNumber, JsString, JsValue}
import joegitau.models.{DynamicFormConfig, FieldType, FormField, InputConfig, Props, SelectOption}

object DynamicFormValidator {
  private def validateFormField(f: FormField): ValidatedNec[String, FormField] = {
    val keyValidator: ValidatedNec[String, String] =
      if (f.fields.key.nonEmpty) f.fields.key.validNec
      else "Key must be provided".invalidNec

    val fieldTypeValidator: ValidatedNec[String, FieldType] = f.fields.`type`.validNec

    val defaultValueValidator: ValidatedNec[String, Option[String]] = f.fields.`type` match {
      case FieldType.CHECKBOX if f.fields.defaultValue.isEmpty =>
        "Default value must be provided for checkbox".invalidNec
      case FieldType.SELECT | FieldType.RADIO
        if f.fields.defaultValue.isDefined && !f.fields.props.options
          .map(_.value)
          .contains(f.fields.defaultValue.get) =>
        "Default value must be one of the options provided".invalidNec
      case _ => f.fields.defaultValue.validNec
    }

    val expressionsValidator: ValidatedNec[String, Option[Map[String, String]]] = {
      def validateKeys(m: Map[String, String]): ValidatedNec[String, Map[String, String]] = {
        if (m.keys.nonEmpty) m.validNec
        else "Expressions map must contain at least one key.".invalidNec
      }

      def validateValues(m: Map[String, String]) = {
        val valuesValidation = m.values
          .map { v =>
            if (v.nonEmpty) m.validNec
            else "Expression map must contain at least one value".invalidNec
          }
          .toList
          .sequence

        valuesValidation.map(_ => m)
      }

      f.fields.expressions match {
        case Some(m) =>
          (validateKeys(m), validateValues(m)).mapN { case (_, _) => m.some }
        case None => None.validNec
      }
    }

    val valueValidator: ValidatedNec[String, Option[JsValue]] = {
      (f.fields.`type`, f.value) match {
        case (FieldType.NUMBER, Some(v: JsNumber)) => f.value.validNec
        case (FieldType.NUMBER, _) => "Value must be a Number for NUMBER fields".invalidNec

        case (FieldType.TEXT | FieldType.TEXTAREA, Some(v: JsString)) => validateStringField(f)
        case (FieldType.TEXT | FieldType.TEXTAREA, _) => "Value must be a String for String fields".invalidNec

        case (FieldType.DATE, Some(v: JsString)) =>
          if (isValidDate(v.value)) f.value.validNec
          else "Value must be a valid date for DATE fields".invalidNec
        case (FieldType.DATE, _) => "Value must be a valid date string for DATE fields".invalidNec

        case _ => f.value.validNec // for other field types, no specific validation is needed
      }
    }

    // format: off
    (
      keyValidator, fieldTypeValidator, defaultValueValidator, valueValidator, expressionsValidator,
      validateProps(f.fields.props, f),
    ).mapN { (key, fieldType, defaultValue, value, expressions, props) =>
      FormField(
        InputConfig(key, fieldType, props, defaultValue, f.fields.className, expressions),
        value,
      )
    }
  }

  private def validateProps(p: Props, f: FormField): ValidatedNec[String, Props] = {
    val optionsValidator: ValidatedNec[String, Seq[SelectOption]] = f.fields.`type` match {
      case FieldType.SELECT | FieldType.RADIO | FieldType.CHECKBOX if p.options.isEmpty =>
        s"Options must be provided for ${f.fields.`type`}".invalidNec
      case _ => p.options.validNec
    }

    val requiredValidator: ValidatedNec[String, Option[Boolean]] = {
      f.fields.props.required match {
        case Some(value) => value.some.validNec
        case None => "Required must be a boolean value".invalidNec
      }
    }

    val multipleValidator: ValidatedNec[String, Option[Boolean]] = f.fields.`type` match {
      case FieldType.CHECKBOX | FieldType.SELECT if p.multiple.isEmpty =>
        "Multiple must be specified for checkbox or select fields".invalidNec
      case _ => p.multiple.validNec
    }

    val minValidator: ValidatedNec[String, Option[Int]] =
      if (p.min.forall(_ >= 0)) p.min.validNec
      else "Min cannot be negative".invalidNec

    val maxValidator: ValidatedNec[String, Option[Int]] =
      if (p.max.forall(_ >= 0)) p.max.validNec
      else "Max cannot be negative".invalidNec

    val minLengthValidator: ValidatedNec[String, Option[Int]] =
      if p.minLength.forall(_ >= 0)) p.minLength.validNec
      else "MinLength cannot be negative".invalidNec

    val maxLengthValidator: ValidatedNec[String, Option[Int]] =
      if p.maxLength.forall(_ >= 0))  p.maxLength.validNec
      else "MaxLength cannot be negative".invalidNec

    val colsValidator: ValidatedNec[String, Option[Int]] =
      if p.cols.forall(_ > 0)) p.cols.validNec
      else "Cols must be greater than 0".invalidNec

    val readonlyValidator: ValidatedNec[String, Option[Boolean]] =
      p.readonly match {
        case Some(value: Boolean) => value.some.validNec
        case None => "Readonly must be a boolean value".invalidNec
      }

    val disabledValidator: ValidatedNec[String, Option[Boolean]] =
      p.disabled match {
        case Some(value: Boolean) => value.some.validNec
        case None => "Disabled must be a boolean value".invalidNec
      }

    val patternValidator: ValidatedNec[String, Option[String]] =
      p.pattern match {
        case Some(value: String) => value.some.validNec
        case None => "Pattern must be a regular expression".invalidNec // doesn't really validate regex (Scala compiler should?)
      }

    // format: off
    (
      optionsValidator, requiredValidator, multipleValidator, minValidator, maxValidator,
      minLengthValidator, maxLengthValidator, colsValidator, readonlyValidator, disabledValidator, patternValidator,
    ).mapN { (opts, required, multiple, min, max, minLength, maxLength, cols, readonly, disabled, pattern) =>
      Props(
        f.fields.props.label, f.fields.props.placeholder, opts, required, multiple,
        f.fields.props.description, min, max, minLength, maxLength, cols, readonly, disabled, pattern,
      )
    }
  }

  private def validateUniqueFieldNames(df: DynamicFormConfig): ValidatedNec[String, DynamicFormConfig] = {
    val duplicateNames = getDuplicates(df.fields.map(_.fields.key))

    if (duplicateNames.isEmpty) df.validNec
    else s"Duplicate field names found: ${duplicateNames.mkString(", ")}".invalidNec
  }

  private def getDuplicates(values: Seq[String]): Iterable[String] =
    values.groupBy(identity).collect { case (v, List(_, _, _*)) => v }

  private def validateStringField(f: FormField): ValidatedNec[String, Option[JsValue]] = {
    val minLengthValidation = f.fields.props.minLength.fold(().validNec) { minLength =>
      f.value
        .collect {
          case JsString(value) if value.length < minLength =>
            "Value is shorter than the minimum length defined".invalidNec
        }
        .getOrElse(().validNec)
    }

    val maxLengthValidation = f.fields.props.maxLength.fold(().validNec) { maxLength =>
      f.value
        .collect {
          case JsString(value) if value.length > maxLength =>
            "Value is longer than the maximum length defined".invalidNec
        }
        .getOrElse(().validNec)
    }

    (minLengthValidation, maxLengthValidation).mapN { case (_, _) =>
      f.value
    }
  }

  // assert if the value is a valid date format using regex or libraries like java.time.LocalDate.parse
  private def isValidDate(value: String): Boolean = value.matches("\\d{2}.\\d{2}.\\d{4}")

  def validateDynamicForm(form: DynamicFormConfig): ValidatedNec[String, DynamicFormConfig] = {
    val fieldsValidator: ValidatedNec[String, Seq[FormField]] =
      form.fields.traverse(validateFormField)

    val uniqueNamesValidator: ValidatedNec[String, DynamicFormConfig] = validateUniqueFieldNames(form)

    (fieldsValidator, uniqueNamesValidator)
      .mapN((_, _) => form)
  }
}
