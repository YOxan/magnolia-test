package updater

import cats.implicits._
import magnolia._
import updater.UpdateField.{
  FieldShouldExist,
  FieldValue,
  Fields,
  OptFieldValue,
  Param,
  SeqField,
  UpdaterError,
  WrongParamType
}

import scala.language.experimental.macros

trait UpdateClass[T] {
  def update(values: Param): Either[List[UpdaterError], T]
}

object UpdateField {
  sealed trait Param

  case class OptFieldValue[T](fieldName: String, value: Option[Param])
      extends Param
  case class FieldValue[T](value: T) extends Param
  case class Fields(map: Map[String, Param]) extends Param
  case class SeqField[T](values: Seq[Param]) extends Param

  object Fields {
    def apply(params: (String, Param)*): Fields = Fields(params.toMap)
  }

  sealed trait UpdaterError

  case class FieldShouldExist(name: String) extends UpdaterError
  case object WrongParamType extends UpdaterError
}

trait CaseClassUpdater {
  type Typeclass[T] = UpdateClass[T]

  def combine[T](caseClass: CaseClass[Typeclass, T]): Typeclass[T] = {
    def resolveFields(
        fields: Map[String, Param]
    ) =
      caseClass
        .constructEither { param =>
          val value = fields.get(param.label)
          param.typeclass.update(OptFieldValue(param.label, value))
        }
        .leftMap(_.flatten)

    (params: Param) => {
      params match {
        case Fields(fields) => resolveFields(fields)
        case OptFieldValue(_, Some(Fields(fields))) =>
          resolveFields(fields)
        case _ => List(WrongParamType).asLeft
      }
    }
  }

  def dispatch[T](ctx: SealedTrait[Typeclass, T]): Typeclass[T] = ???

  implicit def gen[T]: Typeclass[T] = macro Magnolia.gen[T]
}

object Updater extends CaseClassUpdater {
  implicit def updateClassOpt[T](implicit
      updateClass: UpdateClass[T]
  ): UpdateClass[Option[T]] = {
    case fv: FieldValue[T] => Some(fv.value).asRight
    case optV: OptFieldValue[T] =>
      optV.value.traverse(updateClass.update)
    case _ => List(WrongParamType).asLeft
  }
  implicit def updateClassList[T](implicit
      updateClass: UpdateClass[T]
  ): UpdateClass[List[T]] = {
    case OptFieldValue(_, Some(seqF: SeqField[T])) =>
      seqF.values.toList.traverse(updateClass.update)
    case OptFieldValue(fieldName, None) =>
      List(FieldShouldExist(fieldName)).asLeft
    case _ => List(WrongParamType).asLeft
  }
  implicit def updateClassStr: UpdateClass[String] = updateClassBuild[String]
  implicit def updateClassInt: UpdateClass[Int] = updateClassBuild[Int]

  private def updateClassBuild[T]: UpdateClass[T] = {
    case fv: FieldValue[T]                         => fv.value.asRight
    case OptFieldValue(_, Some(fv: FieldValue[T])) => fv.value.asRight
    case OptFieldValue(fieldName, None) =>
      List(FieldShouldExist(fieldName)).asLeft
    case _ => List(WrongParamType).asLeft
  }
}
