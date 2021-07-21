@file:Suppress("UnstableApiUsage")

package org.jetbrains.plugins.ninefix.inspection
import com.android.tools.idea.lint.common.AndroidLintInspectionBase
import com.android.tools.idea.lint.common.LintIdeIssueRegistry
import com.android.tools.lint.detector.api.*
import org.jetbrains.plugins.ninefix.Config
import org.jetbrains.plugins.ninefix.Helper
import org.w3c.dom.Attr
import java.util.*

val XMLAttributeIssue: Issue = Issue
    .create(
        id = "XMLResourceIssueEntry",
        briefDescription = "",
        explanation = "",
        category = Category.CORRECTNESS,
        priority = 0,
        severity = Severity.WARNING,
        androidSpecific = true,
        implementation = Implementation(
            XMLAttributeIssueDetector::class.java,
            EnumSet.of(Scope.RESOURCE_FILE)
        )
    )

class XMLResourceInspection : AndroidLintInspectionBase("Xml Resource Inspection", XMLAttributeIssue) {

    init {
        val registry = LintIdeIssueRegistry()
        val myIssue = registry.getIssue(XMLAttributeIssue.id)
        if (myIssue == null) {
            val list = registry.issues as MutableList<Issue>
            list.add(XMLAttributeIssue)
        }
    }

    override fun getShortName(): String {
        return "XMLResourceInspection"
    }
}

class XMLAttributeIssueDetector: Detector(), Detector.XmlScanner {
    private val rules = Helper.loadRules(Config.PATH,Config.TYPE_XMLAttribute)

    override fun getApplicableAttributes(): Collection<String> {
        return Helper.getField(rules, Config.FIELD_XMLAttribute)
    }

    override fun visitAttribute(context: XmlContext, attribute: Attr) {
        //context.report(XMLAttributeIssue, context.getValueLocation(attribute), "Key name:"+attribute.name.split(":")[-1])
        for(rule in rules){
            if(attribute.name.split(':').last() == rule[Config.FIELD_XMLAttribute]){
                if(attribute.value == rule[Config.FIELD_XMLAttributeValue]){
                    if(rule[Config.FIELD_needFix].trim() != "1"){
                        context.report(createXMLResourceIssue(rule, this), context.getValueLocation(attribute), rule[Config.FIELD_ID])
                    }
                    else{
                        context.report(createXMLResourceIssue(rule, this), context.getValueLocation(attribute), rule[Config.FIELD_ID], createXMLAttribute(context, attribute, rule))
                    }
                }
            }
        }
    }

    private fun createXMLAttribute(context: XmlContext, attribute: Attr, rule: List<String>): LintFix? {
        return LintFix.create()
            .name(rule[Config.FIELD_fixName])
            .replace()
            .range(context.getValueLocation(attribute))
            .pattern(rule[Config.FIELD_fixOld])
            .with(rule[Config.FIELD_fixNew])
            .autoFix()
            .reformat(true)
            .build()
    }

    private fun createXMLResourceIssue(rule: List<String>, detector: Detector): Issue {
        return Issue.create(
            id = rule[Config.FIELD_ID],
            briefDescription = rule[Config.FIELD_briefDescription],
            explanation = rule[Config.FIELD_explanation],
            category = Category.CORRECTNESS,
            priority = rule[Config.FIELD_priority].toInt(),
            severity = Severity.WARNING,
            androidSpecific = true,
            implementation = Implementation(
                detector::class.java,
                EnumSet.of(Scope.RESOURCE_FILE)
            )
        )
    }
}