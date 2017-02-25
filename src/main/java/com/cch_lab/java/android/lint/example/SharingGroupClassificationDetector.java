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
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharingGroupClassificationDetector extends Detector implements Detector.JavaPsiScanner {
    public static final Issue SEPARATE_BY_ISOLATED_GROUP_ISSUE = Issue.create(
            "IsolatedSharingWriteFieldsClassification",
            "変更するフィールド値(状態)の共有度から、メソッド責務が完全共有かつ唯一と判断しました。",
            "変更の共有度によるメソッドグループの変更状態分割提案",
            Category.CORRECTNESS,
            4,
            Severity.INFORMATIONAL
            ,
            new Implementation(
                    SharingGroupClassificationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public static final Issue SEPARATE_BY_GROUP_ISSUE = Issue.create(
            "SharingWriteFieldsClassification",
            "変更するフィールド値(状態)の共有度から、メソッド責務が完全共有かつ唯一でないと判断しました。",
            "変更の共有度によるメソッドグループのクラス分離提案",
            Category.CORRECTNESS,
            4,
            Severity.INFORMATIONAL
            ,
            new Implementation(
                    SharingGroupClassificationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public static final Issue SEPARATE_BY_SINGLE_ISSUE = Issue.create(
            "SingleWriteFieldsClassification",
            "変更するフィールド値(状態)の共有度から、メソッド責務が完全独立と判断しました。",
            "変更の共有度によるメソッドのクラス分離提案",
            Category.CORRECTNESS,
            4,
            Severity.INFORMATIONAL,
            new Implementation(
                    SharingGroupClassificationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public static final Issue TRY_SEPARATE_BY_ROLE_ISSUE = Issue.create(
            "MixedWriteFieldsClassification",
            "変更するフィールド値(状態)の共有度から、メソッド責務が他と混合していると判断しました。",
            "変更の共有度によるメソッドの責務混合問題",
            Category.CORRECTNESS,
            4,
            Severity.WARNING,
            new Implementation(
                    SharingGroupClassificationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public SharingGroupClassificationDetector() {
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
                PsiMethod.class);
    }

    @Override
    public JavaElementVisitor createPsiVisitor(@NonNull JavaContext context) {
        return new JavaElementWalkVisitor(context);
    }

    private static class JavaElementWalkVisitor extends JavaElementVisitor {
        private final JavaContext mContext;
        private SharingFieldGroupChecker mVisitor;

        private JavaElementWalkVisitor(JavaContext context) {
            mContext = context;
            mVisitor = new SharingFieldGroupChecker(context);
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
        System.out.println("beforeCheckProject (Ph." + context.getPhase() + ")<<<");
    }

    private void reportAfterCheckProject(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("afterCheckProject (Ph." + context.getPhase() + ")<<<");
    }

    private void reportBeforeCheckFile(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("beforeCheckFile (Ph." + context.getPhase() + ") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }

    private void reportAfterCheckFile(Context context) {
        if (!DEBUG) return;
        System.out.println();
        System.out.println("afterCheckFile (Ph." + context.getPhase() + ") -> Source=>>>\n" + context.getContents() + "\n<<<");
    }


    /**
     * 状態変更の共有度グループから、クラスの責務（役割）の混在をチェックするクラス
     * <p>
     * はじめに、<strong>単一責務の原則</strong>
     * （SRP:the Simple Responsibility Principal)の視点から、
     * フィールド変数（状態）を変更するメソッドが、
     * クラスでただ１つ(単一責務)か、複数存在するか（複合責務）かを判定します。<br/>
     * <br/>
     * 次に、複合責務の問題があった場合は、
     * 変更するフィールド変数(状態)より、他メソッドとその共有度でメソッドをグループ分けし、
     * グルーピング結果から、クラス内で責務(役割)の混在が発生していないか、
     * チェックします。
     *
     * <ol>状態変更の共有度によるメソッドのグループ分け
     * <li>変更するフィールド変数(状態)を持っていないメソッドである。<br/> ⇒ 関数なので対象外</li>
     * <li>変更するフィールド変数(状態)を持っているクラスで唯一のメソッドである。<br/> ⇒ 単一責務の原則を満たしているので対象外</li>
     * <li>複合責務であるが、共有度100％の他メソッドがなく、かつ 0% < 共有度 < 100% の他メソッドもない。<br/> ⇒ メソッド単独を別のクラスに分離することを勧める</li>
     * <li>複合責務であるが、共有度100％の他メソッドがあり、かつ 0% < 共有度 < 100% の他メソッドもなく、クラスで唯一の状態変更メソッドグループである。<br/> ⇒ メソッドの利用先の責務確認と状態の分割を勧める</li>
     * <li>複合責務であるが、共有度100％の他メソッドがあり、かつ 0% < 共有度 < 100% の他メソッドがないが、クラスに他の状態変更メソッドがある。<br/>⇒ メソッドのグループを別のクラスに分離することを勧める</li>
     * <li>複合責務である上、共有度１００％と0%の他メソッドがなく、0% < 共有度 < 100% の他メソッドしかない。<br/> ⇒ 責務混在メソッドなので、先ずはメソッドの分解や統合を勧める</li>
     * </ol>
     * </p>
     */
    private static class SharingFieldGroupChecker {

        private final JavaContext mContext;
        private PsiClass mClass;
        private Map<String, PsiField> mClassFields;
        private List<WriteFieldGroupMethod> mFunctions;
        private List<WriteFieldGroupMethod> mWriteFieldGroupMethods;
        private List<List<SharedWriteFieldGroup>> mShardWriteFieldGroupMatrix;

        private Map<String, Set<WriteFieldGroupMethod>> mMethodsToSeparateByGroup;
        private List<WriteFieldGroupMethod> mMethodsToSeparateBySingle;
        private Map<String, Set<WriteFieldGroupMethod>> mMethodsTrySeparateByRole;

        private Set<PsiMethod> mUnusedMethods;

        private final PsiClassStructureDetector.VisitReporter reporter;

        SharingFieldGroupChecker(JavaContext context) {
            mContext = context;
            reporter = new PsiClassStructureDetector.VisitReporter(context);
        }

        void check(@NonNull PsiClass clazz) {
            // クラス内のインナークラスや匿名クラスは、対象としていません。
            if (mClass != null) return;

            // 無名クラスは、対象としていません。
            if (clazz.getQualifiedName() == null) return;

            init(clazz);

            Debug.classFields(mClass, mClassFields);
            Debug.classMethods(mClass, mUnusedMethods);
        }

        void check(@NonNull PsiMethod method) {
            if (mClass == null) return;
            Debug.checkedMethod(mClass, method);

            if (!mUnusedMethods.remove(method)) {
                // クラス内のインナークラスや匿名クラスに無名クラスのメソッドは、対象としていません。
                return;
            }
            if (method.isConstructor()) return;

            // メソッドを関数とフィールド変数変更メソッドに分離
            WriteFieldGroupMethod writeFieldGroup = new WriteFieldGroupMethod(method, mClassFields);
            if (writeFieldGroup.hasWriteFields()) {
                mWriteFieldGroupMethods.add(writeFieldGroup);
            } else {
                mFunctions.add(writeFieldGroup);
            }

            if (mUnusedMethods.isEmpty()) {
                Debug.methodTypes(mClass, mWriteFieldGroupMethods, mFunctions);

                // フィールド変数変更メソッドのグループ分け開始
                parse();
                clean();
            }
        }

        private void parse() {
            if (mClass == null
                    || mClassFields.isEmpty()
                    || mWriteFieldGroupMethods.isEmpty()) return;

            createMatrix();
            parseSharedType();
            report();
        }

        private void createMatrix() {

            List<WriteFieldGroupMethod> ownerMethods = new ArrayList<>();
            List<WriteFieldGroupMethod> guestMethods = new ArrayList<>();
            ownerMethods.addAll(mWriteFieldGroupMethods);
            guestMethods.addAll(mWriteFieldGroupMethods);
            mShardWriteFieldGroupMatrix.clear();

            Debug.ownerMethod(mClass);
            for (WriteFieldGroupMethod ownerMethod : ownerMethods) {
                Debug.ownerMethod(ownerMethod);

                List<SharedWriteFieldGroup> sharedWriteFields = new ArrayList<>();
                for (WriteFieldGroupMethod guestMethod : guestMethods) {
                    if (ownerMethod == guestMethod) continue;

                    SharedWriteFieldGroup sharedWriteField =
                            new SharedWriteFieldGroup(ownerMethod, guestMethod);
                    sharedWriteFields.add(sharedWriteField);
                }
                mShardWriteFieldGroupMatrix.add(sharedWriteFields);
            }
            Debug.ownerMethod();
        }

        private void parseSharedType() {

            for (List<SharedWriteFieldGroup> sharedList : mShardWriteFieldGroupMatrix) {
                if (sharedList.isEmpty()) continue;

                Set<WriteFieldGroupMethod> perfectSharedSet = new HashSet<>();
                Set<WriteFieldGroupMethod> mixedShardSet = new HashSet<>();

                for (SharedWriteFieldGroup shared : sharedList) {
                    Debug.sharedMethod(shared);
                    if (shared.getSharedPercentage() == 1.0f) {
                        perfectSharedSet.add(shared.getLeftMethod());
                        perfectSharedSet.add(shared.getRightMethod());
                    } else
                    if (shared.getSharedPercentage() != 0.0f) {
                        mixedShardSet.add(shared.getRightMethod());
                    }
                }

                WriteFieldGroupMethod ownerMethod = sharedList.get(0).getLeftMethod();
                boolean hasPerfectShared = perfectSharedSet.size() > 0;
                boolean hasMixedShared = mixedShardSet.size() > 0;

                if (hasPerfectShared && !hasMixedShared) {
                    // グループで分離をすすめる (共有度１００％)
                    Set<WriteFieldGroupMethod> sharedSet = mMethodsToSeparateByGroup.get(ownerMethod.getWriteFieldsId());
                    if (sharedSet == null) sharedSet = new HashSet<>();
                    sharedSet.addAll(perfectSharedSet);
                    mMethodsToSeparateByGroup.put(ownerMethod.getWriteFieldsId(), sharedSet);

                } else
                if (!hasPerfectShared && !hasMixedShared) {
                    // 単独で分離をすすめる (共有度0%)
                    mMethodsToSeparateBySingle.add(ownerMethod);

                } else {
                    // 責務混在の分離をすすめる (0% < 共有度 < 100%)

                    // Matrix リストオーナー
                    Set<WriteFieldGroupMethod> ownerMixedSet = mMethodsTrySeparateByRole.get(ownerMethod.getWriteFieldsId());
                    if (ownerMixedSet == null) ownerMixedSet = new HashSet<>();
                    ownerMixedSet.add(ownerMethod);
                    mMethodsTrySeparateByRole.put(ownerMethod.getWriteFieldsId(), ownerMixedSet);

                    // Matrix リストゲスト
                    for (WriteFieldGroupMethod mixedMethod : mixedShardSet) {
                        Set<WriteFieldGroupMethod> mixedSet = mMethodsTrySeparateByRole.get(mixedMethod.getWriteFieldsId());
                        if (mixedSet == null) mixedSet = new HashSet<>();
                        mixedSet.add(mixedMethod);
                        mMethodsTrySeparateByRole.put(mixedMethod.getWriteFieldsId(), mixedSet);
                    }
                }
            }
            Debug.methodGroups(mClass,
                    mMethodsToSeparateByGroup,
                    mMethodsToSeparateBySingle,
                    mMethodsTrySeparateByRole);
        }

        private void report() {

            // 変更するフィールド変数(状態)が、完全共有のメソッドグループ
            boolean isIsolatedGroup = (mMethodsToSeparateByGroup.keySet().size() == 1
                                        && mMethodsToSeparateBySingle.size() == 0
                                        && mMethodsTrySeparateByRole.keySet().size() == 0);
            for (Set<WriteFieldGroupMethod> methods : mMethodsToSeparateByGroup.values()) {
                reportSeparateByGroup(methods, isIsolatedGroup);
            }

            // 変更するフィールド変数(状態)が、完全独立のメソッド
            reportSeparateBySingle(mMethodsToSeparateBySingle);

            // 変更するフィールド変数(状態)が、他メソッドと責務(役割)混合のメソッド
            for (Set<WriteFieldGroupMethod> methods : mMethodsTrySeparateByRole.values()) {
                reportTrySeparateByRole(methods);
            }
        }

        private void reportSeparateByGroup(@NonNull Set<WriteFieldGroupMethod> methods, boolean isIsolatedGroup) {
            List<WriteFieldGroupMethod> sharedMethods = new ArrayList<>();
            List<WriteFieldGroupMethod> groupMethods = new ArrayList<>();
            sharedMethods.addAll(methods);

            for (WriteFieldGroupMethod method : sharedMethods) {
                groupMethods.clear();
                groupMethods.addAll(methods);
                groupMethods.remove(method);
                if (isIsolatedGroup) {
                    reportSeparateByIsolatedGroup(method, groupMethods);
                } else {
                    reportSeparateByGroup(method, groupMethods);
                }
            }
        }
        private void reportSeparateByIsolatedGroup(@NonNull WriteFieldGroupMethod method, @NonNull List<WriteFieldGroupMethod> groupMethods) {
            // ISSUE レポート
            String contents = mContext.getJavaFile().getText();
            int startOffset = method.getMethod().getNameIdentifier().getTextRange().getStartOffset();
            int endOffset = method.getMethod().getNameIdentifier().getTextRange().getEndOffset();
            String groups = getSharedMethodNames(groupMethods);
            String fieldNames = getWriteFieldNames(method);
            String message = "メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有かつクラス唯一です。\n"
                    + "これはメソッドの責務(役割)が共有されている、責務合体したクラスであることを示すので、\n"
                    + "他のメソッド " + groups + " と共に、"
                    + "メソッドの利用先が適切であるかを確認して、変更するフイールド変数(状態)の分割をおすすめします。\n"
                    + fieldNames;
            Location location = Location.create(mContext.file, contents, startOffset, endOffset);

            mContext.report(SEPARATE_BY_ISOLATED_GROUP_ISSUE, location, message);
            Debug.report("SeparateByIsolateGroup", method);
        }
        private void reportSeparateByGroup(@NonNull WriteFieldGroupMethod method, @NonNull List<WriteFieldGroupMethod> groupMethods) {
            // ISSUE レポート
            String contents = mContext.getJavaFile().getText();
            int startOffset = method.getMethod().getNameIdentifier().getTextRange().getStartOffset();
            int endOffset = method.getMethod().getNameIdentifier().getTextRange().getEndOffset();
            String groups = getSharedMethodNames(groupMethods);
            String fieldNames = getWriteFieldNames(method);
            String message = "メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有ですがクラス唯一でありません。\n"
                    + "これはメソッドの責務(役割)が共有されていますが、責務混在したクラスであることを示すので、\n"
                    + "他のメソッド " + groups + " と、"
                    + "メソッドが変更するフィールド変数(状態)ごと新クラスへの分離をおすすめします。\n"
                    + fieldNames;
            Location location = Location.create(mContext.file, contents, startOffset, endOffset);

            mContext.report(SEPARATE_BY_GROUP_ISSUE, location, message);
            Debug.report("SeparateByGroup", method);
        }
        private String getSharedMethodNames(@NonNull List<WriteFieldGroupMethod> groupMethods) {
            StringBuilder sb = new StringBuilder();
            for (WriteFieldGroupMethod method : groupMethods) {
                if (sb.length() != 0) sb.append("、");
                sb.append(method.getMethod().getName()).append("()");
            }
            return sb.toString();
        }

        private void reportSeparateBySingle(@NonNull List<WriteFieldGroupMethod> methods) {
            for (WriteFieldGroupMethod method : methods) {
                reportSeparateBySingle(method);
            }
        }
        private void reportSeparateBySingle(@NonNull WriteFieldGroupMethod method) {
            // ISSUE レポート
            String contents = mContext.getJavaFile().getText();
            int startOffset = method.getMethod().getNameIdentifier().getTextRange().getStartOffset();
            int endOffset = method.getMethod().getNameIdentifier().getTextRange().getEndOffset();
            String fieldNames = getWriteFieldNames(method);
            String message = "メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立しています。\n"
                    + "これはメソッドの責務(役割)が独立していることを示すので、\n"
                    + "メソッドが変更するフィールド変数(状態)ごと新クラスへの分離をおすすめします。\n"
                    + fieldNames;
            Location location = Location.create(mContext.file, contents, startOffset, endOffset);

            mContext.report(SEPARATE_BY_SINGLE_ISSUE, location, message);
            Debug.report("SeparateBySingle", method);
        }

        private void reportTrySeparateByRole(@NonNull Set<WriteFieldGroupMethod> methods) {
            for (WriteFieldGroupMethod method : methods) {
                reportTrySeparateByRole(method);
            }
        }
        private void reportTrySeparateByRole(@NonNull WriteFieldGroupMethod method) {
            // ISSUE レポート
            String contents = mContext.getJavaFile().getText();
            int startOffset = method.getMethod().getNameIdentifier().getTextRange().getStartOffset();
            int endOffset = method.getMethod().getNameIdentifier().getTextRange().getEndOffset();
            String fieldNames = getWriteFieldNames(method);
            String message = "メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立でも完全共有でもありません。\n"
                    + "このメソッドが変更するフィールド変数(状態)は、他のメソッドとの間で共有の一部に過不足があります。\n"
                    + "これはメソッドの責務(役割)が明確に区別されておらず、責務(役割)が混在していることを示すので、\n"
                    + "先ずはメソッドや変更フィールド変数(状態)の分割や統合をおすすめします。\n"
                    + fieldNames;
            Location location = Location.create(mContext.file, contents, startOffset, endOffset);

            mContext.report(TRY_SEPARATE_BY_ROLE_ISSUE, location, message);
            Debug.report("TrySeparateByRole", method);
        }

        private String getWriteFieldNames(@NonNull WriteFieldGroupMethod method) {
            StringBuilder sb = new StringBuilder();
            for (PsiField field : method.provideWriteFields().values()) {
                if (sb.length() != 0) sb.append(", ");
                sb.append(field.getName());
            }
            return "fields={" + sb.toString() + "}";
        }

        private void init(@NonNull PsiClass clazz) {
            mClass = clazz;
            mClassFields = new HashMap<>();
            mWriteFieldGroupMethods = new ArrayList<>();
            mShardWriteFieldGroupMatrix = new ArrayList<>();

            mFunctions = new ArrayList<>();
            mMethodsToSeparateByGroup = new HashMap<>();
            mMethodsToSeparateBySingle = new ArrayList<>();
            mMethodsTrySeparateByRole = new HashMap<>();

            mUnusedMethods = new HashSet<>();
            mUnusedMethods.addAll(Arrays.asList(mClass.getMethods()));
            // PsiClass＃getMethods() では、コンストラクタもメソッドに含まれます。

            for (PsiField field : mClass.getFields()) {
                mClassFields.put(field.getName(), field);
            }
        }

        private void clean() {
            mClass = null;
            mClassFields = null;
            mWriteFieldGroupMethods = null;
            mShardWriteFieldGroupMatrix = null;

            mFunctions = null;
            mMethodsToSeparateByGroup = null;
            mMethodsToSeparateBySingle = null;
            mMethodsTrySeparateByRole = null;

            mUnusedMethods = null;
        }


        private static class Debug {

            private static void report(String tag, WriteFieldGroupMethod method) {
                String str = "";
                for (PsiField field : method.provideWriteFields().values()) {
                    str += field.getName() +", ";
                }
                if (DEBUG_DETAIL) System.out.println("find issue -> " + tag + ", method=" + method.getMethod().getName() + ", fields=" + str);
            }

            private static void classFields(PsiClass clazz, Map<String, PsiField> clazzFields){
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> 1 class=" + clazz.getQualifiedName());
                for (PsiField test : clazzFields.values()) {
                    System.out.println("mClassFields field=" + test.getName());
                }
                System.out.println();
            }

            private static void classMethods(PsiClass clazz, Set<PsiMethod> unusedMethods) {
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> 2 class=" + clazz.getQualifiedName());
                for (PsiMethod test : unusedMethods) {
                    System.out.println("mUnusedMethods method=" + test.getName());
                }
                System.out.println();
            }

            private static void checkedMethod(PsiClass clazz, PsiMethod method) {
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> 3 class=" + clazz.getQualifiedName());
                System.out.println("checked method=" + method.getName());
                System.out.println();
            }

            private static void methodTypes(PsiClass clazz, List<WriteFieldGroupMethod> writeFieldGroupMethods, List<WriteFieldGroupMethod> functions) {
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> 4 class=" + clazz.getQualifiedName());
                for (WriteFieldGroupMethod test : writeFieldGroupMethods) {
                    System.out.println("write field method=" + test.getMethod().getName());
                }
                for (WriteFieldGroupMethod test : functions) {
                    System.out.println("function=" + test.getMethod().getName());
                }
                System.out.println();
            }

            private static void ownerMethod(PsiClass clazz) {
                if (!DEBUG_DETAIL) return;
                System.out.println("step -> 5 class=" + clazz.getQualifiedName());
            }
            private static void ownerMethod(WriteFieldGroupMethod ownerMethod) {
                if (!DEBUG_DETAIL) return;
                System.out.println("WriteFieldGroupMethods ownerMethod=" + ownerMethod.getMethod().getName());
            }
            private static void ownerMethod() {
                if (!DEBUG_DETAIL) return;
                System.out.println();
            }

            private static void sharedMethod(SharedWriteFieldGroup shared) {
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> shared leftMethod=" + shared.getLeftMethod().getMethod().getName()
                        + ", rightMethod=" + shared.getRightMethod().getMethod().getName()
                        + ", sharedPercentage=" + shared.getSharedPercentage());
            }

            private static void methodGroups(PsiClass clazz,
                                             Map<String, Set<WriteFieldGroupMethod>> methodsToSeparateByGroup,
                                             List<WriteFieldGroupMethod> methodsToSeparateBySingle,
                                             Map<String, Set<WriteFieldGroupMethod>> methodsTrySeparateByRole) {
                if (!DEBUG_DETAIL) return;

                System.out.println("step -> 7 class=" + clazz.getQualifiedName());
                for (Set<WriteFieldGroupMethod> itemSet : methodsToSeparateByGroup.values()) {
                    for (WriteFieldGroupMethod item : itemSet) {
                        System.out.println("MethodsToSeparateByGroup method=" + item.getMethod().getName());
                    }
                }
                for (WriteFieldGroupMethod item : methodsToSeparateBySingle) {
                    System.out.println("MethodsToSeparateBySingle=" + item.getMethod().getName());
                }
                for (Set<WriteFieldGroupMethod> itemSet : methodsTrySeparateByRole.values()) {
                    for (WriteFieldGroupMethod item : itemSet) {
                        System.out.println("MethodsTrySeparateByRole method=" + item.getMethod().getName());
                    }
                }
                System.out.println();
            }
        }
    }

    /**
     * メソッドが変更するフィールド変数(状態)グループのクラス
     */
    private static class WriteFieldGroupMethod {

        private PsiMethod mMethod;
        private Map<String, PsiField> mWriteFields;
        private String mWriteFieldsId;

        WriteFieldGroupMethod(@NonNull PsiMethod method, @NonNull Map<String,PsiField> allFields) {
            mMethod = method;
            mWriteFields = parse(allFields);
            mWriteFieldsId = createId(mWriteFields.values());
        }

        /**
         * メソッドを取得
         * @return メソッド
         */
        PsiMethod getMethod(){
            return mMethod;
        }

        /**
         * メソッドが変更するフィールド変数(状態)の提供
         * @return メソッドが変更するフィールド変数のセット
         */
        Map<String, PsiField> provideWriteFields(){
            Map<String, PsiField> provideWriteFields = new HashMap<>();
            provideWriteFields.putAll(mWriteFields);
            return provideWriteFields;
        }

        /**
         * メソッドが変更するフィールド変数(状態)の識別子を取得
         * @return メソッドが変更するフィールド変数の識別子
         * (変更するフィールド変数がない場合、空文字列が返ります)
         */
        String getWriteFieldsId(){
            return mWriteFieldsId;
        }

        /**
         * メソッドが変更するフィールド変数(状態)の有無を取得
         * @return メソッドが変更するフィールド変数の有無
         * (true:変更フィールド変数あり、false:変更フィールド変数なし)
         */
        boolean hasWriteFields(){
            return !mWriteFieldsId.isEmpty();
        }

        /**
         * メソッドが変更するフィールド変数(状態)を解析
         * @param allFields クラスが所持するすべてのフィールド
         * @return メソッドが変更する全てのフィールド
         */
        private Map<String, PsiField> parse(@NonNull Map<String, PsiField> allFields) {

            Map<String, PsiField> writeFields = new HashMap<>();

            // メソッド内の全代入式を抽出
            List<PsiAssignmentExpression> assignments = ElementUtil.extractAssignments(mMethod);
            for (PsiAssignmentExpression assignment : assignments) {

                // 左辺から変数名を取得
                List<PsiIdentifier> ids =
                        ElementUtil.extractIdentifiers(assignment.getLExpression());
                PsiIdentifier id = ids.get(ids.size()-1);

                if (allFields.containsKey(id.getText())) {

                    // フィールドと同名のクラス変数やインスタンス変数か否かをチェック
                    boolean isInstance = ElementUtil.isThisExpression(id) || ElementUtil.isRefExpression(id);

                    // フィールドと同名のローカル変数やパラメータか否かをチェック
                    PsiElement element =
                            ElementUtil.ScopeBacktrack
                                    .seek(assignment, allFields.get(id.getText()).getNameIdentifier());
                    if (element != null && !isInstance) continue;

                    writeFields.put(id.getText(), allFields.get(id.getText()));
                }
            }
            return writeFields;
        }
    }

    /**
     * 共有する変更フィールド変数(状態)グループのクラス
     */
    private static class SharedWriteFieldGroup {

        private WriteFieldGroupMethod mLeftMethod;
        private WriteFieldGroupMethod mRightMethod;
        private Map<String, PsiField> mSharedWriteFields;
        private String mSharedWriteFieldsId;
        private float mSharedPercentage;

        SharedWriteFieldGroup(
                @NonNull WriteFieldGroupMethod leftMethod,
                @NonNull WriteFieldGroupMethod rightMethod) {

            if (!leftMethod.hasWriteFields() || !rightMethod.hasWriteFields()) {
                throw new IllegalArgumentException(
                        "both parameter must be has a writing fields that a over one counts.");
            }

            mLeftMethod = leftMethod;
            mRightMethod = rightMethod;
            mSharedWriteFields = parse();
            mSharedWriteFieldsId = createId(mSharedWriteFields.values());
            mSharedPercentage = computeSharedPercentage();
        }

        /**
         * 左側のメソッド変更フィールド変数グループを取得
         * @return メソッド変更フィールド変数グループ
         */
        @NonNull WriteFieldGroupMethod getLeftMethod() {
            return mLeftMethod;
        }

        /**
         * 右側のメソッド変更フィールド変数グループを取得
         * @return メソッド変更フィールド変数グループ
         */
        @NonNull WriteFieldGroupMethod getRightMethod() {
            return mRightMethod;
        }

        /**
         * 左右のメソッドが共有する変更フィールド変数(状態)の提供
         * @return メソッドが変更するフィールド変数のセット
         */
        Map<String, PsiField> provideSharedWriteFields() {
            Map<String, PsiField> provideSharedWriteFields = new HashMap<>();
            provideSharedWriteFields.putAll(mSharedWriteFields);
            return provideSharedWriteFields;
        }

        /**
         * 共有する変更フィールド変数(状態)の識別子を取得
         * @return 共有する変更フィールド変数の識別子
         * (共有する変更フィールド変数がない場合、空文字列が返ります)
         */
        @NonNull String getSharedWriteFieldsId(){
            return mSharedWriteFieldsId;
        }

        /**
         * 共有する変更フィールド変数(状態)の共有度を取得
         * @return 共有する変更フィールド変数の共有度合
         * (共有する変更フィールド変数がない場合、0%が返ります)
         */
        float getSharedPercentage() {
            return mSharedPercentage;
        }

        /**
         * 左右のメソッドが共有する変更フィールド変数(状態)を解析
         * @return 左右のメソッドが共有する変更フィールド変数
         */
        private Map<String, PsiField> parse() {
            Map<String, PsiField> leftWriteFields = mLeftMethod.provideWriteFields();
            Map<String, PsiField> rightWriteFields = mRightMethod.provideWriteFields();

            Map<String, PsiField> sharedWriteFields = new HashMap<>();
            Collection<PsiField> leftValues = leftWriteFields.values();
            Collection<PsiField> rightValues = rightWriteFields.values();
            for (PsiField field : leftValues) {
                if (rightValues.contains(field)) {
                    sharedWriteFields.put(field.getName(), field);
                }
            }

            return sharedWriteFields;
        }

        /**
         * 左右のメソッドが共有する変更フィールド変数の共有度を計算
         * @return 左右のメソッドが共有する変更フィールド変数の共有度
         */
        private float computeSharedPercentage() {

            Map<String, PsiField>  leftWriteFields = mLeftMethod.provideWriteFields();
            Map<String, PsiField>  rightWriteFields = mRightMethod.provideWriteFields();

            float sharedCounts = mSharedWriteFields.size();
            float totalCounts = leftWriteFields.size() + rightWriteFields.size();

            if (sharedCounts == 0) return 0.0f;
            // コンストラクタ引数で、変更フィールド変数有無をチェックするため total が0になることはない。

            float sharedPercentage = (sharedCounts * 2.0f) / totalCounts;
            return sharedPercentage;
        }
    }

    /**
     * フィールド変数集合の識別子を生成する
     * @param fields フィルード変数集合
     * @return フィールド変数集合の識別子
     */
    private static String createId(Collection<PsiField> fields) {
        List<PsiField> fieldList = new ArrayList<>();
        fieldList.addAll(fields);

        fieldList.sort(mFieldIdComparator);
        StringBuilder sb = new StringBuilder();
        for (PsiField id : fieldList) {
            if (sb.length() != 0) sb.append(":");
            sb.append(Integer.toHexString(id.hashCode()));
        }
        return sb.toString();
    }

    /**
     * ハッシュコードを元にしたフィールド変数のコンパレータ
     */
    private static final Comparator<PsiField> mFieldIdComparator = new Comparator<PsiField>() {
        @Override
        public int compare(PsiField src, PsiField target) {
            return src.hashCode() == target.hashCode()
                    ? 0 : src.hashCode() > target.hashCode()
                    ? 1
                    : -1;

        }
    };
}