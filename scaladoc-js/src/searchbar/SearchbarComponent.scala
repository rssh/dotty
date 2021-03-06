package dotty.tools.scaladoc

import org.scalajs.dom._
import org.scalajs.dom.html.Input

class SearchbarComponent(val callback: (String) => List[PageEntry]):
  val resultsChunkSize = 100
  extension (p: PageEntry)
    def toHTML =
      val wrapper = document.createElement("div").asInstanceOf[html.Div]
      wrapper.classList.add("scaladoc-searchbar-result")
      wrapper.classList.add("monospace")

      val resultA = document.createElement("a").asInstanceOf[html.Anchor]
      resultA.href = Globals.pathToRoot + p.location
      resultA.text = s"${p.fullName}"

      val location = document.createElement("span")
      location.classList.add("pull-right")
      location.classList.add("scaladoc-searchbar-location")
      location.textContent = p.description

      wrapper.appendChild(resultA)
      wrapper.appendChild(location)
      wrapper

  def handleNewQuery(query: String) =
    val result = callback(query).map(_.toHTML)
    resultsDiv.scrollTop = 0
    while (resultsDiv.hasChildNodes()) resultsDiv.removeChild(resultsDiv.lastChild)
    val fragment = document.createDocumentFragment()
    result.take(resultsChunkSize).foreach(fragment.appendChild)
    resultsDiv.appendChild(fragment)
    def loadMoreResults(result: List[raw.HTMLElement]): Unit = {
      resultsDiv.onscroll = (event: Event) => {
          if (resultsDiv.scrollHeight - resultsDiv.scrollTop == resultsDiv.clientHeight)
          {
              val fragment = document.createDocumentFragment()
              result.take(resultsChunkSize).foreach(fragment.appendChild)
              resultsDiv.appendChild(fragment)
              loadMoreResults(result.drop(resultsChunkSize))
          }
      }
    }
    loadMoreResults(result.drop(resultsChunkSize))

  private val searchIcon: html.Div =
    val span = document.createElement("span").asInstanceOf[html.Span]
    span.innerHTML = """<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20"><path d="M19.64 18.36l-6.24-6.24a7.52 7.52 0 10-1.28 1.28l6.24 6.24zM7.5 13.4a5.9 5.9 0 115.9-5.9 5.91 5.91 0 01-5.9 5.9z"></path></svg>"""
    span.id = "scaladoc-search"
    span.onclick = (event: Event) =>
      if (document.body.contains(rootDiv)) {
        document.body.removeChild(rootDiv)
      }
      else document.body.appendChild(rootDiv)

    val element = createNestingDiv("search-content")(
      createNestingDiv("search-container")(
        createNestingDiv("search")(
          span
        )
      )
    )
    document.getElementById("scaladoc-searchBar").appendChild(element)
    element


  private val input: html.Input =
    val element = document.createElement("input").asInstanceOf[html.Input]
    element.id = "scaladoc-searchbar-input"
    element.addEventListener("input", (e) => handleNewQuery(e.target.asInstanceOf[html.Input].value))
    element

  private val resultsDiv: html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.id = "scaladoc-searchbar-results"
    element

  private val rootHiddenClasses = "hidden"
  private val rootShowClasses   = ""

  private def createNestingDiv(className: String)(innerElement: html.Element): html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.className = className
    element.appendChild(innerElement)
    element

  private val rootDiv: html.Div =
    val element = document.createElement("div").asInstanceOf[html.Div]
    element.addEventListener("mousedown", (e: Event) => e.stopPropagation())
    searchIcon.addEventListener("mousedown", (e: Event) => e.stopPropagation())
    document.body.addEventListener("mousedown", (e: Event) =>
      if (document.body.contains(element)) {
        document.body.removeChild(element)
      }
    )
    element.id = "scaladoc-searchbar"
    element.appendChild(input)
    element.appendChild(resultsDiv)
    element

  handleNewQuery("")
