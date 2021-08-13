package org.jetbrains.plugins.ninefix

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import javax.swing.table.TableModel

@Suppress("SENSELESS_COMPARISON", "UnstableApiUsage")
class Helper {

    companion object {

        fun getField(rules: List<List<String>>, field: Int): List<String>{
            val res: MutableList<String> = mutableListOf()
            for(rule in rules){
                res.add(rule[field])
            }
            return res.distinct()
        }

        fun loadRules(path: String, type: String, typeStr: String): List<List<String>>{
            val res: MutableList<List<String>> = mutableListOf()
            val item: MutableList<String> = mutableListOf()
            val inputStream = File(path).inputStream()
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)
            var eventType = parser.eventType
            var text : String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagname = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagname.equals("rule", ignoreCase = true)) {
                            // create a new instance of employee
                            item.clear()
                        }
                    }
                    XmlPullParser.TEXT -> {
                        text = parser.text
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagname.equals("rule", ignoreCase = true)) {
                            if (item[Config.FIELD_type] == type || item[Config.FIELD_type] == typeStr || type == "") {
                                res.add(item.toList())
                            }
                        } else {
                            if (text != null) {
                                item.add(text)
                            }
                        }
                    }
                    else -> {
                    }
                }
                eventType = parser.next()
            }
            return res
        }

        fun saveRules(path: String, model: TableModel) {
            val outputStream = File(path).outputStream()
            outputStream.write("<rules>\n".toByteArray())
            for (countRow in 0 until model.rowCount) {
                if(model.getValueAt(countRow, 0).toString().isEmpty()){
                    continue
                }
                outputStream.write("\t<rule>\n".toByteArray())
                for (countCol in 0 until model.columnCount) {
                    outputStream.write(("\t\t<"+Config.FIELD_NAMES[countCol]+">").toByteArray())
                    outputStream.write(model.getValueAt(countRow, countCol).toString().trim().toByteArray())
                    outputStream.write(("</"+Config.FIELD_NAMES[countCol]+">\n").toByteArray())
                }
                outputStream.write("\t</rule>\n".toByteArray())
            }
            outputStream.write("</rules>".toByteArray())
        }

        fun convertListListToArrayArray(dataList: List<List<String>>): Array<Array<String>?> {
            val res : Array<Array<String>?> = arrayOfNulls(dataList.size)

            var i = 0
            while (i < dataList.size){
                res[i] = dataList[i].toTypedArray()
                i++
            }
            return res
        }
    }
}