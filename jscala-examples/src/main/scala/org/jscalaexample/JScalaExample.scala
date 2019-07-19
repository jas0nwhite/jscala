package org.jscalaexample

import org.jscala._
import scala.util.Random
import org.scalajs.dom._

case class User(name: String, id: Int)

object JScalaExample {
  def domManipulations() {
    val html = javascript {
      val node = document.getElementById("myList2").lastChild
      document.getElementById("myList1").appendChild(node)
      val buttons = document.getElementsByTagName("link")
      for (idx <- 0 until buttons.length) {
        console.log(buttons.item(idx).attributes)
      }
    }.asString
    println(html)
  }

  def hello() {
    val ast = javascript {
      def main(args: Array[String]) {
        val language = if (args.length == 0) "EN" else args(0)
        val res = language match {
          case "EN" => "Hello!"
          case "FR" => "Salut!"
          case "IT" => "Ciao!"
          case _    => s"Sorry, I can't greet you in $language yet"
        }
        print(res)
      }
    }
    println(ast.asString)
  }

  def browserStuff() {
    val js = javascript {
      window.location.href = "http://jscala.org"
      window.open("https://github.com")
      window.history.back()
    }
    println(js.asString)
  }

  def shortExample() {
    val scalaValue = "https://github.com/nau/jscala"

    def rand() = Random.nextInt(5).toJs

    val $ = new JsDynamic {}

    val js = javascript {
      window.setTimeout(() => {
        val links = Array("https://github.com/nau/scala")
        include("var raw = 'JavaScript'")
        for (link <- links) {
          $("#id").append(s"<p>$link</p>")
        }
        for (i <- 0 to rand().as[Int]) print(inject(scalaValue))
      }, 1000)
    }
    println(js.asString)
  }

  def complexExample() {
    val js = javascript {
      window.setTimeout(() => {
        val r = new RegExp("d.*", "g")
        class Point(val x: Int, val y: Int)
        val point = new Point(1, 2)

        def func(i: String) = r.exec(i)

        val list = document.getElementById("myList2")
        val map = collection.mutable.Map[String, String]()
        if (typeof(map) == "string") {
          for (idx <- 0 until list.attributes.length) {
            val attr = list.attributes.item(idx)
            map(attr.name) = func(attr.textContent)
          }
        } else {
          val obj = new {
            val field = 1

            def func2(i: Int) = "string"
          }
          val links = Array("https://github.com/nau/scala")
          for (link <- links) {
            include("var raw = 'JavaScript'")
            console.log(link + obj.func2(obj.field) + point.x)
          }
          window.location.href = links(0).replace("scala", "jscala")
        }
      }, 1000)
    }
    println(js.asString)
  }

  def ajaxExample() {
    val $ = new JsDynamic {}

    def ajaxCall(pageId: Int) = javascript {
      $.get("ajax/" + pageId, (data: String) => $("#someId").html(data))
    }

    def genAjaxCall(pageId: Int) = javascript {
      ajaxCall(pageId)
    }

    println(genAjaxCall(123).asString)
  }


  import play.api.libs.json._

  def readmeExample() {
    implicit val userJson = Json.format[User]
    @Javascript class Greeter {
      def hello(u: User) {
        print(s"Hello, ${u.name} \n")
      }
    }
    // Run on JVM
    val u1 = User("Alex", 1)
    val greeter = new Greeter()
    greeter.hello(u1) // prints "Hello, Alex"
    val json = Json.stringify(Json.toJson(u1))
    val main = javascript {
      val u = User(id = 2, name = "nau")
      // read User from json string generated above
      val u1Json = eval(s"(${include(json)})").as[User]
      val t = new Greeter()
      t.hello(u)
      t.hello(u1Json)
    }
    // join classes definitions with main code
    val js = Greeter.jscala.javascript ++ main
    println(js.asString) // prints resulting JavaScript
    // js.eval() // run using Rhino /* ERROR: ReferenceError: "json" is not defined in <eval> at line number 11 */
  }


  def astManipulation() {
    val vardef = JsVarDef("test", "Test".toJs).block
    val print = JsCall(JsIdent("print"), JsIdent("test") :: Nil)
    val ast = vardef ++ print
    ast.eval()
    println(ast.asString)
    /* prints
    Test
    {
     var test = "Test";
     print(test);
    }
     */
  }


  def sc() = println(
    {
      val molecules = List("DA", "NE", "5HT")
      val editDetailId = "edit-details"


      // let's try jscala.org DSL
      import org.jscala._
      import org.scalajs.dom._
      import scala.scalajs.js
      import play.api.libs.json._
      import org.scalajs.jquery._

      // implicit val arrJson: OFormat[Array[_]] = Json.format[Array[_]]
      val $ = new JsDynamic {}

      // for DataTables...
      class `$.fn.datatable.Editor`(props: Object) extends JsDynamic

      javascript {
        var editor = new JsDynamic {}

        // instantiate and configure editor
        $(document).ready(() => {
          editor = new `$.fn.datatable.Editor`(Map(

            "table" -> inject("#" + editDetailId),

            // custom ajax handler
            /*"ajax" -> ajaxHandler,*/

            // build list of fields from list of molecules
            "fields" -> Array(
              Map("label" -> "index", "name" -> "index"),
              inject(molecules map (m => Map("label" -> m, "name" -> m)))
            ),

            // enable inline editing
            "formOptions" -> Map(
              "inline" -> Map(
                "onBlur" -> "submit",
                "submit" -> "allIfChanged"
              )
            )
          ))

          // highlight changed cells
          editor.on(
            "submitSuccess",
            (e: JsDynamic, json: JsDynamic, data: JsDynamic, action: String) => {
              $("#" + data.DT_RowId)
              .find("td:eq(" + editor.s.modifier.column + ")")
              .addClass("info")
              ()
            })

          // configure datatable
          $(inject("#" + editDetailId)).DataTable(Map(
            "dom" -> "Bfrtip",

            // build list of columns from list of molecules
            "columns" -> Array(
              Map("data" -> "index", "className" -> "text-right"),
              inject(molecules map (m => Map("data" -> m, "className" -> "text-right")))
            ),

            // set up KeyTable extension for tab-to-next-field
            "keys" -> Map(
              "columns" -> ":not(:first-child)",
              "keys" -> Array(9),
              "editor" -> editor,
              "editOnFocue" -> true
            ),

            // set up Select extension
            "select" -> Map(
              "style" -> "os",
              "selector" -> "td:first-child"
            ),

            "stateSave" -> true,
            "processing" -> false,

            // set the editor object for the edit buttons
            "buttons" -> Array(
              Map("extend" -> "create", "editor" -> editor),
              Map("extend" -> "edit", "editor" -> editor),
              Map("extend" -> "remove", "editor" -> editor)
            ),

            // handle FOUC
            "fnInitComplete" -> ((oSettings: JsDynamic, json: JsDynamic) => {
              $("#fouc").css(Map("display" -> "block"))
              ()
            })
          ))

          ()
        })
      }
    }.asString
  )

  def main(args: Array[String]) {
    domManipulations()
    hello()
    browserStuff()
    shortExample()
    complexExample()
    ajaxExample()
    readmeExample()
    astManipulation()
    sc()
  }
}

