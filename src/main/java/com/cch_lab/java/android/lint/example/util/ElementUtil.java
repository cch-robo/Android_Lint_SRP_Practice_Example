package com.cch_lab.java.android.lint.example.util;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.tools.lint.detector.api.LintUtils;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiParenthesizedExpression;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 任意ノード抽出ユーティリティ
 * <p>
 *     指定ノードをルート(親)として、
 *     任意タイプのノードをリカーシブルに全て抽出します。
 * </p>
 */
public class ElementUtil {

    /**
     * クラスアノテーション取得
     * @param clazz ルートノード
     * @return クラスに付加されたアノテーションのリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiAnnotation> getAnnotations(@NonNull PsiClass clazz) {
        PsiModifierList modifierList = clazz.getModifierList();
        if (modifierList == null) return new ArrayList<>();

        PsiAnnotation[] annotations = modifierList.getAnnotations();
        return Arrays.asList(annotations);
    }

    /**
     * フィールド宣言抽出
     * @param clazz ルートノード
     * @return フィールド宣言のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiField> extractFields(@NonNull PsiClass clazz) {
        List<PsiField> fields = new ArrayList<>();
        extractFields(clazz, fields);
        return fields;
    }
    private static void extractFields(@NonNull PsiElement element, List<PsiField> fields) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiField) {
                fields.add((PsiField) child);
                continue;
            }
            extractFields(child, fields);
        }
    }

    /**
     * メソッド宣言抽出
     * @param clazz ルートノード
     * @return メソッド宣言のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiMethod> extractMethods(@NonNull PsiClass clazz) {
        List<PsiMethod> methods = new ArrayList<>();
        extractMethods(clazz, methods);
        return methods;
    }
    private static void extractMethods(@NonNull PsiElement element, List<PsiMethod> methods) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiMethod) {
                methods.add((PsiMethod) child);
                continue;
            }
            extractMethods(child, methods);
        }
    }

    /**
     * メソッドパラメータ取得
     * @param method ルートノード
     * @return メソッドバラメータのリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiParameter> getParameters(@NonNull PsiMethod method) {
        List<PsiParameter> parameterList;
        PsiParameterList parameters = method.getParameterList();
        PsiParameter[] params = parameters.getParameters();
        parameterList = Arrays.asList(params);
        return parameterList;
    }

    /**
     * 文抽出
     * @param element ルートノード
     * @return 文のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiStatement> extractStatements(@NonNull PsiElement element) {
        List<PsiStatement> statements = new ArrayList<>();
        extractStatements(element, statements);
        return statements;
    }
    private static void extractStatements(@NonNull PsiElement element, List<PsiStatement> statements) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiStatement) {
                statements.add((PsiStatement) child);
                continue;
            }
            extractStatements(child, statements);
        }
    }

    /**
     * 宣言文抽出
     * @param element ルートノード
     * @return 宣言文のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiDeclarationStatement> extractDeclarations(@NonNull PsiElement element) {
        List<PsiDeclarationStatement> declarations = new ArrayList<>();
        extractDeclarations(element, declarations);
        return declarations;
    }
    private static void extractDeclarations(@NonNull PsiElement element, List<PsiDeclarationStatement> declarations) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiDeclarationStatement) {
                declarations.add((PsiDeclarationStatement) child);
                continue;
            }
            extractDeclarations(child, declarations);
        }
    }

    /**
     * ローカル変数取得 (宣言文で定義された変数)
     * @param declaration ルートノード
     * @return ローカル変数 (空要素の場合もあります)
     */
    public static @Nullable PsiLocalVariable getVariable(PsiDeclarationStatement declaration) {
        PsiElement[] elements = declaration.getDeclaredElements();
        for (PsiElement element : elements) {
            if (element instanceof PsiLocalVariable) {
                PsiLocalVariable valiable = (PsiLocalVariable) element;
                return valiable;
            }
        }
        return null;
    }

    /**
     * ローカル変数抽出 (ローカル変数宣言のローカル変数の抽出)
     * @param element ルートノード
     * @return ローカル変数のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiLocalVariable> extractLocalVariable(@NonNull PsiElement element) {
        List<PsiLocalVariable> variables = new ArrayList<>();
        extractLocalVariable(element, variables);
        return variables;
    }
    private static void extractLocalVariable(@NonNull PsiElement element, List<PsiLocalVariable> variables) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiLocalVariable) {
                variables.add((PsiLocalVariable) child);
                continue;
            }
            extractLocalVariable(child, variables);
        }
    }

    /**
     * 変数抽出 (変数宣言の変数の抽出)
     * @param element ルートノード
     * @return 変数のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiVariable> extractVariables(@NonNull PsiElement element) {
        List<PsiVariable> variables = new ArrayList<>();
        extractVariables(element, variables);
        return variables;
    }
    private static void extractVariables(@NonNull PsiElement element, List<PsiVariable> variables) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiVariable) {
                variables.add((PsiVariable) child);
                continue;
            }
            extractVariables(child, variables);
        }
    }

    /**
     * 参照式抽出 (変数などの参照)
     * @param element ルートノード
     * @return 参照式のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiReferenceExpression> extractReference(@NonNull PsiElement element) {
        List<PsiReferenceExpression> references = new ArrayList<>();
        extractReference(element, references);
        return references;
    }
    private static void extractReference(@NonNull PsiElement element, List<PsiReferenceExpression> references) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiReferenceExpression) {
                references.add((PsiReferenceExpression) child);
                continue;
            }
            extractReference(child, references);
        }
    }

    /**
     * 代入式
     * @param element ルートノード
     * @return 代入式のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiAssignmentExpression> extractAssignments(@NonNull PsiElement element) {
        List<PsiAssignmentExpression> assignments = new ArrayList<>();
        extractAssignments(element, assignments);
        return assignments;
    }
    private static void extractAssignments(@NonNull PsiElement element, List<PsiAssignmentExpression> assignments) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiAssignmentExpression) {
                assignments.add((PsiAssignmentExpression) child);
                continue;
            }
            extractAssignments(child, assignments);
        }
    }

    /**
     * 式抽出
     * @param element ルートノード
     * @return 式のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiExpression> extractExpressions(@NonNull PsiElement element) {
        List<PsiExpression> expressions = new ArrayList<>();
        extractExpressions(element, expressions);
        return expressions;
    }
    private static void extractExpressions(@NonNull PsiElement element, List<PsiExpression> expressions) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiExpression) {
                expressions.add((PsiExpression) child);
                continue;
            }
            extractExpressions(child, expressions);
        }
    }

    /**
     * 識別子(名前)抽出
     * @param element ルートノード
     * @return 識別子のリスト (空要素の場合もあります)
     */
    public static @NonNull List<PsiIdentifier> extractIdentifiers(@NonNull PsiElement element) {
        List<PsiIdentifier> identifiers = new ArrayList<>();
        extractIdentifiers(element, identifiers);
        return identifiers;
    }
    private static void extractIdentifiers(@NonNull PsiElement element, List<PsiIdentifier> identifiers) {
        for (PsiElement child : element.getChildren()) {
            if (child instanceof PsiIdentifier) {
                identifiers.add((PsiIdentifier) child);
                continue;
            }
            extractIdentifiers(child, identifiers);
        }
    }

    /**
     * 識別子の this 式判定
     * @param id 識別子ノード
     * @return 識別子ノードが、this式の名称か否か (true:this式の識別子、false:this式の識別子でない)
     */
    public static boolean isThisExpression(@NonNull PsiIdentifier id) {
        // 名称がthis式の識別名か否かをチェック
        List<PsiElement> elderBrothers = ElementUtil.getElderBrother(id);
        boolean isThisExpr = false;
        for (PsiElement brother : elderBrothers) {
            isThisExpr = isThisExpr ||
                    (brother instanceof PsiThisExpression) ||
                    (brother instanceof PsiParenthesizedExpression);
        }
        return isThisExpr;
    }

    /**
     * 識別子の 参照式 判定
     * @param id 識別子ノード
     * @return 識別子ノードが、参照式(インスタンス要素）の名称か否か
     * (true:参照式の識別子、false:参照式の識別子でない)
     */
    public static boolean isRefExpression(@NonNull PsiIdentifier id) {
        // 名称が参照式の識別子か否かをチェック
        List<PsiElement> elderBrothers = ElementUtil.getElderBrother(id);
        boolean isInstance = false;
        for (PsiElement brother : elderBrothers) {
            isInstance = isInstance ||
                    (brother instanceof PsiReferenceExpression);
        }
        return isInstance;
    }

    /**
     * 兄要素(ノード)取得
     * @param element ルートノード
     * @return 兄弟要素の兄分(左側)のリスト (空要素の場合もあります)
     */
    public static @Nullable List<PsiElement> getElderBrother(@NonNull PsiElement element) {
        List<PsiElement> elderBrothers = new ArrayList<>();
        PsiElement parent = LintUtils.skipParentheses(element.getParent());
        if (parent == null) return elderBrothers;

        for (PsiElement brother : parent.getChildren()) {
            if (brother == element) break;
            elderBrothers.add(brother);
        }
        return elderBrothers;
    }

    /**
     * 弟要素(ノード)取得
     * @param element ルートノード
     * @return 兄弟要素の弟分(右側)のリスト (空要素の場合もあります)
     */
    public static @Nullable List<PsiElement> getYoungerBrother(@NonNull PsiElement element) {
        List<PsiElement> youngerBrothers = new ArrayList<>();
        PsiElement parent = LintUtils.skipParentheses(element.getParent());
        if (parent == null) return youngerBrothers;

        boolean isYounger = false;
        for (PsiElement brother : parent.getChildren()) {
            if (isYounger) youngerBrothers.add(brother);
            if (brother == element) isYounger = true;
        }
        return youngerBrothers;
    }

    /**
     * スコープ・バックトラック
     * <p>
     *     メソッド内のノードからメソッドルートまで逆戻りしながら、
     *     指定名称の識別子(変数)が存在するかチェックします。
     * </p>
     */
    public static class ScopeBacktrack {

        /**
         * 指定名称のローカル変数またはメソッドパラメータを探索します。
         * @param element メソッド内の探索開始点ノード
         * @param target 探索する識別子(変数名)
         * @return 探索された要素 (見つからなかった場合は、nullが返ります)
         * <ul>
         *     <li>メソッドパラメータが見つかった場合は、PsiLocalVariable インスタンスの要素が返ります。</li>
         *     <li>ローカル変数が見つかった場合は、PsiParameter インスタンスの要素が返ります。</li>
         * </ul>
         */
        public static @Nullable PsiElement seek(PsiElement element, PsiIdentifier target) {
            Find find = new Find();
            seek(element, target, find);
            return find.isEmpty() ? null :
                    find.findLocalVariable != null
                            ? find.findLocalVariable
                            : find.findParameter;
        }
        private static void seek(PsiElement element, PsiIdentifier target, Find find) {
            while(!(element instanceof PsiMethod)) {
                seekElderBrother(element, target, find);
                if (!find.isEmpty()) return;
                element = LintUtils.skipParentheses(element.getParent());
            }
        }
        private static void seekElderBrother(PsiElement element, PsiIdentifier target, Find find){
            PsiElement parent = LintUtils.skipParentheses(element.getParent());
            if (parent == null) return;

            int myIndex = getMyBrotherIndex(element);
            PsiElement[] brothers = parent.getChildren();
            for (int index = myIndex -1; index >= 0; index--) {
                PsiElement brother = brothers[index];
                // 兄弟のブロック内のノードは、アクセス不能なので除外
                if (brother instanceof PsiBlockStatement) continue;
                if (brother instanceof PsiLocalVariable) {
                    PsiLocalVariable localVariable = (PsiLocalVariable) brother;
                    if (isSameName(target, localVariable.getNameIdentifier())) {
                        find.findLocalVariable = localVariable;
                        return;
                    }
                }
                if (brother instanceof PsiParameter) {
                    PsiParameter parameter = (PsiParameter) brother;
                    if (isSameName(target, parameter.getNameIdentifier())) {
                        find.findParameter = parameter;
                        return;
                    }
                }
                seekChildren(brother, target, find);
                if (!find.isEmpty()) return;
            }
        }
        private static void seekChildren(PsiElement element, PsiIdentifier target, Find find) {
            for (PsiElement child : element.getChildren()) {
                if (child instanceof PsiLocalVariable) {
                    PsiLocalVariable localVariable = (PsiLocalVariable) child;
                    if (isSameName(target, localVariable.getNameIdentifier())) {
                        find.findLocalVariable = localVariable;
                        return;
                    }
                }
                if (child instanceof PsiParameter) {
                    PsiParameter parameter = (PsiParameter) child;
                    if (isSameName(target, parameter.getNameIdentifier())) {
                        find.findParameter = parameter;
                        return;
                    }
                }
                seekChildren(child, target, find);
                if (!find.isEmpty()) return;
            }
        }

        private static int getMyBrotherIndex(PsiElement my) {
            PsiElement parent =LintUtils.skipParentheses( my.getParent());
            if (parent == null) return ERROR_INDEX;

            PsiElement[] brothers = parent.getChildren();
            for (int index = 0; index < brothers.length; index++) {
                if (brothers[index] == my) return index;
            }
            return ERROR_INDEX;
        }
        private static final int ERROR_INDEX = -1;

        private static boolean isSameName(@NonNull PsiIdentifier target, @NonNull PsiIdentifier check) {
            return target.getText().equals(check.getText());
        }

        private static class Find {
            PsiParameter findParameter;
            PsiLocalVariable findLocalVariable;
            Find(){}

            boolean isEmpty() {
                return findParameter == null && findLocalVariable == null;
            }
        }
    }


    public static void debugAssignments(String tag, PsiElement root, List<PsiAssignmentExpression> assignments) {
        System.out.println(tag + " :root=>>>>\n" + root.getText() + "\n<<<");
        for (PsiAssignmentExpression assignment : assignments) {
            System.out.println("assignment -> " + assignment.getText());
        }
        System.out.println();
    }

    public static void debugFields(String tag, PsiElement root, List<PsiField> fields) {
        System.out.println(tag + " :root=>>>>\n" + root.getText() + "\n<<<");
        for (PsiField field : fields) {
            System.out.println("field -> " + field.getText());
        }
        System.out.println();
    }

    public static void debugDeclarations(String tag, PsiElement root, List<PsiDeclarationStatement> declarations) {
        System.out.println(tag + " :root=>>>>\n" + root.getText() + "\n<<<");
        for (PsiDeclarationStatement declaration : declarations) {
            System.out.println("declaration -> " + declaration.getText());
            debugVariable(declaration);
        }
        System.out.println();
    }

    public static void debugVariable(PsiDeclarationStatement declaration) {
        PsiLocalVariable variable = getVariable(declaration);
        debugVariable(variable);
        System.out.println();
    }

    public static void debugVariable(PsiLocalVariable variable) {
        if (variable != null) {
            PsiIdentifier id = variable.getNameIdentifier();
            PsiTypeElement type = variable.getTypeElement();
            PsiExpression init = variable.getInitializer();
            System.out.println("variable -> " + variable.getText()
                    + ", id=" + (id != null ? id.getText() : "null")
                    + ", type=" + (type != null ? type.getText() : "null")
                    + ", init=" + (init != null ? init.getText() : "null"));
        }
    }

    public static void debugExpressions(String tag, PsiElement root, List<PsiExpression> expressions) {
        System.out.println(tag + " :root=>>>>\n" + root.getText() + "\n<<<");
        for (PsiExpression expr : expressions) {
            System.out.println("expression -> " + expr.getText());
        }
        System.out.println();
    }

    public static void debugParameters(String tag, PsiElement root, List<PsiParameter> parameters) {
        System.out.println(tag + " :root=>>>>\n" + root.getText() + "\n<<<");
        for (PsiParameter param : parameters) {
            PsiIdentifier id = param.getNameIdentifier();
            PsiTypeElement type = param.getTypeElement();
            PsiExpression init = param.getInitializer();
            System.out.println("parameter -> " + param.getText()
                    + ", id=" + (id != null ? id.getText() : "null")
                    + ", type=" + (type != null ? type.getText() : "null"));
        }
        System.out.println();
    }
}
