package joegitau.utils

import cats.data._
import cats.implicits._
import joegitau.models._
import play.api.libs.json.{JsNumber, JsString, JsValue}

object DynamicFormValidator {
  private def validateFormField(f: FormField): ValidatedNec[String, FormField] = {
    val keyValidator: ValidatedNec[String, String] =
      if (f.fields.key.nonEmpty) f.fields.key.validNec
      else s"Key must be provided on '${f.fields.props.label.getOrElse("")}'".invalidNec

    val defaultValueValidator: ValidatedNec[String, Option[String]] = f.fields.`type` match {
      case FieldType.CHECKBOX if f.fields.defaultValue.isEmpty =>
        s"Default value must be provided for checkbox on '${f.fields.props.label.getOrElse("")}'".invalidNec
      case FieldType.SELECT | FieldType.RADIO
        if f.fields.defaultValue.isDefined && !f.fields.props.options
          .exists(_.exists(_.value == f.fields.defaultValue.get)) =>
        s"Default value must be one of the options provided on '${f.fields.props.label.getOrElse("")}'".invalidNec
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
        case (FieldType.NUMBER, None) => f.value.validNec // treat None as valid
        case (FieldType.NUMBER, _) => s"Value must be a Number for NUMBER fields on '${f.fields.props.label.getOrElse("")}'".invalidNec

        case (FieldType.TEXT | FieldType.TEXTAREA, Some(v: JsString)) => validateStringField(f)
        case (FieldType.TEXT | FieldType.TEXTAREA, None) => f.value.validNec // treat None as valid
        case (FieldType.TEXT | FieldType.TEXTAREA, _) => s"Value must be a String for String fields '${f.fields.props.label.getOrElse("")}'".invalidNec

        case (FieldType.DATE, Some(v: JsString)) =>
          if (isValidDate(v.value)) f.value.validNec
          else s"Value must be a valid date for DATE fields on '${f.fields.props.label.getOrElse("")}'".invalidNec
        case (FieldType.DATE, None) => f.value.validNec // treat None as valid
        case (FieldType.DATE, _) => s"Value must be a valid date string for DATE fields on '${f.fields.props.label.getOrElse("")}'".invalidNec

        case _ => f.value.validNec // for other field types, no specific validation is needed
      }
    }

    // format: off
    (
      keyValidator, defaultValueValidator, valueValidator, expressionsValidator,
      validateProps(f.fields.props, f),
    ).mapN { (key, defaultValue, value, expressions, props) =>
      FormField(
        InputConfig(key, f.fields.`type`, props, defaultValue, f.fields.className, expressions),
        value,
      )
    }
  }

  private def validateProps(p: Props, f: FormField): ValidatedNec[String, Props] = {
    val optionsValidator: ValidatedNec[String, Option[Seq[SelectOption]]] = f.fields.`type` match {
      case FieldType.SELECT | FieldType.RADIO | FieldType.CHECKBOX if p.options.isEmpty =>
        s"Options must be provided for ${f.fields.`type`} on '${f.fields.props.label.getOrElse("")}'".invalidNec
      case _ => p.options.validNec
    }

    val multipleValidator: ValidatedNec[String, Option[Boolean]] = f.fields.`type` match {
      case FieldType.CHECKBOX | FieldType.SELECT if p.multiple.isEmpty =>
        s"Multiple must be specified for checkbox or select fields on '${f.fields.props.label.getOrElse("")}'".invalidNec
      case _ => p.multiple.validNec
    }

    val minValidator: ValidatedNec[String, Option[Int]] =
      if (p.min.forall(_ >= 0)) p.min.validNec
      else s"Min cannot be negative on '${f.fields.props.label.getOrElse("")}'".invalidNec

    val maxValidator: ValidatedNec[String, Option[Int]] =
      if (p.max.forall(_ >= 0)) p.max.validNec
      else s"Max cannot be negative on '${f.fields.props.label.getOrElse("")}'".invalidNec

    val minLengthValidator: ValidatedNec[String, Option[Int]] =
      if (p.minLength.forall(_ >= 0)) p.minLength.validNec
      else s"MinLength cannot be negative on '${f.fields.props.label.getOrElse("")}'".invalidNec

    val maxLengthValidator: ValidatedNec[String, Option[Int]] =
      if (p.maxLength.forall(_ >= 0))  p.maxLength.validNec
      else s"MaxLength cannot be negative on '${f.fields.props.label.getOrElse("")}'".invalidNec

    val colsValidator: ValidatedNec[String, Option[Int]] =
      if (p.cols.forall(_ > 0)) p.cols.validNec
      else s"Cols must be greater than 0 on '${f.fields.props.label.getOrElse("")}'".invalidNec

    // format: off
    (
      optionsValidator,  multipleValidator, minValidator, maxValidator,
      minLengthValidator, maxLengthValidator, colsValidator
    ).mapN { (opts, multiple, min, max, minLength, maxLength, cols) =>
      Props(
        f.fields.props.label, f.fields.props.placeholder, opts, f.fields.props.required, multiple,
        f.fields.props.description, min, max, minLength, maxLength, cols, f.fields.props.readonly,
        f.fields.props.disabled, f.fields.props.pattern,
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
    val minLengthValidation = f.fields.props.minLength.fold("".validNec[String]) { minLength =>
      f.value
        .collect {
          case JsString(value) if value.length < minLength =>
            "Value is shorter than the minimum length defined".invalidNec
        }
        .getOrElse("".validNec)
    }

    val maxLengthValidation = f.fields.props.maxLength.fold("".validNec[String]) { maxLength =>
      f.value
        .collect {
          case JsString(value) if value.length > maxLength =>
            "Value is longer than the maximum length defined".invalidNec
        }
        .getOrElse("".validNec)
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
