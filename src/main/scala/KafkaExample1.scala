import cats.effect.{ContextShift, ExitCode, IO}
import cats.syntax.functor._
import fs2.kafka._

import scala.concurrent.duration._

class KafkaExample1 {

  def stream(implicit cs: ContextShift[IO]): IO[ExitCode] = {
    def processRecord(
        record: ConsumerRecord[String, String]
    ): IO[(String, String)] =
      IO.pure(record.key -> record.value)

    val consumerSettings =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    val producerSettings =
      ProducerSettings[IO, String, String]
        .withBootstrapServers("localhost:9092")
    consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("topic"))
      .flatMap(_.stream)
      .mapAsync(25) { committable =>
        processRecord(committable.record)
          .map {
            case (key, value) =>
              val record = ProducerRecord("topic", key, value)
              ProducerRecords.one(record, committable.offset)
          }
      }
      .through(produce(producerSettings))
      .map(_.passthrough)
      .through(commitBatchWithin(500, 15.seconds))

  }

}
