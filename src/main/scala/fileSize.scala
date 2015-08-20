import java.io.{FileWriter, BufferedWriter}

import scala.io.Source

/**
 * Created by guest on 8/13/15.
 */
import scala.sys.process._
object fileSize extends App{
  val filename = "numfeat2.txt"
  val writer = new BufferedWriter(new FileWriter("numfeat3.txt"))
  for (line <- Source.fromFile(filename).getLines()){
    val path = line.substring(0, line.indexOf(","))
    val cmd = Seq("du", "-sh", "/Users/guest/Dropbox/Research/LinuxConfig/TypeChef-BusyBoxAnalysis/busybox-1.18.5/" + path)!!
    val ag = line + "," + cmd
    val rwhitespace = ag.trim()
    val filesize = rwhitespace.substring(0, rwhitespace.indexOf("K"))
    writer.write(filesize + "\n")
  }
  writer.close()
}
