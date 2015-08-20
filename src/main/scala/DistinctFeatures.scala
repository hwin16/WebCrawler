/**
 * Created by guest on 8/13/15.
 */
import java.io.{FileWriter, BufferedWriter}
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.sys.process._
import scala.xml.XML
object DistinctFeatures extends App{
  val filename = "numfeat.txt"
  var featSet = ListBuffer[String]()
  for (line <- Source.fromFile(filename).getLines()){
  val line = "networking/ifenslave.c,0,5"
    var nameSet = Set[String]()
    val path = line.substring(0, line.indexOf(","))
    val file = path.substring(path.lastIndexOf("/")+1)
    val cmd = Seq("/Users/guest/Downloads/srcML/src2srcml", "--language=C", "/Users/guest/Dropbox/Research/LinuxConfig/TypeChef-BusyBoxAnalysis/busybox-1.18.5/" + path, "-o", file + ".xml").!!
    val xml = XML.loadFile(file + ".xml")

    val ifs = xml \\ "if" filter(z => z.namespace == "http://www.sdml.info/srcML/cpp")
    val name = ifs \\ "name"
    for (a <- name){
      val string = a.toString()
      val lastIndex = string.substring(string.indexOf(">")+1, string.lastIndexOf("<"))
      nameSet = nameSet + lastIndex
    }

    val ifdef = xml \\ "ifdef" filter(z => z.namespace == "http://www.sdml.info/srcML/cpp")
    val names= ifdef \\ "name"
    for (a <- names){
      val string = a.toString()
      val lastIndex = string.substring(string.indexOf(">")+1, string.lastIndexOf("<"))
      nameSet = nameSet + lastIndex
    }

    val ifndef = xml \\ "ifndef" filter(z => z.namespace == "http://www.sdml.info/srcML/cpp")
    val nName = ifndef \\ "name"
    for (a <- nName){
      val string = a.toString()
      val lastIndex = string.substring(string.indexOf(">")+1, string.lastIndexOf("<"))
      nameSet = nameSet + lastIndex
    }
    val createString = line + "," + nameSet.size
    val rString = createString.replaceAll(",", "#")
    featSet += createString
    val del = Seq("rm", file+".xml").!!
  }

  val writer = new BufferedWriter(new FileWriter("numfeat2.txt"))
  for (e <- featSet){
    val changeTComma = e.replaceAll("#", ",")
    writer.write(changeTComma + "\n")
  }
  writer.close()
}
