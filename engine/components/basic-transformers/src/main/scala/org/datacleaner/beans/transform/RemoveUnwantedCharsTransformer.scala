package org.datacleaner.beans.transform

import org.datacleaner.beans.api.Transformer
import org.datacleaner.beans.api.TransformerBean
import org.datacleaner.beans.api.Description
import org.datacleaner.beans.api.Categorized
import org.datacleaner.beans.categories.StringManipulationCategory
import org.datacleaner.data.InputColumn
import org.datacleaner.beans.api.Configured
import org.datacleaner.beans.api.OutputColumns
import org.datacleaner.data.InputRow
import org.datacleaner.util.CharIterator

@TransformerBean("Remove unwanted characters")
@Description("Removes characters from strings that are not wanted. Use it to cleanse codes and identifiers that may have additional dashes, punctuations, unwanted letters etc.")
@Categorized(Array(classOf[StringManipulationCategory]))
class RemoveUnwantedCharsTransformer(col: InputColumn[String]) extends Transformer[String] {

  @Configured
  var column: InputColumn[String] = col;

  @Configured(order = 1)
  var removeWhitespaces = true;

  @Configured(order = 2)
  var removeLetters = true;

  @Configured(order = 3)
  var removeDigits = false;

  @Configured(order = 4)
  @Description("Remove additional signs, such as dashes, punctiations, slashes and more?")
  var removeSigns = true;

  def this() = this(null)

  def getOutputColumns() = new OutputColumns(column.getName() + " (cleansed)");

  def transform(row: InputRow): Array[String] = {
    val value = row.getValue(column);
    return transform(value);
  }

  def transform(value: String): Array[String] = {
    if (value == null) {
      return Array(null);
    }
    val it = new CharIterator(value)
    while (it.hasNext()) {
      val c = it.next();
      if (it.isWhitespace()) {
        if (removeWhitespaces) {
          it.remove();
        }
      } else if (it.isLetter()) {
        if (removeLetters) {
          it.remove();
        }
      } else if (it.isDigit()) {
        if (removeDigits) {
          it.remove();
        }
      } else if (removeSigns) {
        it.remove();
      }
    }
    return Array(it.toString());
  }
}
