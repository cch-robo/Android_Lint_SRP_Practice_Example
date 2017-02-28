package com.cch_lab.java.android.lint.example;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.cch_lab.java.android.lint.example.util.ElementUtil;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiFunctionFlagStructureDetector extends Detector implements Detector.JavaPsiScanner {
    public static final Issue ISSUE = Issue.create(
            "MultiFunctionFlagStructure",
            "boolean フラグ引数によるメソッドの複数機能提供問題",
            "メソッド引数の boolean フラグによる処理切り替えで、フィールド値(状態)を変更しています。\n"
                    +"メソッドは、多重の責務(役割)を提供しています。メソッドの分割を検討してください。",
            Category.CORRECTNESS,
            4,
            Severity.WARNING,
            new Implementation(
                    MultiFunctionFlagStructureDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public MultiFunctionFlagStructureDetector() {
    }

    @Override
    public void beforeCheckProject(Context context) {
        reportBeforeCheckProject(context);
    }

    @Override
    public void afterCheckProject(Context context) {
        reportAfterCheckProject(context);
    }

    @Override
    public void beforeCheckFile(Context context) {
        reportBeforeCheckFile(context);
    }

    @Override
    public void afterCheckFile(Context context) {
        reportAfterCheckFile(context);
    }

    @Override
    public List<Class<? extends PsiElement>> getApplicablePsiTypes() {
        return Arrays.asList(
                PsiClass.class,
                PsiMethod.class,
                PsiIfStatement.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(@NonNull JavaContext context) {
        return new JavaElementWalkVisitor(context);
    }

    private static class JavaElementWalkVisitor extends JavaElementVisitor {
        private final JavaContext mContext;
        private MultiPurposeFunctionFlagChecker mVisitor;

        private JavaElementWalkVisitor(JavaContext context) {
            mContext = context;
            mVisitor = new MultiPurposeFunctionFlagChecker(context);
        }


        //
        // JavaElementVisitor の取り扱い PsiElement 一覧
        //

        @Override
        public void visitAnonymousClass(PsiAnonymousClass aClass) {
            if (DEBUG) mVisitor.reporter.report("PsiAnonymousClass", aClass.getText(), aClass);
            mVisitor.check(aClass);
        }

        @Override
        public void visitClass(PsiClass aClass) {
            if (DEBUG) mVisitor.reporter.report("PsiClass", aClass.getText(), aClass);
            mVisitor.check(aClass);
        }

        @Override
        public void visitIfStatement(PsiIfStatement statement) {
            if (DEBUG) mVisitor.reporter.report("PsiIfStatement", statement.getText(), statement);
            mVisitor.check(statement);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            if (DEBUG) mVisitor.reporter.report("PsiMethod", method.getText(), method);
            mVisitor.check(method);
        }
    }

    private static boolean DEBUG = false;
    private static boolean DEBUG_DETAIL = false;

    private void reportBeforeCheckProject(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("beforeCheckProject (Ph."+context.getPhase()+")<<<");
    }

    private void reportAfterCheckProject(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("afterCheckProject (Ph."+context.getPhase()+")<<<");
    }

    private void reportBeforeCheckFile(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("beforeCheckFile (Ph."+context.getPhase()+") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }

    private void reportAfterCheckFile(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("afterCheckFile (Ph."+context.getPhase()+") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }


    /**
     * メソッドが多重の責務（役割）を提供していないかチェックするクラス
     * <p>
     *     メソッド引数の boolean フラグによる処理の切り替えかつ、
     *     切り替え処理内で状態(フィールド変数)の変更が行われていないか、
     *     チェックします。
     * </p>
     */
    private static class MultiPurposeFunctionFlagChecker {

        private final JavaContext mContext;
        private PsiClass mClass;
        private Map<String, PsiField> mClassFields = new HashMap<>();
        private PsiMethod mMethod;
        private Map<String, PsiParameter> mFlagMethodParameters = new HashMap<>();
        private PsiIfStatement mFlagLogic;

        private final PsiClassStructureDetector.VisitReporter reporter;

        MultiPurposeFunctionFlagChecker(JavaContext context) {
            mContext = context;
            reporter = new PsiClassStructureDetector.VisitReporter(context);
        }

        void check(@NonNull PsiClass clazz) {
            // インナークラスは、考慮していません。
            mClass = clazz;
            mClassFields.clear();
            mMethod = null;
            mFlagMethodParameters.clear();
            mFlagLogic = null;

            for (PsiField field : mClass.getFields()) {
                mClassFields.put(field.getName(), field);
            }
        }

        void check(@NonNull PsiMethod method) {
            if (mClass != null &&
                    !mClassFields.isEmpty()) {

                // メソッド引数に boolean のパラメータがある場合のみ対象とする
                Map<String, PsiParameter> parameters = new HashMap<>();
                PsiParameter[] params = method.getParameterList().getParameters();
                for (PsiParameter param : params) {
                    if (param.getType() != PsiType.BOOLEAN) continue;
                    parameters.put(param.getName(), param);
                }
                if (parameters.isEmpty()) return;

                mMethod = method;
                mFlagMethodParameters.clear();
                mFlagMethodParameters.putAll(parameters);
                mFlagLogic = null;
            }
        }

        void check(@NonNull PsiIfStatement flagLogic) {
            if (mClass != null &&
                    !mClassFields.isEmpty() &&
                    mMethod != null &&
                    !mFlagMethodParameters.isEmpty()) {

                PsiExpression condition = flagLogic.getCondition();
                PsiStatement thenBranch = flagLogic.getThenBranch();
                PsiStatement elseBranch = flagLogic.getElseBranch();
                if (condition == null || thenBranch == null || elseBranch == null) return;

                // 条件式に boolean のメソッドパラメータを使っているかチェック
                boolean useParameter = false;
                List<PsiIdentifier> identifiers = ElementUtil.extractIdentifiers(condition);
                for (PsiIdentifier id : identifiers) {
                    if (mFlagMethodParameters.containsKey(id.getText())) {
                        PsiParameter param = mFlagMethodParameters.get(id.getText());

                        // パラメータと同名のクラスやインスタンス変数か否かをチェック
                        boolean isInstance = ElementUtil.isThisExpression(id) || ElementUtil.isRefExpression(id);
                        Debug.ifConditionInstance(id, condition, isInstance);
                        if (isInstance) continue;

                        // パラメータと同名のローカル変数か否かをチェック
                        PsiElement element =
                                ElementUtil.ScopeBacktrack
                                        .seek(condition, param.getNameIdentifier());
                        useParameter = (element != null && element instanceof PsiParameter);
                        Debug.ifConditionLocal(id, condition, useParameter);
                        if (useParameter) break;
                    }
                }
                if (!useParameter) return;

                // then 節で、フィールド変数を変更(代入)したかチェック
                List<PsiExpression> writeFieldThenExpressions = new ArrayList<>();
                List<PsiExpression> thenExprs = ElementUtil.extractExpressions(thenBranch);
                for (PsiExpression expr : thenExprs) {
                    // 代入式を選択
                    PsiAssignmentExpression assignment = null;
                    if (expr instanceof PsiAssignmentExpression) {
                        assignment = (PsiAssignmentExpression) expr;
                    } else {
                        List<PsiAssignmentExpression> assignments = ElementUtil.extractAssignments(expr);
                        assignment = assignments.isEmpty() ? null : assignments.get(0);
                    }
                    Debug.ifThenBranchAssignment(assignment, expr);
                    if (assignment == null) continue;

                    List<PsiIdentifier> ids = ElementUtil.extractIdentifiers(assignment.getLExpression());
                    if (ids.isEmpty()) continue;

                    PsiIdentifier id = ids.get(0);
                    Debug.ifThenBranchField(id, expr);
                    if (mClassFields.containsKey(id.getText())) {
                        // フィールドと同名のローカル変数やパラメータか否かをチェック
                        PsiElement element =
                                ElementUtil.ScopeBacktrack
                                        .seek(expr, mClassFields.get(id.getText()).getNameIdentifier());
                        Debug.ifThenBranchId(id, expr, element);
                        if (element != null) continue;

                        writeFieldThenExpressions.add(expr);
                    }
                }
                if (writeFieldThenExpressions.isEmpty()) return;

                // else 節で、フィールド変数を変更(代入)したかチェック
                List<PsiExpression> writeFieldElseExpressions = new ArrayList<>();
                List<PsiExpression> elseExprs = ElementUtil.extractExpressions(elseBranch);
                for (PsiExpression expr : elseExprs) {
                    // 代入式を選択
                    PsiAssignmentExpression assignment = null;
                    if (expr instanceof PsiAssignmentExpression) {
                        assignment = (PsiAssignmentExpression) expr;
                    } else {
                        List<PsiAssignmentExpression> assignments = ElementUtil.extractAssignments(expr);
                        assignment = assignments.isEmpty() ? null : assignments.get(0);
                    }
                    Debug.ifElseBranchAssignment(assignment, expr);
                    if (assignment == null) continue;

                    List<PsiIdentifier> ids = ElementUtil.extractIdentifiers(assignment.getLExpression());
                    if (ids.isEmpty()) continue;

                    PsiIdentifier id = ids.get(0);
                    Debug.ifElseBranchField(id, expr);
                    if (mClassFields.containsKey(id.getText())) {
                        // フィールドと同名のローカル変数やパラメータか否かをチェック
                        PsiElement element =
                                ElementUtil.ScopeBacktrack
                                        .seek(expr, mClassFields.get(id.getText()).getNameIdentifier());
                        Debug.ifElseBranchId(id, expr, element);
                        if (element != null) continue;

                        writeFieldElseExpressions.add(expr);
                    }
                }
                if (writeFieldElseExpressions.isEmpty()) return;

                mFlagLogic = flagLogic;

                // ISSUE レポート
                String contents = mContext.getJavaFile().getText();
                int startOffset = mFlagLogic.getTextRange().getStartOffset();
                int endOffset = mFlagLogic.getTextRange().getEndOffset();
                String message = "メソッドのbooleanパラメータで、クラスの状態変更を切り替えています。\n"
                        + "多重責務(役割)のメソッドは、テスタビリティを下げるので、"
                        + "ifブロック処理を別メソッドに分離することをおすすめします。";
                Location location = Location.create(mContext.file, contents, startOffset, endOffset);

                mContext.report(ISSUE, location, message);
                Debug.report(startOffset, endOffset, contents, mFlagLogic);
            }

        }

        private static class Debug {

            private static void report(int startOffset, int endOffset, String contents, PsiElement element) {
                if (!DEBUG_DETAIL) return;

                System.out.println("find issue -> startOffset="+startOffset+", endOffset="+endOffset
                        +", \nrange=>>>\n"+contents.substring(startOffset,endOffset)
                        + "\n<<<, \nsource >>>\n" + element.getText() + "\n<<<<");
                System.out.println();
            }

            private static void ifConditionInstance(PsiIdentifier id, PsiExpression condition, boolean isInstance) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition -> id=" + id.getText()
                        + ", source=" + condition.getText()
                        + ", isInstance=" + isInstance);
            }
            private static void ifConditionLocal(PsiIdentifier id, PsiExpression condition, boolean useParameter) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition -> id=" + id.getText()
                        + ", source=" + condition.getText()
                        + ", useParameter=" + useParameter);
            }
            private static void ifThenBranchAssignment(PsiAssignmentExpression assignment, PsiExpression expr) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition thenBranch-> assignment=" + (assignment != null ? assignment.getText() : "null")
                        + ", source=" + expr.getText());
            }
            private static void ifThenBranchField(PsiIdentifier id, PsiExpression expr) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition thenBranch-> field=" + id.getText()
                        + ", source=" + expr.getText());
            }
            private static void ifThenBranchId(PsiIdentifier id, PsiExpression expr, PsiElement element) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition thenBranch-> id=" + id.getText()
                        + ", source=" + expr.getText()
                        + ", localVarue or parameter=" + (element != null));
            }
            private static void ifElseBranchAssignment(PsiAssignmentExpression assignment, PsiExpression expr) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition elseBranch-> assignment=" + (assignment != null ? assignment.getText() : "null")
                        + ", source=" + expr.getText());
            }
            private static void ifElseBranchField(PsiIdentifier id, PsiExpression expr) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition elseBranch-> field=" + id.getText()
                        + ", source=" + expr.getText());
            }
            private static void ifElseBranchId(PsiIdentifier id, PsiExpression expr, PsiElement element) {
                if (!DEBUG_DETAIL) return;
                System.out.println("condition elseBranch-> id=" + id.getText()
                        + ", source=" + expr.getText()
                        + ", localVarue or parameter=" + (element != null));
            }
        }

    }
}
