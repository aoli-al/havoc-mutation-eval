from dataproperty import DataProperty
from pytablewriter import LatexTableWriter
import typepy

class TableWriter(LatexTableWriter):
    def _LatexTableWriter__is_requre_verbatim(self, value_dp: DataProperty) -> bool:
        return False

