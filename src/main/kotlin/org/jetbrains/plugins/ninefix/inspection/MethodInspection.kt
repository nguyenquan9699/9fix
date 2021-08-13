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

val MethodIssue: Issue = Issue
        .create(
                id = "MethodIssueEntry",
                briefDescription = "",
                explanation = "",
                priority = 0,
                severity = Severity.WARNING,
                androidSpecific = true,
                implementation = Implementation(
                        MethodIssueDetector::class.java,
                        EnumSet.of(Scope.JAVA_FILE)
                )
        )

class MethodInspection : AndroidLintInspectionBase("Method Inspection", MethodIssue) {

    init {
        val registry = LintIdeIssueRegistry()
        val myIssue = registry.getIssue(MethodIssue.id)
        if (myIssue == null) {
            val list = registry.issues as MutableList<Issue>
            list.add(MethodIssue)
        }
    }

    override fun getShortName(): String {
        return "MethodInspection"
    }
}

class MethodIssueDetector : Detector(), Detector.UastScanner {
    private val rules = Helper.loadRules(Config.PATH, Config.TYPE_METHOD, Config.TYPE_METHOD_STR)

    override fun getApplicableMethodNames(): List<String> {
        return Helper.getField(rules, Config.FIELD_meThodName)
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        for (rule in rules){
            val isInResources = context.evaluator.isMemberInClass(method, rule[Config.FIELD_className])
            if (rule[Config.FIELD_meThodName] == node.methodName && isInResources) {
                if(rule[Config.FIELD_needFix].trim() != "1"){
                    context.report(createMethodIssue(rule, this), node, context.getLocation(node), rule[Config.FIELD_briefDescription])
                }
                else{
                    context.report(createMethodIssue(rule, this), node, context.getLocation(node), rule[Config.FIELD_briefDescription], createMethodIssueQuickFix(context, node, rule))
                }
            }
        }
    }
    private fun createMethodIssue(rule: List<String>, detector: Detector): Issue {
        return Issue.create(
                id = rule[Config.FIELD_ID],
                briefDescription = rule[Config.FIELD_briefDescription],
                explanation = rule[Config.FIELD_explanation],
                category = Category.SECURITY,
                priority = rule[Config.FIELD_priority].toInt(),
                severity = Severity.WARNING,
                androidSpecific = true,
                implementation = Implementation(
                        detector::class.java,
                        EnumSet.of(Scope.JAVA_FILE)
                )
        )
    }
    private fun createMethodIssueQuickFix(context: JavaContext, node: UCallExpression, rule: List<String>) : LintFix{
        return LintFix.create()
                .name(rule[Config.FIELD_fixName])
                .replace()
                .range(context.getRangeLocation(node.uastParent!!, 0, node, 0))
                .pattern(rule[Config.FIELD_fixOld])
                .with(rule[Config.FIELD_fixNew].replace("\\n", "\n"))
                .autoFix()
                .reformat(true)
                .build()
    }
}
