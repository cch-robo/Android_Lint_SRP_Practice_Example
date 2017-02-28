package com.cch_lab.java.android.lint.example;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.ImplicitVariable;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMethod;
import com.intellij.psi.PsiAnnotationParameterList;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiArrayAccessExpression;
import com.intellij.psi.PsiArrayInitializerExpression;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiAssertStatement;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiBreakStatement;
import com.intellij.psi.PsiCallExpression;
import com.intellij.psi.PsiCatchSection;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassInitializer;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiConditionalExpression;
import com.intellij.psi.PsiContinueStatement;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiDoWhileStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEmptyStatement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiEnumConstantInitializer;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiExpressionListStatement;
import com.intellij.psi.PsiExpressionStatement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiForStatement;
import com.intellij.psi.PsiForeachStatement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiImportStaticReferenceElement;
import com.intellij.psi.PsiImportStaticStatement;
import com.intellij.psi.PsiInstanceOfExpression;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiKeyword;
import com.intellij.psi.PsiLabeledStatement;
import com.intellij.psi.PsiLambdaExpression;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiMethodReferenceExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiPackageStatement;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiParenthesizedExpression;
import com.intellij.psi.PsiPolyadicExpression;
import com.intellij.psi.PsiPostfixExpression;
import com.intellij.psi.PsiPrefixExpression;
import com.intellij.psi.PsiReceiverParameter;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.PsiReferenceParameterList;
import com.intellij.psi.PsiResourceExpression;
import com.intellij.psi.PsiResourceList;
import com.intellij.psi.PsiResourceVariable;
import com.intellij.psi.PsiReturnStatement;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiSuperExpression;
import com.intellij.psi.PsiSwitchLabelStatement;
import com.intellij.psi.PsiSwitchStatement;
import com.intellij.psi.PsiSynchronizedStatement;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiThrowStatement;
import com.intellij.psi.PsiTryStatement;
import com.intellij.psi.PsiTypeCastExpression;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeParameterList;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.PsiWhileStatement;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.javadoc.PsiDocToken;
import com.intellij.psi.javadoc.PsiInlineDocTag;

import java.util.Arrays;
import java.util.List;

public class PsiClassStructureDetector extends Detector implements Detector.JavaPsiScanner {
    public static final Issue ISSUE = Issue.create(
            "PsiClassStructureReport",
            "Java ソースのASTからクラス内の情報を出力するのみでIssueをレポートしません。",
            "Java ソースのASTからクラス内の情報を出力するサンプルです。",
            Category.CORRECTNESS,
            4,
            Severity.INFORMATIONAL,
            new Implementation(
                    PsiClassStructureDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public PsiClassStructureDetector() {
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
                PsiAnonymousClass.class,
                PsiClass.class,
                PsiAnnotation.class,
                PsiPackage.class,
                PsiCodeBlock.class,
                PsiCallExpression.class,
                PsiTypeParameter.class,
                PsiDocComment.class,
                PsiEnumConstant.class,
                PsiEnumConstantInitializer.class,
                PsiArrayAccessExpression.class,
                PsiArrayInitializerExpression.class,
                PsiAssignmentExpression.class,
                PsiBinaryExpression.class,
                PsiClassObjectAccessExpression.class,
                PsiConditionalExpression.class,
                PsiExpression.class,
                PsiInstanceOfExpression.class,
                PsiLambdaExpression.class,
                PsiLiteralExpression.class,
                PsiMethodCallExpression.class,
                PsiMethodReferenceExpression.class,
                PsiNewExpression.class,
                PsiParenthesizedExpression.class,
                PsiPolyadicExpression.class,
                PsiPostfixExpression.class,
                PsiPrefixExpression.class,
                PsiReferenceExpression.class,
                PsiResourceExpression.class,
                PsiSuperExpression.class,
                PsiThisExpression.class,
                PsiTypeCastExpression.class,
                PsiField.class,
                PsiJavaFile.class,
                PsiIdentifier.class,
                PsiArrayInitializerMemberValue.class,
                PsiClassInitializer.class,
                PsiKeyword.class,
                PsiAnnotationParameterList.class,
                PsiExpressionList.class,
                PsiImportList.class,
                PsiModifierList.class,
                PsiParameterList.class,
                PsiReferenceList.class,
                PsiReferenceParameterList.class,
                PsiTypeParameterList.class,
                PsiAnnotationMethod.class,
                PsiMethod.class,
                PsiNameValuePair.class,
                PsiParameter.class,
                PsiReceiverParameter.class,
                PsiImportStaticReferenceElement.class,
                PsiJavaCodeReferenceElement.class,
                PsiResourceList.class,
                PsiCatchSection.class,
                PsiAssertStatement.class,
                PsiBlockStatement.class,
                PsiBreakStatement.class,
                PsiContinueStatement.class,
                PsiDeclarationStatement.class,
                PsiDoWhileStatement.class,
                PsiEmptyStatement.class,
                PsiExpressionListStatement.class,
                PsiExpressionStatement.class,
                PsiForeachStatement.class,
                PsiForStatement.class,
                PsiIfStatement.class,
                PsiImportStatement.class,
                PsiImportStaticStatement.class,
                PsiLabeledStatement.class,
                PsiPackageStatement.class,
                PsiReturnStatement.class,
                PsiStatement.class,
                PsiSwitchLabelStatement.class,
                PsiSwitchStatement.class,
                PsiSynchronizedStatement.class,
                PsiThrowStatement.class,
                PsiTryStatement.class,
                PsiWhileStatement.class,
                PsiDocTag.class,
                PsiInlineDocTag.class,
                PsiDocToken.class,
                PsiJavaToken.class,
                PsiTypeElement.class,
                PsiDocTagValue.class,
                ImplicitVariable.class,
                PsiLocalVariable.class,
                PsiResourceVariable.class,
                PsiVariable.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(@NonNull JavaContext context) {
        return new JavaElementWalkVisitor(context);
    }

    private static class JavaElementWalkVisitor extends JavaElementVisitor {
        private final JavaContext mContext;
        private VisitReporter mVisitor;

        private JavaElementWalkVisitor(JavaContext context) {
            mContext = context;
            mVisitor = new VisitReporter(context);
        }


        //
        // JavaElementVisitor の取り扱い PsiElement 一覧
        //

        @Override
        public void visitAnonymousClass(PsiAnonymousClass aClass) {
            mVisitor.report("PsiAnonymousClass", aClass.getText(), aClass);
            super.visitClass(aClass);
        }

        @Override
        public void visitClass(PsiClass aClass) {
            mVisitor.report("PsiClass", aClass.getText(), aClass);
            super.visitElement(aClass);
        }

        @Override
        public void visitArrayAccessExpression(PsiArrayAccessExpression expression) {
            mVisitor.report("PsiArrayAccessExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitArrayInitializerExpression(PsiArrayInitializerExpression expression) {
            mVisitor.report("PsiArrayInitializerExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitAssertStatement(PsiAssertStatement statement) {
            mVisitor.report("PsiAssertStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitAssignmentExpression(PsiAssignmentExpression expression) {
            mVisitor.report("PsiAssignmentExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitBinaryExpression(PsiBinaryExpression expression) {
            mVisitor.report("PsiBinaryExpression", expression.getText(), expression);
            super.visitPolyadicExpression(expression);
        }

        @Override
        public void visitBlockStatement(PsiBlockStatement statement) {
            mVisitor.report("PsiBlockStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitBreakStatement(PsiBreakStatement statement) {
            mVisitor.report("PsiBreakStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitClassInitializer(PsiClassInitializer initializer) {
            mVisitor.report("PsiClassInitializer", initializer.getText(), initializer);
            super.visitElement(initializer);
        }

        @Override
        public void visitClassObjectAccessExpression(PsiClassObjectAccessExpression expression) {
            mVisitor.report("PsiClassObjectAccessExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitCodeBlock(PsiCodeBlock block) {
            mVisitor.report("PsiCodeBlock", block.getText(), block);
            super.visitElement(block);
        }

        @Override
        public void visitConditionalExpression(PsiConditionalExpression expression) {
            mVisitor.report("PsiConditionalExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitContinueStatement(PsiContinueStatement statement) {
            mVisitor.report("PsiContinueStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitDeclarationStatement(PsiDeclarationStatement statement) {
            mVisitor.report("PsiDeclarationStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitDocComment(PsiDocComment comment) {
            mVisitor.report("PsiDocComment", comment.getText(), comment);
            super.visitComment(comment);
        }

        @Override
        public void visitDocTag(PsiDocTag tag) {
            mVisitor.report("PsiDocTag", tag.getText(), tag);
            super.visitElement(tag);
        }

        @Override
        public void visitDocTagValue(PsiDocTagValue value) {
            mVisitor.report("PsiDocTagValue", value.getText(), value);
            super.visitElement(value);
        }

        @Override
        public void visitDoWhileStatement(PsiDoWhileStatement statement) {
            mVisitor.report("PsiDoWhileStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitEmptyStatement(PsiEmptyStatement statement) {
            mVisitor.report("PsiEmptyStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitExpression(PsiExpression expression) {
            mVisitor.report("PsiExpression", expression.getText(), expression);
            super.visitElement(expression);
        }

        @Override
        public void visitExpressionList(PsiExpressionList list) {
            mVisitor.report("PsiExpressionList", list.getText(), list);
            mVisitor.report(list);
            super.visitElement(list);
        }

        @Override
        public void visitExpressionListStatement(PsiExpressionListStatement statement) {
            mVisitor.report("PsiExpressionListStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitExpressionStatement(PsiExpressionStatement statement) {
            mVisitor.report("PsiExpressionStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitField(PsiField field) {
            mVisitor.report("PsiField", field.getText(), field);
            super.visitVariable(field);
        }

        @Override
        public void visitForStatement(PsiForStatement statement) {
            mVisitor.report("PsiForStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitForeachStatement(PsiForeachStatement statement) {
            mVisitor.report("PsiForeachStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitIdentifier(PsiIdentifier identifier) {
            mVisitor.report("PsiIdentifier", identifier.getText(), identifier);
            super.visitJavaToken(identifier);
        }

        @Override
        public void visitIfStatement(PsiIfStatement statement) {
            mVisitor.report("PsiIfStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitImportList(PsiImportList list) {
            mVisitor.report("PsiImportList", list.getText(), list);
            super.visitElement(list);
        }

        @Override
        public void visitImportStatement(PsiImportStatement statement) {
            mVisitor.report("PsiImportStatement", statement.getText(), statement);
            super.visitElement(statement);
        }

        @Override
        public void visitImportStaticStatement(PsiImportStaticStatement statement) {
            mVisitor.report("PsiImportStaticStatement", statement.getText(), statement);
            super.visitElement(statement);
        }

        @Override
        public void visitInlineDocTag(PsiInlineDocTag tag) {
            mVisitor.report("PsiInlineDocTag", tag.getText(), tag);
            super.visitDocTag(tag);
        }

        @Override
        public void visitInstanceOfExpression(PsiInstanceOfExpression expression) {
            mVisitor.report("PsiInstanceOfExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitJavaToken(PsiJavaToken token) {
            mVisitor.report("PsiJavaToken", token.getText(), token);
            super.visitElement(token);
        }

        @Override
        public void visitKeyword(PsiKeyword keyword) {
            mVisitor.report("PsiKeyword", keyword.getText(), keyword);
            super.visitJavaToken(keyword);
        }

        @Override
        public void visitLabeledStatement(PsiLabeledStatement statement) {
            mVisitor.report("PsiLabeledStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitLiteralExpression(PsiLiteralExpression expression) {
            mVisitor.report("PsiLiteralExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitLocalVariable(PsiLocalVariable variable) {
            mVisitor.report("PsiLocalVariable", variable.getText(), variable);
            super.visitVariable(variable);
        }

        @Override
        public void visitMethod(PsiMethod method) {
            mVisitor.report("PsiMethod", method.getText(), method);
            super.visitElement(method);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            mVisitor.report("PsiMethodCallExpression", expression.getText(), expression);
            super.visitCallExpression(expression);
        }

        @Override
        public void visitCallExpression(PsiCallExpression callExpression) {
            mVisitor.report("PsiCallExpression", callExpression.getText(), callExpression);
            super.visitExpression(callExpression);
        }

        @Override
        public void visitModifierList(PsiModifierList list) {
            mVisitor.report("PsiModifierList", list.getText(), list);
            mVisitor.report(list);
            super.visitElement(list);
        }

        @Override
        public void visitNewExpression(PsiNewExpression expression) {
            mVisitor.report("PsiNewExpression", expression.getText(), expression);
            super.visitCallExpression(expression);
        }

        @Override
        public void visitPackage(PsiPackage aPackage) {
            mVisitor.report("PsiPackage", aPackage.getText(), aPackage);
            super.visitElement(aPackage);
        }

        @Override
        public void visitPackageStatement(PsiPackageStatement statement) {
            mVisitor.report("PsiPackageStatement", statement.getText(), statement);
            super.visitElement(statement);
        }

        @Override
        public void visitParameter(PsiParameter parameter) {
            mVisitor.report("PsiParameter", parameter.getText(), parameter);
            super.visitVariable(parameter);
        }

        @Override
        public void visitReceiverParameter(PsiReceiverParameter parameter) {
            mVisitor.report("PsiReceiverParameter", parameter.getText(), parameter);
            super.visitVariable(parameter);
        }

        @Override
        public void visitParameterList(PsiParameterList list) {
            mVisitor.report("PsiParameterList", list.getText(), list);
            mVisitor.report(list);
            super.visitElement(list);
        }

        @Override
        public void visitParenthesizedExpression(PsiParenthesizedExpression expression) {
            mVisitor.report("PsiParenthesizedExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitPostfixExpression(PsiPostfixExpression expression) {
            mVisitor.report("PsiPostfixExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitPrefixExpression(PsiPrefixExpression expression) {
            mVisitor.report("PsiPrefixExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitReferenceElement(PsiJavaCodeReferenceElement reference) {
            mVisitor.report("PsiJavaCodeReferenceElement", reference.getText(), reference);
            super.visitElement(reference);
        }

        @Override
        public void visitImportStaticReferenceElement(PsiImportStaticReferenceElement reference) {
            mVisitor.report("PsiImportStaticReferenceElement", reference.getText(), reference);
            this.visitElement(reference);
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            mVisitor.report("PsiReferenceExpression", expression.getText(), expression);
        }

        @Override
        public void visitMethodReferenceExpression(PsiMethodReferenceExpression expression) {
            mVisitor.report("PsiMethodReferenceExpression", expression.getText(), expression);
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitReferenceList(PsiReferenceList list) {
            mVisitor.report("PsiReferenceList", list.getText(), list);
            super.visitElement(list);
        }

        @Override
        public void visitReferenceParameterList(PsiReferenceParameterList list) {
            mVisitor.report("PsiReferenceParameterList", list.getText(), list);
            super.visitElement(list);
        }

        @Override
        public void visitTypeParameterList(PsiTypeParameterList list) {
            mVisitor.report("PsiTypeParameterList", list.getText(), list);
            super.visitElement(list);
        }

        @Override
        public void visitReturnStatement(PsiReturnStatement statement) {
            mVisitor.report("PsiReturnStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitStatement(PsiStatement statement) {
            mVisitor.report("PsiStatement", statement.getText(), statement);
            super.visitElement(statement);
        }

        @Override
        public void visitSuperExpression(PsiSuperExpression expression) {
            mVisitor.report("PsiSuperExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
            mVisitor.report("PsiSwitchLabelStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitSwitchStatement(PsiSwitchStatement statement) {
            mVisitor.report("PsiSwitchStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitSynchronizedStatement(PsiSynchronizedStatement statement) {
            mVisitor.report("PsiSynchronizedStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitThisExpression(PsiThisExpression expression) {
            mVisitor.report("PsiThisExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitThrowStatement(PsiThrowStatement statement) {
            mVisitor.report("PsiThrowStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitTryStatement(PsiTryStatement statement) {
            mVisitor.report("PsiTryStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitCatchSection(PsiCatchSection section) {
            mVisitor.report("PsiCatchSection", section.getText(), section);
            super.visitElement(section);
        }

        @Override
        public void visitResourceList(PsiResourceList resourceList) {
            mVisitor.report("PsiResourceList", resourceList.getText(), resourceList);
            super.visitElement(resourceList);
        }

        @Override
        public void visitResourceVariable(PsiResourceVariable variable) {
            mVisitor.report("PsiResourceVariable", variable.getText(), variable);
            super.visitLocalVariable(variable);
        }

        @Override
        public void visitResourceExpression(PsiResourceExpression expression) {
            mVisitor.report("PsiResourceExpression", expression.getText(), expression);
            super.visitElement(expression);
        }

        @Override
        public void visitTypeElement(PsiTypeElement type) {
            mVisitor.report("PsiTypeElement", type.getText(), type);
            super.visitElement(type);
        }

        @Override
        public void visitTypeCastExpression(PsiTypeCastExpression expression) {
            mVisitor.report("PsiTypeCastExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitVariable(PsiVariable variable) {
            mVisitor.report("PsiVariable", variable.getText(), variable);
            super.visitElement(variable);
        }

        @Override
        public void visitWhileStatement(PsiWhileStatement statement) {
            mVisitor.report("PsiWhileStatement", statement.getText(), statement);
            super.visitStatement(statement);
        }

        @Override
        public void visitJavaFile(PsiJavaFile file) {
            mVisitor.report("PsiJavaFile", file.getText(), file);
            super.visitFile(file);
        }

        @Override
        public void visitImplicitVariable(ImplicitVariable variable) {
            mVisitor.report("ImplicitVariable", variable.getText(), variable);
            super.visitLocalVariable(variable);
        }

        @Override
        public void visitDocToken(PsiDocToken token) {
            mVisitor.report("PsiDocToken", token.getText(), token);
            super.visitElement(token);
        }

        @Override
        public void visitTypeParameter(PsiTypeParameter classParameter) {
            mVisitor.report("PsiTypeParameter", classParameter.getText(), classParameter);
            super.visitClass(classParameter);
        }

        @Override
        public void visitAnnotation(PsiAnnotation annotation) {
            mVisitor.report("PsiAnnotation", annotation.getText(), annotation);
            super.visitElement(annotation);
        }

        @Override
        public void visitAnnotationParameterList(PsiAnnotationParameterList list) {
            mVisitor.report("PsiAnnotationParameterList", list.getText(), list);
            super.visitElement(list);
        }

        @Override
        public void visitAnnotationArrayInitializer(PsiArrayInitializerMemberValue initializer) {
            mVisitor.report("PsiArrayInitializerMemberValue", initializer.getText(), initializer);
            super.visitElement(initializer);
        }

        @Override
        public void visitNameValuePair(PsiNameValuePair pair) {
            mVisitor.report("PsiNameValuePair", pair.getText(), pair);
            super.visitElement(pair);
        }

        @Override
        public void visitAnnotationMethod(PsiAnnotationMethod method) {
            mVisitor.report("PsiAnnotationMethod", method.getText(), method);
            super.visitMethod(method);
        }

        @Override
        public void visitEnumConstant(PsiEnumConstant enumConstant) {
            mVisitor.report("PsiEnumConstant", enumConstant.getText(), enumConstant);
            super.visitField(enumConstant);
        }

        @Override
        public void visitEnumConstantInitializer(PsiEnumConstantInitializer enumConstantInitializer) {
            mVisitor.report("PsiEnumConstantInitializer", enumConstantInitializer.getText(), enumConstantInitializer);
            super.visitAnonymousClass(enumConstantInitializer);
        }

        @Override
        public void visitPolyadicExpression(PsiPolyadicExpression expression) {
            mVisitor.report("PsiPolyadicExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }

        @Override
        public void visitLambdaExpression(PsiLambdaExpression expression) {
            mVisitor.report("PsiLambdaExpression", expression.getText(), expression);
            super.visitExpression(expression);
        }
    }

    private void reportBeforeCheckProject(Context context) {
        System.out.println();
        System.out.println("beforeCheckProject (Ph."+context.getPhase()+")<<<");
    }

    private void reportAfterCheckProject(Context context) {
        System.out.println();
        System.out.println("afterCheckProject (Ph."+context.getPhase()+")<<<");
    }

    private void reportBeforeCheckFile(Context context) {
        System.out.println();
        System.out.println("beforeCheckFile (Ph."+context.getPhase()+") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }

    private void reportAfterCheckFile(Context context) {
        System.out.println();
        System.out.println("afterCheckFile (Ph."+context.getPhase()+") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }

    /**
     * 訪問ノードレポータ
     * <p>
     *     visit したノードのJavaソースコードや親子構成をレポートするだけのクラスです。<br/>
     *     問題(Issue)のチェックは行いません。
     * </p>
     */
    public static class VisitReporter {

        VisitReporter(JavaContext context){
        }

        public void report(String nodeName, String source, PsiElement element) {
            System.out.println();
            System.out.println("Node=" + nodeName);
            System.out.println("NodeImpl=" + element.getClass().getSimpleName());
            System.out.println("Source=>>>" + source + "<<<");
            System.out.println("parent=" + parseParent(element));
            System.out.println("children=" + parseChildren(element));
        }

        public String parseParent(PsiElement element) {
            PsiElement parentInfo = element.getParent();
            return parentInfo != null
                    ? (">>>\n" + parentInfo.getText() + "\n<<<:" + parentInfo.getClass().getSimpleName())
                    : "null:null";
        }

        public String parseChildren(PsiElement element) {
            PsiElement[] children = element.getChildren();
            String childrenInfo = "";
            childrenInfo = ">>>[";
            for (PsiElement child : children) {
                childrenInfo += (child.getText() + ":" + child.getClass().getSimpleName() + ", ");
            }
            childrenInfo += (childrenInfo.isEmpty() ? "]<<<" : "\n]<<<");
            return childrenInfo;
        }

        public void report(PsiModifierList list) {
            for(String modifier : PsiModifier.MODIFIERS){
                if (list.hasExplicitModifier(modifier) || list.hasModifierProperty(modifier)) {
                    System.out.println("  PsiModifier -> "+modifier);
                }
            }
        }

        public void report(PsiParameterList list) {
            for(PsiParameter parameter : list.getParameters()){
                System.out.println("  PsiParameter -> "+parameter.getText());
            }
        }

        public void report(PsiExpressionList list) {
            for(PsiExpression expression : list.getExpressions()){
                System.out.println("  PsiExpression -> "+expression.getText());
            }
        }
    }
}
