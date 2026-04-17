import com.github.tototoshi.csv.CSVReader
import java.io.File

class CargadorCSV {

  def cargarCSV(archivo: File): Map[String, List[Any]] = {
    val reader = CSVReader.open(archivo)
    val filas = reader.allWithHeaders()
    reader.close()

    if (filas.isEmpty) return Map.empty

    val columnas = filas.head.keys.toList
    columnas.map { col =>
      val crudos = filas.map(_(col).trim)
      val noVacios = crudos.filter(_.nonEmpty)

      val (tipoColumna, reemplazo: Any) =
        if (noVacios.nonEmpty && noVacios.forall(s => s.toDoubleOption.exists(_.isValidInt))) ("Int", 0)
        else if (noVacios.nonEmpty && noVacios.forall(_.toDoubleOption.isDefined)) ("Double", Double.NaN)
        else ("String", "")

      val valores: List[Any] = crudos.map { s =>
        if (s.isEmpty) reemplazo
        else tipoColumna match {
          case "Int"    => s.toDouble.toInt
          case "Double" => s.toDouble
          case _        => s
        }
      }

      (if (col.isEmpty) "id" else col) -> valores
    }.toMap
  }
}