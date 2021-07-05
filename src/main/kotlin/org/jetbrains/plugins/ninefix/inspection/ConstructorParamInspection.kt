@file:Suppress("UnstableApiUsage")

package org.jetbrains.plugins.ninefix.inspection
import com.android.tools.idea.lint.common.AndroidLintInspectionBase
import com.android.tools.idea.lint.common.LintIdeIssueRegistry
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.ninefix.Config
import org.jetbrains.plugins.ninefix.Helper
import org.jetbrains.uast.UCallExpression
import java.util.*

val ConstructorParamIssue: Issue = Issue
        .create(
                id = "ConstructorParamIssueEntry",
                briefDescription = "empty",
                explanation = "empty",
                category = Category.SECURITY,
                priority = 0,
                severity = Severity.WARNING,
                androidSpecific = true,
                implementation = Implementation(
                        ConstructorParamIssueDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE)
                )
        )

class ConstructorParamInspection : AndroidLintInspectionBase("Constructor Parameter Inspection", ConstructorParamIssue) {

    init {
        val registry = LintIdeIssueRegistry()
        val myIssue = registry.getIssue(ConstructorParamIssue.id)
        if (myIssue == null) {
            val list = registry.issues as MutableList<Issue>
            list.add(ConstructorParamIssue)
        }
    }

    override fun getShortName(): String {
        return "ConstructorParamInspection"
    }
}

class ConstructorParamIssueDetector : Detector(), Detector.UastScanner {
    private val rules = Helper.loadRules(Config.PATH, Config.TYPE_CONSTRUCTOR_PARAM)

    override fun getApplicableConstructorTypes(): List<String> {
        return Helper.getField(rules, Config.FIELD_className)
    }
    override fun visitConstructor(context: JavaContext, node: UCallExpression, constructor: PsiMethod) {
        for (rule in rules) {
            if(node.valueArgumentCount == 0)
                continue
            if (rule[Config.FIELD_className].split('.').last() == constructor.name){

                val param = node.getArgumentForParameter(rule[Config.FIELD_paramIndex].toInt())!!.sourcePsi!!.text

                val reg = Regex(rule[Config.FIELD_paramPattern])
                if (param.matches(reg)) {
                    if(rule[Config.FIELD_needFix].trim() != "1"){
                        context.report(createConstructureParamIssue(rule, this), node, context.getLocation(node), rule[Config.FIELD_briefDescription])
                    }
                    else{
                        val oldText = getParamTextFromConstructor(node, rule[Config.FIELD_paramIndex].toInt())
                        context.report(createConstructureParamIssue(rule, this), node, context.getLocation(node), rule[Config.FIELD_briefDescription], createConstructorParamIssueQuickFix(context, node, rule, oldText))
                    }
                }
            }
        }
    }

    private fun getParamTextFromConstructor(node: UCallExpression, index: Int): String {
        return node.getArgumentForParameter(index)!!.javaPsi!!.text
    }

    private fun createConstructureParamIssue(rule: List<String>, detector: Detector): Issue {
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
                        EnumSet.of(Scope.JAVA_FILE)
                )
        )
    }
    private fun createConstructorParamIssueQuickFix(context: JavaContext, node: UCallExpression, rule: List<String>, oldText: String) : LintFix{
        return LintFix.create()
                .name(rule[Config.FIELD_fixName])
                .replace()
                .range(context.getRangeLocation(node.uastParent!!, 0, node, 0))
                .pattern(rule[Config.FIELD_fixOld])
                .with(rule[Config.FIELD_fixNew].replace(Config.templateString, oldText).replace("\\n", "\n"))
                .autoFix()
                .reformat(true)
                .build()
    }
}