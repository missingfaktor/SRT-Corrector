package srt_corrector

import util.Try
import org.joda.time.{Period => JPeriod, LocalTime => JTime}
import java.io.{PrintWriter, Writer}

object SrtCorrector {

  sealed trait Sign
  case object Positive extends Sign
  case object Negative extends Sign

  object Sign {
    def from(s: String) = if (s == "-") Negative else Positive
  }

  class TimeAdjustment(val jPeriod: JPeriod, val sign: Sign)

  object TimeAdjustment {
    val Format = """([+-]?)(\d+):(\d+):(\d+),(\d+)""".r

    def from(srtPeriodString: String) = {
      val TimeAdjustment.Format(sign, hours, minutes, seconds, milliseconds) = srtPeriodString
      val jPeriod = new JPeriod(hours.toInt, minutes.toInt, seconds.toInt, milliseconds.toInt)
      new TimeAdjustment(jPeriod, Sign from sign)
    }
  }

  class Time(jTime: JTime) {
    def adjustedBy(adjustment: TimeAdjustment) = {
      val operation: (JTime, JPeriod) => JTime = adjustment.sign match {
        case Positive => _ plus _
        case Negative => _ minus _
      }
      new Time(operation(jTime, adjustment.jPeriod))
    }

    def asString = {
      "%02d:%02d:%02d,%03d" format (jTime.getHourOfDay, jTime.getMinuteOfHour, jTime.getSecondOfMinute, jTime.getMillisOfSecond)
    }

    override def toString = this.asString
  }

  object Time {
    val Format = """(\d+):(\d+):(\d+),(\d+)""".r

    def from(srtTimeString: String) = {
      val Time.Format(hours, minutes, seconds, milliseconds) = srtTimeString
      val jTime = new JTime(hours.toInt, minutes.toInt, seconds.toInt, milliseconds.toInt)
      new Time(jTime)
    }
  }

  case class Duration(from: Time, to: Time) {
    def adjustedBy(adjustment: TimeAdjustment) = Duration(from adjustedBy adjustment, to adjustedBy adjustment)
    def asString = s"${from.asString} --> ${to.asString}"
  }

  object Duration {
    val Format = """(.*)\s+-->\s+(.*)""".r

    def from(srtDurationString: String) = {
      val Duration.Format(from, to) = srtDurationString
      Duration(Time from from, Time from to)
    }
  }

  case class Subtitle(index: Int, duration: Duration, text: String) {
    def adjustedBy(adjustment: TimeAdjustment) = this.copy(duration = duration.adjustedBy(adjustment))
    def asString = s"$index\n${duration.asString}\n$text"
  }

  class Subtitles(val contents: Iterator[Subtitle]) {
    def adjustedBy(adjustment: TimeAdjustment) = new Subtitles(contents.map(_.adjustedBy(adjustment)))

    def write(writer: Writer): Unit = {
      for (subtitle <- contents) {
        writer.write(subtitle.asString + "\n\n")
      }
      writer.flush
      writer.close
    }
  }

  object Subtitles {
    def fromLines(lines: Iterator[String]) = {
      val iterator = new Iterator[Subtitle] {
        def hasNext: Boolean = lines.hasNext

        def next: Subtitle = {
          val index = lines.next.toInt
          val duration = Duration.from(lines.next)
          val text = Iterator.
            continually(Try(lines.next).getOrElse("")).
            takeWhile(_.nonEmpty).
            mkString("\n")
          Subtitle(index, duration, text)
        }
      }
      new Subtitles(iterator)
    }

    def fromFile(filePath: String) = {
      Subtitles.fromLines(io.Source.fromFile(filePath).getLines)
    }
  }

  def main(args: Array[String]): Unit = {
    val Array(filePath, diff, outPath, _*) = args
    val subtitles = Subtitles.fromFile(filePath)
    val adjustment = TimeAdjustment.from(diff)
    subtitles.adjustedBy(adjustment).write(new PrintWriter(outPath))
  }
}
