package com.cch_lab.java.android.lint.example;

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharingGroupClassificationDetectorTest extends LintDetectorTest {

    /**
     * The set of enabled issues for a given test.
     */
    private Set<Issue> mEnabled = new HashSet<Issue>();

    protected SharingGroupClassificationDetector getDetector() {
        return new SharingGroupClassificationDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE);
    }

    /**
     * Gets the configuration for the test.
     * Each test can have a set of enabled issues by assigning the member field {@link #mEnabled}.
     */
    @Override
    protected TestConfiguration getConfiguration(LintClient client, Project project) {
        return new TestConfiguration(client, project, null) {
            @Override
            public boolean isEnabled(@NonNull Issue issue) {
                return super.isEnabled(issue) && mEnabled.contains(issue);
            }
        };
    }

    public void testNoSharingGroup() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                            SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                            SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                            SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                            SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = "No warnings.";

        String result = lintProject(
                java(
                        "src/test/pkg/MultiLogicFlagTest.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "public class MultiLogicFlagTest {\n"
                                + "\n"
                                + "    private String message;\n"
                                + "    private boolean isSuccess;\n"
                                + "\n"
                                + "    public MultiLogicFlagTest(){\n"
                                + "    }\n"
                                + "\n"
                                + "    public void setup(boolean isSuccess) {\n"
                                + "        // 条件式にメソッドパラメータがある\n"
                                + "        if (isSuccess) {\n"
                                + "            message = \"success step.1\";\n"
                                + "        } else {\n"
                                + "            message = \"failed step.1\";\n"
                                + "        }\n"
                                + "        System.out.println(\"step.1 message -> \" + message);\n"
                                + "        \n"
                                + "        // 条件式にメソッドパラメータがある\n"
                                + "        if (isSuccess) \n"
                                + "            message = \"success step.2\";\n"
                                + "        else \n"
                                + "            message = \"failed step.2\";\n"
                                + "        System.out.println(\"step.2 message -> \" + message);\n"
                                + "        \n"
                                + "        // 条件式にメソッドパラメータがない\n"
                                + "        if (this.isSuccess) {\n"
                                + "            message = \"success step.3\";\n"
                                + "        } else {\n"
                                + "            message = \"failed step.3\";\n"
                                + "        }\n"
                                + "        System.out.println(\"step.3 message -> \" + message);\n"
                                + "        \n"
                                + "        // 条件式にメソッドパラメータがない\n"
                                + "        if (SubClass.isSuccess) {\n"
                                + "            message = \"success step.4\";\n"
                                + "        } else {\n"
                                + "            message = \"failed step.4\";\n"
                                + "        }\n"
                                + "        System.out.println(\"step.4 message -> \" + message);\n"
                                + "        \n"
                                + "        {\n"
                                + "            // フィールド変数を変更していない\n"
                                + "            String message;\n"
                                + "            if (isSuccess) {\n"
                                + "                message = \"success step.5\";\n"
                                + "            } else {\n"
                                + "                message = \"failed step.5\";\n"
                                + "            }\n"
                                + "            System.out.println(\"step.5 message -> \" + message);\n"
                                + "        }\n"
                                + "        \n"
                                + "    }\n"
                                + "\n"
                                + "    static class SubClass {\n"
                                + "\n"
                                + "        private static boolean isSuccess;\n"
                                + "        public SubClass(){\n"
                                + "        }\n"
                                + "\n"
                                + "    }\n"
                                + "\n"
                                + "}"
                )
        );

        assertEquals(expected, result);
    }

    public void testMixedGroup() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = ""
                + "src/test/pkg/MixedMethodsGreet.java:18: Warning: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立でも完全共有でもありません。\n"
                + "このメソッドが変更するフィールド変数(状態)は、他のメソッドとの間で共有の一部に過不足があります。\n"
                + "これはメソッドの責務(役割)が明確に区別されておらず、責務(役割)が混在していることを示すので、\n"
                + "先ずはメソッドや変更フィールド変数(状態)の分割や統合をおすすめします。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetName, mGreetTime} [MixedWriteFieldsClassification]\n"
                + "    private void init(String name) {\n"
                + "                 ~~~~\n"
                + "src/test/pkg/MixedMethodsGreet.java:26: Warning: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立でも完全共有でもありません。\n"
                + "このメソッドが変更するフィールド変数(状態)は、他のメソッドとの間で共有の一部に過不足があります。\n"
                + "これはメソッドの責務(役割)が明確に区別されておらず、責務(役割)が混在していることを示すので、\n"
                + "先ずはメソッドや変更フィールド変数(状態)の分割や統合をおすすめします。\n"
                + "fields={mGreetMessage, mGreetTime} [MixedWriteFieldsClassification]\n"
                + "    private void createGreet() {\n"
                + "                 ~~~~~~~~~~~\n"
                + "src/test/pkg/MixedMethodsGreet.java:47: Warning: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立でも完全共有でもありません。\n"
                + "このメソッドが変更するフィールド変数(状態)は、他のメソッドとの間で共有の一部に過不足があります。\n"
                + "これはメソッドの責務(役割)が明確に区別されておらず、責務(役割)が混在していることを示すので、\n"
                + "先ずはメソッドや変更フィールド変数(状態)の分割や統合をおすすめします。\n"
                + "fields={mGreetCount} [MixedWriteFieldsClassification]\n"
                + "    public void greet() {\n"
                + "                ~~~~~\n"
                + "0 errors, 3 warnings\n";

        String result = lintProject(
                java(
                        "src/test/pkg/MixedMethodsGreet.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "public class MixedMethodsGreet {\n"
                                + "\n"
                                + "    private String mGreetName;\n"
                                + "    private String mGreetMessage;\n"
                                + "    private long mGreetTime;\n"
                                + "    private int mGreetCount;\n"
                                + "\n"
                                + "    public MixedMethodsGreet(String name) {\n"
                                + "        init(name);\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更混在メソッド\n"
                                + "    private void init(String name) {\n"
                                + "        mGreetName = name;\n"
                                + "        mGreetMessage = \"\";\n"
                                + "        mGreetTime = 0L;\n"
                                + "        mGreetCount = 0;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更混在メソッド\n"
                                + "    private void createGreet() {\n"
                                + "        mGreetTime = System.currentTimeMillis();\n"
                                + "        long dayOffset = getDayOffset(mGreetTime);\n"
                                + "        if (dayOffset >= 0 && dayOffset < (3600000*6)) {\n"
                                + "            mGreetMessage = \"ZZZ...\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*6) && dayOffset < (3600000*10)) {\n"
                                + "            mGreetMessage = \"おはようございます。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*10) && dayOffset < (3600000*18)) {\n"
                                + "            mGreetMessage = \"こんにちは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*18) && dayOffset < (3600000*21)) {\n"
                                + "            mGreetMessage = \"こんばんは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*21) && dayOffset < (3600000*24)) {\n"
                                + "            mGreetMessage = \"おやすみなさい。\";\n"
                                + "        }\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更混在メソッド\n"
                                + "    public void greet() {\n"
                                + "        mGreetCount = mGreetCount + 1;\n"
                                + "        createGreet();\n"
                                + "        System.out.println(mGreetName+\"さん、\"+mGreetMessage+\" (\"+mGreetCount+\"回目の挨拶)\");\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    private static long getDayOffset(final long timeInMillis) {\n"
                                + "        long fixedTimestamp = timeInMillis + TIMEZONE_OFFSET_MILLISECOND;\n"
                                + "        long timestampOf12am = (fixedTimestamp / MILLISECOND_AT_A_DAY) * MILLISECOND_AT_A_DAY;\n"
                                + "        return fixedTimestamp - timestampOf12am;\n"
                                + "    }\n"
                                + "    private static final long MILLISECOND_AT_A_DAY = 24 * 3600 * 1000;\n"
                                + "    private static final int TIMEZONE_OFFSET_MILLISECOND = TimeZone.getDefault().getRawOffset();\n"
                                + "}\n"
                )
        );

        assertEquals(expected, result);
    }

    public void testShareAndSingleGroup() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = ""
                + "src/test/pkg/ShareAndSingleMethodsGreet.java:23: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有ですがクラス唯一でありません。\n"
                + "これはメソッドの責務(役割)が共有されていますが、責務混在したクラスであることを示すので、\n"
                + "他のメソッド createGreetTimeCountMessage() と、メソッドが変更するフィールド変数(状態)ごと新クラスに分離してください。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [SharingWriteFieldsClassification]\n"
                + "    private void initGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ShareAndSingleMethodsGreet.java:30: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有ですがクラス唯一でありません。\n"
                + "これはメソッドの責務(役割)が共有されていますが、責務混在したクラスであることを示すので、\n"
                + "他のメソッド initGreetTimeCountMessage() と、メソッドが変更するフィールド変数(状態)ごと新クラスに分離してください。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [SharingWriteFieldsClassification]\n"
                + "    private void createGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ShareAndSingleMethodsGreet.java:18: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立しています。\n"
                + "これはメソッドの責務(役割)が独立していることを示すので、\n"
                + "メソッドが変更するフィールド変数(状態)ごと新クラスに分離できます。\n"
                + "fields={mGreetName} [SingleWriteFieldsClassification]\n"
                + "    private void init(String name) {\n"
                + "                 ~~~~\n"
                + "0 errors, 3 warnings\n";

        String result = lintProject(
                java(
                        "src/test/pkg/ShareAndSingleMethodsGreet.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "public class ShareAndSingleMethodsGreet {\n"
                                + "\n"
                                + "    private String mGreetName;\n"
                                + "    private String mGreetMessage;\n"
                                + "    private long mGreetTime;\n"
                                + "    private int mGreetCount;\n"
                                + "\n"
                                + "    public ShareAndSingleMethodsGreet(String name) {\n"
                                + "        init(name);\n"
                                + "        initGreetTimeCountMessage();\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更独立メソッド\n"
                                + "    private void init(String name) {\n"
                                + "        mGreetName = name;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更共有メソッド（非唯一）\n"
                                + "    private void initGreetTimeCountMessage() {\n"
                                + "        mGreetMessage = \"\";\n"
                                + "        mGreetTime = 0L;\n"
                                + "        mGreetCount = 0;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更共有メソッド（非唯一）\n"
                                + "    private void createGreetTimeCountMessage() {\n"
                                + "        mGreetTime = System.currentTimeMillis();\n"
                                + "        long dayOffset = getDayOffset(mGreetTime);\n"
                                + "        if (dayOffset >= 0 && dayOffset < (3600000*6)) {\n"
                                + "            mGreetMessage = \"ZZZ...\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*6) && dayOffset < (3600000*10)) {\n"
                                + "            mGreetMessage = \"おはようございます。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*10) && dayOffset < (3600000*18)) {\n"
                                + "            mGreetMessage = \"こんにちは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*18) && dayOffset < (3600000*21)) {\n"
                                + "            mGreetMessage = \"こんばんは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*21) && dayOffset < (3600000*24)) {\n"
                                + "            mGreetMessage = \"おやすみなさい。\";\n"
                                + "        }\n"
                                + "        mGreetCount = mGreetCount + 1;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    public void greet() {\n"
                                + "        createGreetTimeCountMessage();\n"
                                + "        System.out.println(mGreetName+\"さん、\"+mGreetMessage+\" (\"+mGreetCount+\"回目の挨拶)\");\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    private static long getDayOffset(final long timeInMillis) {\n"
                                + "        long fixedTimestamp = timeInMillis + TIMEZONE_OFFSET_MILLISECOND;\n"
                                + "        long timestampOf12am = (fixedTimestamp / MILLISECOND_AT_A_DAY) * MILLISECOND_AT_A_DAY;\n"
                                + "        return fixedTimestamp - timestampOf12am;\n"
                                + "    }\n"
                                + "    private static final long MILLISECOND_AT_A_DAY = 24 * 3600 * 1000;\n"
                                + "    private static final int TIMEZONE_OFFSET_MILLISECOND = TimeZone.getDefault().getRawOffset();\n"
                                + "}\n"
                )
        );

        assertEquals(expected, result);
    }

    public void testOnlyShareGroup() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = ""
                + "src/test/pkg/OnlyShareMethodsGreet.java:18: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有かつクラス唯一です。\n"
                + "これはメソッドの責務(役割)が共有されていますが、責務独立したクラスであることを示すので、\n"
                + "他のメソッド createGreetTimeCountMessage() と共に、メソッドの利用先が適切であるか確認してください。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [IsolatedSharingWriteFieldsClassification]\n"
                + "    private void initGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/OnlyShareMethodsGreet.java:25: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有かつクラス唯一です。\n"
                + "これはメソッドの責務(役割)が共有されていますが、責務独立したクラスであることを示すので、\n"
                + "他のメソッド initGreetTimeCountMessage() と共に、メソッドの利用先が適切であるか確認してください。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [IsolatedSharingWriteFieldsClassification]\n"
                + "    private void createGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "0 errors, 2 warnings\n";

        String result = lintProject(
                java(
                        "src/test/pkg/OnlyShareMethodsGreet.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "public class OnlyShareMethodsGreet {\n"
                                + "\n"
                                + "    private String mGreetName;\n"
                                + "    private String mGreetMessage;\n"
                                + "    private long mGreetTime;\n"
                                + "    private int mGreetCount;\n"
                                + "\n"
                                + "    public OnlyShareMethodsGreet(String name) {\n"
                                + "        mGreetName = name;\n"
                                + "        initGreetTimeCountMessage();\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更共有メソッド（唯一）\n"
                                + "    private void initGreetTimeCountMessage() {\n"
                                + "        mGreetMessage = \"\";\n"
                                + "        mGreetTime = 0L;\n"
                                + "        mGreetCount = 0;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更共有メソッド（唯一）\n"
                                + "    private void createGreetTimeCountMessage() {\n"
                                + "        mGreetTime = System.currentTimeMillis();\n"
                                + "        long dayOffset = getDayOffset(mGreetTime);\n"
                                + "        if (dayOffset >= 0 && dayOffset < (3600000*6)) {\n"
                                + "            mGreetMessage = \"ZZZ...\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*6) && dayOffset < (3600000*10)) {\n"
                                + "            mGreetMessage = \"おはようございます。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*10) && dayOffset < (3600000*18)) {\n"
                                + "            mGreetMessage = \"こんにちは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*18) && dayOffset < (3600000*21)) {\n"
                                + "            mGreetMessage = \"こんばんは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*21) && dayOffset < (3600000*24)) {\n"
                                + "            mGreetMessage = \"おやすみなさい。\";\n"
                                + "        }\n"
                                + "        mGreetCount = mGreetCount + 1;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    public void greet() {\n"
                                + "        createGreetTimeCountMessage();\n"
                                + "        System.out.println(mGreetName+\"さん、\"+mGreetMessage+\" (\"+mGreetCount+\"回目の挨拶)\");\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    private static long getDayOffset(final long timeInMillis) {\n"
                                + "        long fixedTimestamp = timeInMillis + TIMEZONE_OFFSET_MILLISECOND;\n"
                                + "        long timestampOf12am = (fixedTimestamp / MILLISECOND_AT_A_DAY) * MILLISECOND_AT_A_DAY;\n"
                                + "        return fixedTimestamp - timestampOf12am;\n"
                                + "    }\n"
                                + "    private static final long MILLISECOND_AT_A_DAY = 24 * 3600 * 1000;\n"
                                + "    private static final int TIMEZONE_OFFSET_MILLISECOND = TimeZone.getDefault().getRawOffset();\n"
                                + "}\n"
            )
        );

        assertEquals(expected, result);
    }

    public void testSrpClass() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = "No warnings.";

        String result = lintProject(
                java(
                        "src/test/pkg/SrpMethodsGreet.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "public class SrpMethodsGreet {\n"
                                + "\n"
                                + "    private String mGreetName;\n"
                                + "    private String mGreetMessage;\n"
                                + "    private long mGreetTime;\n"
                                + "    private int mGreetCount;\n"
                                + "\n"
                                + "    public SrpMethodsGreet(String name) {\n"
                                + "        mGreetName = name;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更メソッド （唯一）\n"
                                + "    private void createGreetTimeCountMessage() {\n"
                                + "        mGreetTime = System.currentTimeMillis();\n"
                                + "        long dayOffset = getDayOffset(mGreetTime);\n"
                                + "        if (dayOffset >= 0 && dayOffset < (3600000*6)) {\n"
                                + "            mGreetMessage = \"ZZZ...\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*6) && dayOffset < (3600000*10)) {\n"
                                + "            mGreetMessage = \"おはようございます。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*10) && dayOffset < (3600000*18)) {\n"
                                + "            mGreetMessage = \"こんにちは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*18) && dayOffset < (3600000*21)) {\n"
                                + "            mGreetMessage = \"こんばんは。\";\n"
                                + "        } else\n"
                                + "        if (dayOffset >= (3600000*21) && dayOffset < (3600000*24)) {\n"
                                + "            mGreetMessage = \"おやすみなさい。\";\n"
                                + "        }\n"
                                + "        mGreetCount = mGreetCount + 1;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    public void greet() {\n"
                                + "        createGreetTimeCountMessage();\n"
                                + "        System.out.println(mGreetName+\"さん、\"+mGreetMessage+\" (\"+mGreetCount+\"回目の挨拶)\");\n"
                                + "    }\n"
                                + "\n"
                                + "    // 関数\n"
                                + "    private static long getDayOffset(final long timeInMillis) {\n"
                                + "        long fixedTimestamp = timeInMillis + TIMEZONE_OFFSET_MILLISECOND;\n"
                                + "        long timestampOf12am = (fixedTimestamp / MILLISECOND_AT_A_DAY) * MILLISECOND_AT_A_DAY;\n"
                                + "        return fixedTimestamp - timestampOf12am;\n"
                                + "    }\n"
                                + "    private static final long MILLISECOND_AT_A_DAY = 24 * 3600 * 1000;\n"
                                + "    private static final int TIMEZONE_OFFSET_MILLISECOND = TimeZone.getDefault().getRawOffset();\n"
                                + "}\n"
            )
        );

        assertEquals(expected, result);
    }
}
