import com.github.tototoshi.csv.CSVReader
import java.io.File

class CargadorCSV {

  private def inferirTipo(valor: String): Any = {
    val t = valor.trim
    t.toIntOption match {
      case Some(i) => i
      case None => t.toDoubleOption match {
        case Some(d) if d.isValidInt => d.toInt
        case Some(d) => d
        case None => valor
      }
    }
  }

  private def esVacio(v: Any): Boolean = v match {
    case s: String => s.trim.isEmpty
    case _ => false
  }

  def cargarCSV(archivo: File): Map[String, List[Any]] = {
    val reader = CSVReader.open(archivo)
    val filas = reader.allWithHeaders()
    reader.close()

    if (filas.isEmpty) return Map.empty

    val columnas = filas.head.keys.toList.tail
    columnas.map { col =>
      val valores = filas.map(fila => inferirTipo(fila(col)))
      val noVacios = valores.filterNot(esVacio)
      val reemplazo: Any =
        if (noVacios.nonEmpty && noVacios.forall(_.isInstanceOf[Int])) 0
        else if (noVacios.nonEmpty && noVacios.forall(v => v.isInstanceOf[Int] || v.isInstanceOf[Double])) Double.NaN
        else ""
      col -> valores.map(v => if (esVacio(v)) reemplazo else v)
    }.toMap
  }
}