
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import scala.annotation.tailrec
object Main {
  var datos: Map[String, List[Any]] = Map.empty
  def fase1(): Unit={
    print("Introduzca el umbral: ")
    val entrada_umbral: Int = scala.io.StdIn.readLine().trim.toInt
    print("¿Es retraso o adelanto? (r/a): ")
    val umbral:Int = if (scala.io.StdIn.readLine().trim.toLowerCase == "r") entrada_umbral else -entrada_umbral
    val retrasos = datos("DEP_DELAY").asInstanceOf[List[Int]]
    val ids = datos("id").asInstanceOf[List[Int]]
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
      case _: Int    => buscar_valor(l.asInstanceOf[List[Int]],    buscarMax, l.head.asInstanceOf[Int])
      case _: Double => buscar_valor(l.asInstanceOf[List[Double]], buscarMax, l.head.asInstanceOf[Double])
    }
    //Si es convertible a Double, pero tiene .0, lo mostramos como Int para mejor legibilidad
    println(s"${if (buscarMax) "Max()" else "Min()"} $nombre_columna = ${valor_extremo match{
      case d: Double if d.isWhole => d.toInt
      case other => other
    }} minutos")
  }
  def fase4():Unit={}
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

