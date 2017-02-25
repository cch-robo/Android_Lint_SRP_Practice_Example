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
                + "他のメソッド createGreetTimeCountMessage() と、メソッドが変更するフィールド変数(状態)ごと新クラスへの分離をおすすめします。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [SharingWriteFieldsClassification]\n"
                + "    private void initGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ShareAndSingleMethodsGreet.java:30: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有ですがクラス唯一でありません。\n"
                + "これはメソッドの責務(役割)が共有されていますが、責務混在したクラスであることを示すので、\n"
                + "他のメソッド initGreetTimeCountMessage() と、メソッドが変更するフィールド変数(状態)ごと新クラスへの分離をおすすめします。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [SharingWriteFieldsClassification]\n"
                + "    private void createGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/ShareAndSingleMethodsGreet.java:18: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全独立しています。\n"
                + "これはメソッドの責務(役割)が独立していることを示すので、\n"
                + "メソッドが変更するフィールド変数(状態)ごと新クラスへの分離をおすすめします。\n"
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
                                + "    // 状態変更要求メソッド\n"
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
                + "これはメソッドの責務(役割)が共有されている、責務合体したクラスであることを示すので、\n"
                + "他のメソッド createGreetTimeCountMessage() と共に、メソッドの利用先が適切であるかを確認して、変更するフイールド変数(状態)の分割をおすすめします。\n"
                + "fields={mGreetMessage, mGreetCount, mGreetTime} [IsolatedSharingWriteFieldsClassification]\n"
                + "    private void initGreetTimeCountMessage() {\n"
                + "                 ~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "src/test/pkg/OnlyShareMethodsGreet.java:25: Information: メソッドが変更するフィールド変数(状態)は、他のメソッドと完全共有かつクラス唯一です。\n"
                + "これはメソッドの責務(役割)が共有されている、責務合体したクラスであることを示すので、\n"
                + "他のメソッド initGreetTimeCountMessage() と共に、メソッドの利用先が適切であるかを確認して、変更するフイールド変数(状態)の分割をおすすめします。\n"
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
                                + "    // 状態変更要求メソッド\n"
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
                        "src/test/pkg/SrpClassGreet.java",
                        ""
                                + "package test.pkz;\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "public class SrpClassGreet {\n"
                                + "\n"
                                + "    private Person mGreetPerson;\n"
                                + "    private Counts mGreetCount;\n"
                                + "    private GreetingWords mGreetingWords;\n"
                                + "\n"
                                + "    public SrpClassGreet(String name) {\n"
                                + "        mGreetPerson = new Person(name);\n"
                                + "        mGreetCount = new Counts();\n"
                                + "        mGreetingWords = new GreetingWords();\n"
                                + "    }\n"
                                + "\n"
                                + "    // 外部の状態変更要求メソッド\n"
                                + "    private void updateGreet() {\n"
                                + "        mGreetCount.addCounts();\n"
                                + "        mGreetingWords.createGreetingWords();\n"
                                + "    }\n"
                                + "\n"
                                + "    // 外部の状態変更要求＆状態参照メソッド\n"
                                + "    public void greet() {\n"
                                + "        updateGreet();\n"
                                + "        System.out.println(mGreetPerson.getName()+\"さん、\"\n"
                                + "                +mGreetingWords.getGreetingWords()\n"
                                + "                +\" (\"+mGreetCount.getCounts()+\"回目の挨拶)\");\n"
                                + "    }\n"
                                + "}\n"
                                + "\n"
                                + "// 挨拶する人を別クラスに分離\n"
                                + "class Person {\n"
                                + "    private String mName;\n"
                                + "\n"
                                + "    Person(String name) {\n"
                                + "        mName = name;\n"
                                + "    }\n"
                                + "\n"
                                + "    String getName() {\n"
                                + "        return mName;\n"
                                + "    }\n"
                                + "}\n"
                                + "\n"
                                + "// 挨拶回数を別クラスに分離\n"
                                + "class Counts {\n"
                                + "    private int mCounts;\n"
                                + "\n"
                                + "    Counts() {\n"
                                + "        mCounts = 0;\n"
                                + "    }\n"
                                + "\n"
                                + "    int getCounts() {\n"
                                + "        return mCounts;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更メソッド（唯一）\n"
                                + "    void addCounts() {\n"
                                + "        mCounts = mCounts + 1;\n"
                                + "    }\n"
                                + "}\n"
                                + "\n"
                                + "// 挨拶の言葉を別クラスに分離\n"
                                + "class GreetingWords {\n"
                                + "    private long mGreetTime;\n"
                                + "    private String mGreetMessage;\n"
                                + "\n"
                                + "    GreetingWords() {\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態変更メソッド（唯一）\n"
                                + "    void createGreetingWords() {\n"
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
                                + "    // 状態参照メソッド\n"
                                + "    String getGreetingWords(){\n"
                                + "        return mGreetMessage;\n"
                                + "    }\n"
                                + "\n"
                                + "    // 状態参照メソッド\n"
                                + "    long getGreetingTime(){\n"
                                + "        return mGreetTime;\n"
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

    public void testGodMethod() throws Exception {
        mEnabled.clear();
        mEnabled.addAll(Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE));
        String expected = "No warnings.";

        /*
        今後の課題
        実装構造的には、状態変更メソッドがクラスに唯一のため単一責務に見えますが、
        論理的な責務分割がされていない、神メソッドの区別まではしていません。
         */
        String result = lintProject(
                java(
                        "src/test/pkg/GodMethodGreet.java",
                        ""
                                + "package test.pkg;\n"
                                + "\n"
                                + "import java.util.TimeZone;\n"
                                + "\n"
                                + "// 状態変更は唯一だが神メソッドのクラス\n"
                                + "public class GodMethodGreet {\n"
                                + "\n"
                                + "    private String mGreetName;\n"
                                + "    private String mGreetMessage;\n"
                                + "    private long mGreetTime;\n"
                                + "    private int mGreetCount;\n"
                                + "\n"
                                + "    public GodMethodGreet(String name) {\n"
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
                                + "    // 状態変更要求メソッド\n"
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
