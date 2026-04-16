
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
object Main {
  def main(args: Array[String]): Unit = {
    val cargador = new CargadorCSV
    println("Cargando datos del CSV...")
    val datos: Map[String, List[Any]] = cargador.cargarCSV(new java.io.File("Airline_dataset.csv"))
    datos.foreach { case (key, value) =>
      println(s"$key: ${value.take(5)}")
    }
    val retrasos: List[Int] = datos("DEP_DELAY").map(_.asInstanceOf[Int])
    println(s"Retrasos: ${retrasos.take(5)}")
  }
}

