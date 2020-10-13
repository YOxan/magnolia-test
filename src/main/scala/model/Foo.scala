package model

case class Update(
    name: Option[String],
    amount: Int,
    subFoo: Option[SubUpdate]
)

case class SubUpdate(sku: String, seq: List[Int])
