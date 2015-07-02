import java.io.FileOutputStream
import java.net.URL
import java.nio.channels.Channels
import scala.sys.process._
import org.jsoup._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Breaks

object GitScraper extends App{
//  val CVEID = id
//  val CVEURL = url
  val CVEID = "CVE-2015-1593"
  val cveURL = "http://www.cvedetails.com/cve/CVE-2015-1593/"
  val lineList = new mutable.Queue[String]()

  val doc = Jsoup.connect(cveURL).get()
  val listtable = doc.select("table.listtable#vulnrefstable")
  val tag = listtable.select("td a[href~=.*git.*")
  val github = listtable.select("td a[href^=https://github.com")
  val gitkernel = listtable.select("td a[href^=http://git.kernel.org")
  if (tag == null || tag.isEmpty()){
    println("noFuncFound")
  }
  else if (github != null || !github.isEmpty){
    val gitlink = github.attr("abs:href")
    val gitDoc = Jsoup.connect(gitlink).get()

    //populate Queue
    val linegroup = gitDoc.select("tr td.blob-num.blob-num-addition.js-linkable-line-number").toArray
    for (td <- linegroup){
      val splitid = td.toString.split(" ")
      val id = splitid(1)
      val diff = id.substring(id.indexOf("\"")+1, id.lastIndexOf("\""))
      lineList.enqueue(diff)
    }

    val fileref = gitDoc.select("div.file-header a").toString.split("\n")

    var funcSet = Set[String]()
    for(e <- fileref){
      val splitspace = e.split(" ")
      val href = splitspace(1)
      val link = href.substring(href.indexOf("\"")+1, href.size-1)
      val absLink = "https://github.com" + link
      val filename = absLink.substring(absLink.lastIndexOf("/")+1)

      val randombreak = new Breaks
      var lineNum = Seq[String]()
      randombreak.breakable {
        while (!lineList.isEmpty) {
          val firstItem = lineList.dequeue()
          val dstring = firstItem.substring(firstItem.indexOf("-") + 1, firstItem.indexOf("-") + 4)
          lineNum = lineList.dequeueAll(p => p.contains(dstring)) :+ firstItem
          randombreak.break()
        }
      }

      downloadFile(absLink, filename)
      val lineNumbers = getLineNumbers(lineNum)
      println(lineNumbers)

      val findFunc = new funcFinder()
      val funcNames = findFunc.funcName(filename, lineNumbers)
      funcSet = funcSet ++ funcNames
      val cmd = Seq("rm", filename, filename+".xml").!!
    }
    println(funcSet)
  }

  def downloadFile(url:String, filename:String) = {
    val parser = Jsoup.connect(url).get()
    val header = parser.select("div.file-header a")
    val container = header.select("a:contains(Raw)").attr("abs:href")
    val weblink = new URL(container)
    val openStream = Channels.newChannel(weblink.openStream())
    val outputStream = new FileOutputStream(filename)
    outputStream.getChannel.transferFrom(openStream, 0, Long.MaxValue)
  }

  def getLineNumbers(buffer:Seq[String]): ListBuffer[Int] ={
    val list = ListBuffer[Int]()
    for (elements <- buffer){
      val posR = elements.substring(elements.indexOf("R")+1)
      list +=  posR.toInt
    }
    list
  }
}
