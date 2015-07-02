import java.io.{FileWriter, BufferedWriter}
import org.jsoup._
import org.jsoup.nodes._
import org.jsoup.select.Elements

/**
 * Created by htut khine on 6/11/15.
 */
object WebScraper extends App {

  val originalURL = parseURL("http://www.cvedetails.com/product/47/Linux-Linux-Kernel.html?vendor_id=33")
  val yearList = extractYear(originalURL)
  var combinedList = List[String]()
  for (e <- yearList){
    val subURL = extractURL(e, originalURL)
    val checkPages = checkNumPages(subURL)
    for (pageURL <- checkPages){
      val subList = extractElements(pageURL)
      combinedList = subList ::: combinedList
    }
  }
  val csvString = composeCSV(combinedList)

  def extractYear(e:Document): List[String] = {
    val readPage = e.body()
    val selector = readPage.select("table.barchart:contains(Vulnerabilities by Year) tr td[title]").text().split(" ")
    var list = List[String]()
    for (e <- 1 to selector.size){
      val titles = readPage.select("table.barchart:contains(Vulnerabilities by Year) tr td[title]:nth-child(" + e + ")").attr("title")
      list = list :+ titles
    }
    println("returning years complete.")
    list
  }

  def extractURL(year:String, doc:Document): String = {
    val elements = doc.select("a[href^=/vulnerability-list/vendor_id-33/product_id-47/year-" + year + "/Linux")
    val urlF = "http://www.cvedetails.com" + elements.toString().replaceFirst(" ", "").replaceFirst(".html(.*)", "").replaceAll("<ahref=\"", "")
    urlF
  }

  def parseURL(url: String):Document ={
    val doc = Jsoup.connect(url).timeout(0).get()
    doc
  }

  def extractElements(url:String) : List[String] = {
    val v = parseURL(url)
    val CVEID = v.select("a[href^=/cve/").text().split(" ")
    var combinedList = List[String]()
    for (id <- CVEID){
      var list = List[String]()
      val trclass = v.select("tr.srrowns:contains("+ id + ")")
      val parseList = tdParser(id,trclass)
      combinedList = combinedList :+ parseList
      println(id + " done.")
    }
    combinedList
  }

  private def checkNumPages(url: String): List[String] = {
    val e = parseURL(url)
    var pageList = List[String]()
    val links = e.select("div.paging#pagingb a")
    val size = links.toString().split("\n").length
    for (n <- 2 to size+1){
      val link = links.select("a:nth-child(" + n + ")")
      val absLink = link.attr("abs:href")
      pageList = pageList :+ absLink
    }
    pageList
  }

  private def tdParser(id:String, element: Elements) : String = {
    val html = element.html()
    var gitList = List[String]()
    val gitScraper = new GitScraper(id, "http://www.cvedetails.com/cve/" + id)
    gitList = gitScraper.gitFuncGrabber().toList
    val splitByTd = html.split("</td>")
    var returnList = List[String]()
    for (n <- splitByTd) {
      val firstI = n.indexOf(">")
      val value = n.substring(firstI + 1, n.length)
      val replace = value.replaceAll("<(.*?)>", "").trim()
      returnList = returnList :+ replace.trim()
    }
    returnList = returnList ++ gitList
    val combine = returnList.mkString("/")
    combine
  }

  def composeCSV(cvelist: List[String]){
    println("size " + cvelist.size)
    println("writing complete.")
    val writer = new BufferedWriter(new FileWriter("cvelist.csv"))
    for (e <- cvelist){
      val changeTComma = e.replaceAll("/", ",")
      val printSt = changeTComma.substring(changeTComma.indexOf(",")+1, changeTComma.length)
      writer.write(printSt + "\n")
    }
    writer.close()
  }
}