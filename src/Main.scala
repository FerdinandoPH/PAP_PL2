
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import java.io.ByteArrayOutputStream
import scala.annotation.tailrec
object Main {
  var datos: Map[String, List[Any]] = Map.empty
  var nombre_usuario: String = ""
  def subir_datos(fase:String, entrada:String, salida:String): Unit = {
    if (nombre_usuario.isEmpty){
      print("Introduzca su nombre de usuario (se guardará durante la sesión): ")
      nombre_usuario = scala.io.StdIn.readLine().trim
    }
    val fecha_y_hora = java.time.LocalDateTime.now().withNano(0).toString
    def escapar(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    val entrada_json = s"""{"nombre_usuario": "${escapar(nombre_usuario)}", "nombre_fase": "${escapar(fase)}", "fecha": "$fecha_y_hora", "entrada": "${escapar(entrada)}", "salida": "${escapar(salida)}"}"""
    try{
      val cliente = java.net.http.HttpClient.newHttpClient()
      val cuerpo = java.net.http.HttpRequest.BodyPublishers.ofString(entrada_json)
      val req = java.net.http.HttpRequest.newBuilder(java.net.URI.create("http://127.0.0.1:5000/api/registrar_entrada"))
        .POST(cuerpo)
        .header("Content-Type", "application/json")
        .build()
      val res = cliente.send(req, java.net.http.HttpResponse.BodyHandlers.ofString())
      if (res.statusCode() == 201) println("Datos subidos al servidor.")
      else println(s"Error al subir los datos al servidor: ${res.statusCode()} - ${res.body()}")
    }catch{
      case e: Exception => println(s"Error al conectar con el servidor, comprueba que esté encendido: ${e.toString}")
    }

  }
  def fase1(): Unit={
    print("Introduzca el umbral: ")
    val entrada_umbral: Int = scala.io.StdIn.readLine().trim.toInt
    print("¿Es retraso o adelanto? (r/a): ")
    val umbral:Int = if (scala.io.StdIn.readLine().trim.toLowerCase == "r") entrada_umbral else -entrada_umbral
    val retrasos = datos("DEP_DELAY").asInstanceOf[List[Int]]
    val ids = datos("id").asInstanceOf[List[Int]]
    val buffer = new ByteArrayOutputStream()
    Console.withOut(buffer){
      @tailrec
      def imprimir_retrasos(retrasos: List[Int], umbral: Int, ids:List[Int]): Unit = {
        retrasos match{
          case Nil => ()
          case head :: tail =>
            if (head > umbral) {
              if(umbral >= 0) println(s"#${ids.head}: Retraso de $head minutos")
              else println(s"#${ids.head}: Adelanto de ${-head} minutos")
            }
            imprimir_retrasos(tail, umbral, ids.tail)
        }
      }
      imprimir_retrasos(retrasos, umbral, ids)
    }
    val salida = buffer.toString("UTF-8")
    println(salida)
    print("¿Desea subir estos datos al servidor? (s/n): ")
    if (scala.io.StdIn.readLine().trim.toLowerCase == "s") subir_datos("Fase 1", s"Umbral: $umbral minutos", salida)
    println("--------------------")
  }
  def fase2():Unit={
    print("Introduzca el umbral: ")
    val entrada_umbral: Int = scala.io.StdIn.readLine().trim.toInt
    print("¿Es retraso o adelanto? (r/a): ")
    val umbral:Int = if (scala.io.StdIn.readLine().trim.toLowerCase == "r") entrada_umbral else -entrada_umbral
    val retrasos = datos("ARR_DELAY").asInstanceOf[List[Int]]
    val matriculas = datos("TAIL_NUM").asInstanceOf[List[String]]
    val ids = datos("id").asInstanceOf[List[Int]]

    val buffer = new ByteArrayOutputStream()
    Console.withOut(buffer){
      @tailrec
      def imprimir_retrasos(retrasos: List[Int], umbral: Int, matriculas: List[String], ids:List[Int]): Unit = {
        retrasos match {
          case Nil => ()
          case head :: tail =>
            if (head > umbral) {
              if (umbral >= 0) println(s"#${ids.head} | Matricula: ${matriculas.head} | Retraso (llegada) de $head minutos")
              else println(s"#${ids.head} | Matricula: ${matriculas.head} | Adelanto (llegada) de ${-head} minutos")
            }
            imprimir_retrasos(tail, umbral, matriculas.tail, ids.tail)
        }
      }
      imprimir_retrasos(retrasos, umbral, matriculas, ids)
    }
    val salida = buffer.toString("UTF-8")
    println(salida)
    print("¿Desea subir estos datos al servidor? (s/n): ")
    if (scala.io.StdIn.readLine().trim.toLowerCase == "s") subir_datos("Fase 2", s"Umbral: $umbral minutos", salida)
    println("--------------------")
  }
  def fase3():Unit={
    println("¿Sobre qué columna desea buscar?")
    println("1. DEP_DELAY")
    println("2. ARR_DELAY")
    println("3. WEATHER_DELAY")
    print("Seleccione una opción: ")
    val nombre_columna:String = scala.io.StdIn.readLine().trim match {
      case "1" => "DEP_DELAY"
      case "2" => "ARR_DELAY"
      case "3" => "WEATHER_DELAY"
      case _ => println("Opción no válida, se usará WEATHER_DELAY por defecto."); "WEATHER_DELAY"
    }
    print("¿Buscar máximo o mínimo? (max/min): ")
    val buscarMax:Boolean = scala.io.StdIn.readLine().trim.toLowerCase match {
      case "max" => true
      case "min" => false
      case _ => println("Opción no válida, se buscará el máximo por defecto."); true
    }

    val buffer = new ByteArrayOutputStream()
    Console.withOut(buffer) {
      val l = datos(nombre_columna)

      @tailrec
      def buscar_valor[T: Numeric](l: List[T], buscarMax: Boolean, candidato: T): T = {
        val num = implicitly[Numeric[T]]
        import num._
        l match {
          case Nil => candidato
          case head :: tail =>
            if (buscarMax) {
              if (head > candidato) buscar_valor(tail, buscarMax, head)
              else buscar_valor(tail, buscarMax, candidato)
            } else {
              if (head < candidato) buscar_valor(tail, buscarMax, head)
              else buscar_valor(tail, buscarMax, candidato)
            }
        }
      }

      val valor_extremo = l.head match {
        case _: Int => buscar_valor(l.asInstanceOf[List[Int]], buscarMax, l.head.asInstanceOf[Int])
        case _: Double => buscar_valor(l.asInstanceOf[List[Double]], buscarMax, l.head.asInstanceOf[Double])
      }
      //Si es convertible a Double, pero tiene .0, lo mostramos como Int para mejor legibilidad
      println(s"${if (buscarMax) "Max()" else "Min()"} $nombre_columna = ${
        valor_extremo match {
          case d: Double if d.isWhole => d.toInt
          case other => other
        }
      } minutos")
    }
    val salida = buffer.toString("UTF-8")
    println(salida)
    print("¿Desea subir estos datos al servidor? (s/n): ")
    if (scala.io.StdIn.readLine().trim.toLowerCase == "s") subir_datos("Fase 3", s"Columna: $nombre_columna\nBuscar: ${if(buscarMax) "Máximo" else "Mínimo"}", salida)
    println("--------------------")
  }
  def fase4():Unit={
    println("¿Sobre qué columna desea hacer el histograma?")
    println("1. ORIGIN_AIRPORT")
    println("2. DEST_AIRPORT")
    print("Seleccione una opción: ")
    val nombre_columna:String = scala.io.StdIn.readLine().trim match {
      case "1" => "ORIGIN_AIRPORT"
      case "2" => "DEST_AIRPORT"
      case _ => println("Opción no válida, se usará ORIGIN_AIRPORT por defecto."); "ORIGIN_AIRPORT"
    }
    print("¿Cuál es el umbral mínimo de frecuencia para mostrar en el histograma? ")
    val umbral: Int = scala.io.StdIn.readLine().trim.toInt

    val buffer = new ByteArrayOutputStream()
    Console.withOut(buffer) {
      val l = datos(nombre_columna).asInstanceOf[List[String]]

      @tailrec
      def reverso(l: List[(String, Int)], acc: List[(String, Int)] = List.empty): List[(String, Int)] = {
        l match {
          case Nil => acc
          case head :: tail => reverso(tail, head :: acc)
        }
      }


      def concat_listas(l1: List[(String, Int)], l2: List[(String, Int)]): List[(String, Int)] = {
        @tailrec
        def concat_aux(l: List[(String, Int)], acc: List[(String, Int)]): List[(String, Int)] = {
          l match {
            case Nil => reverso(acc)
            case head :: tail => concat_aux(tail, head :: acc)
          }
        }

        concat_aux(l2, reverso(l1))
      }

      @tailrec
      def longitud(l: List[Any], acc: Int = 0): Int = {
        l match {
          case Nil => acc
          case _ :: tail => longitud(tail, acc + 1)
        }
      }

      def generar_histograma(l: List[String]): List[(String, Int)] = {
        @tailrec
        def generar_histograma_aux(l: List[String], hist: List[(String, Int)]): List[(String, Int)] = {
          l match {
            case Nil => hist
            case head :: tail =>
              @tailrec
              def actualizar_histograma(hist: List[(String, Int)], valor: String, acc: List[(String, Int)] = List.empty): List[(String, Int)] = {
                hist match {
                  case Nil => (valor, 1) :: acc
                  case (c, v) :: tail =>
                    if (c == valor) concat_listas((c, v + 1) :: tail, acc)
                    else actualizar_histograma(tail, valor, (c, v) :: acc)
                }
              }

              generar_histograma_aux(tail, actualizar_histograma(hist, head))

          }
        }

        generar_histograma_aux(l, List.empty)
      }

      @tailrec
      def ordenar_y_filtrar_histograma(hist: List[(String, Int)], umbral: Int, acc: List[(String, Int)] = List.empty): List[(String, Int)] = {
        hist match {
          case Nil => acc
          case head :: tail =>
            @tailrec
            def insertar_ordenado(l: List[(String, Int)], valor: (String, Int), acc: List[(String, Int)] = List.empty): List[(String, Int)] = {
              l match {
                case Nil => reverso(valor :: acc)
                case head :: tail =>
                  if (head._2 >= valor._2) insertar_ordenado(tail, valor, head :: acc)
                  else concat_listas(reverso(acc), valor :: head :: tail)
              }
            }

            if (head._2 >= umbral) ordenar_y_filtrar_histograma(tail, umbral, insertar_ordenado(acc, head))
            else ordenar_y_filtrar_histograma(tail, umbral, acc)
        }
      }

      val histograma_desordenado = generar_histograma(l)
      println(s"Histograma de aeropuertos con más ${if (nombre_columna == "ORIGIN_AIRPORT") "salidas" else "llegadas"}:")
      println(s"Número de aeropuertos distintos: ${longitud(histograma_desordenado)}")
      val histograma = ordenar_y_filtrar_histograma(histograma_desordenado, umbral)

      @tailrec
      def imprimir_histograma(hist: List[(String, Int)], max: Int, umbral: Int, num: Int = 0): Unit = {
        hist match {
          case Nil => println(s"Aeropuertos mostrados (con más de $umbral vuelos): $num")
          case head :: tail =>
            println(f"${head._1} | ${"#" * (head._2 * 15 / max)} (${head._2})")
            imprimir_histograma(tail, max, umbral, num + 1)
        }
      }

      if (histograma.isEmpty) println("Ningún aeropuerto supera el umbral indicado.")
      else imprimir_histograma(histograma, histograma.head._2, umbral)
    }
    val salida = buffer.toString("UTF-8")
    println(salida)
    print("¿Desea subir estos datos al servidor? (s/n): ")
    if (scala.io.StdIn.readLine().trim.toLowerCase == "s") subir_datos("Fase 4", s"Columna: $nombre_columna\nUmbral: $umbral", salida)
    println("--------------------")
  }
  def main(args: Array[String]): Unit = {
    val cargador = new CargadorCSV
    var ruta: String = ""
    println("Bienvenido a la PL2 de Fernando y Miguel Ángel")
    print("Introduzca la ruta del CSV (por defecto se usará 'Airline_dataset.csv'): ")
    ruta = ""//scala.io.StdIn.readLine().trim
    if (ruta.isEmpty) {
      ruta = "Airline_dataset.csv"
    }
    println("Cargando CSV...")
    datos = cargador.cargarCSV(new java.io.File(ruta))
    println("CSV cargado exitosamente")
    var continuar = true
    while (continuar) {
      println("Opciones disponibles:")
      println("1. Retraso en despegues")
      println("2. Retraso en aterrizajes")
      println("3. Reducción de retrasos")
      println("4. Histograma de retrasos")
      println("5. Salir")
      print("Seleccione una opción: ")
      scala.io.StdIn.readLine().trim match {
        case "1" => fase1()
        case "2" => fase2()
        case "3" => fase3()
        case "4" => fase4()
        case "5" => continuar = false
        case _ => println("Opción no válida, inténtelo de nuevo.")
      }

    }

  }
}

