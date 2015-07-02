import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.sys.process._
import scala.xml._

class funcFinder(){
  def funcName(file:String, lineNo:ListBuffer[Int]) : Set[String] = {
    val cmd = Seq("/home/brodo/Documents/srcML/src2srcml", "--language=C","--position" , "/home/brodo/GitHub/WebCrawler/" + file, "-o", file + ".xml").!!
    val xml = XML.loadFile(file +".xml")
    val func = xml \\ "function"
    val funcMap = mutable.LinkedHashMap[String, List[Int]]()
    for (e <- func){
      val tagLines = e.toString.split("\n").size
      val funcStart = e \ "name" \ "@{http://www.sdml.info/srcML/position}line"
      val funcEnd = funcStart.toString().toInt + tagLines - 1
      val funcText = e \ "name"
      val funcName = funcText.text
      val list = funcStart.toString().toInt :: funcEnd :: Nil
      funcMap.put(funcName, list)
    }

    var funcSet = Set[String]()
    for (line <- lineNo){
      for ((key,value) <-funcMap){
        if (value(0) <= line && value(1) >= line){
          funcSet = funcSet ++ Set(key)
        }
      }
    }
    funcSet
  }
}
