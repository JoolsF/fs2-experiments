//import cats.implicits._
//import vulcan.Codec
//
////https://fd4s.github.io/fs2-kafka/docs/modules
//
//final case class Person(name: String, age: Option[Int])
//
//implicit val personCodec: Codec[Person] =
//  Codec.record(
//    name = "Person",
//    namespace = "com.example"
//  ) { field =>
//    (
//      field("name", _.name),
//      field("age", _.age)
//      ).mapN(Person(_, _))
//  }
//// personCodec: Codec[Person] = Codec({
////   "type" : "record",
////   "name" : "Person",
////   "namespace" : "com.example",
////   "fields" : [ {
////     "name" : "name",
////     "type" : "string"
////   }, {
////     "name" : "age",
////     "type" : [ "null", "int" ]
////   } ]
//// })