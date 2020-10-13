import model.Update
import updater.UpdateField.{FieldValue, Fields, SeqField}
import updater.Updater

object Main extends App {

  import Updater._

  val params = Fields(
    "name" -> FieldValue("test"),
    "amount" -> FieldValue(123567),
    "subFoo" -> Fields(
      "sku" -> FieldValue("skuName"),
      "seq" -> SeqField(List(FieldValue(123), FieldValue(222)))
    )
  )

  val updaterFoo = gen[Update]

  println(updaterFoo.update(params))

}
