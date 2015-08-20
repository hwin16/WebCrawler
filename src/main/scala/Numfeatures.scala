/**
 * Created by guest on 8/5/15.
 */

import java.io.{FileWriter, BufferedWriter}

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.sys.process._
import scala.xml.XML

object Numfeatures extends App {
  val filename = "busyboxCfiles3.txt"
  val featlist = ListBuffer[String]()
  for (line <- Source.fromFile(filename).getLines()) {
    val path = line.substring(0, line.indexOf(","))
    val file = path.substring(path.lastIndexOf("/")+1)
    val cmd = Seq("/Users/guest/Downloads/srcML/src2srcml", "--language=C", "/Users/guest/Dropbox/Research/LinuxConfig/TypeChef-BusyBoxAnalysis/busybox-1.18.5/" + path, "-o", file + ".xml").!!
    val xml = XML.loadFile(file + ".xml")
    val endif = xml \\ "endif"
    if (!endif.isEmpty) {
      val length = endif.toString().split("</cpp:endif>").length
      val numfeat = line + "," + length
      val numfeats = numfeat.replaceAll(",", "#")
      featlist += numfeats
    }else {
      val numfeat = line + "," + 0
      featlist += numfeat
    }
    val del = Seq("rm", file+".xml").!!
  }

  val writer = new BufferedWriter(new FileWriter("numfeat.txt"))
  for (e <- featlist){
    val changeTComma = e.replaceAll("#", ",")
    writer.write(changeTComma + "\n")
  }
  writer.close()

  // unique features
  // <cpp:if>#<cpp:directive>if</cpp:directive> <expr><name>DEBUG</name></expr></cpp:if>
  // <cpp:ifndef>#<cpp:directive>ifndef</cpp:directive> <name>PIPE_BUF</name></cpp:ifndef>
}
