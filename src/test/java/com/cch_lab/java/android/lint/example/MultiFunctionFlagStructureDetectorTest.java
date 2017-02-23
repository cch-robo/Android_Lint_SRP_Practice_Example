package com.cch_lab.java.android.lint.example;

import com.android.annotations.NonNull;
import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import com.android.tools.lint.client.api.LintClient;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Project;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiFunctionFlagStructureDetectorTest extends LintDetectorTest {

    /**
     * The set of enabled issues for a given test.
     */
    private Set<Issue> mEnabled = new HashSet<Issue>();

    protected MultiFunctionFlagStructureDetector getDetector() {
        return new MultiFunctionFlagStructureDetector();
    }

    @Override
    protected List<Issue> getIssues() {
        return Collections.singletonList(MultiFunctionFlagStructureDetector.ISSUE);
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

    public void testMultiFunctionSource() throws Exception {
        mEnabled = Collections.singleton(MultiFunctionFlagStructureDetector.ISSUE);
        String expected = ""
                + "src/test/pkg/MultiLogicFlagTest.java:13: Warning: "
                + "メソッドのbooleanパラメータで、クラスの状態変更を切り替えています。\n"
                + "多重責務(役割)のメソッドは、テスタビリティを下げるので、ifブロック処理を別メソッドに分離することをおすすめします。 [MultiFunctionFlagStructure]\n"
                + "        if (isSuccess) {\n"
                + "        ^\n"
                + "src/test/pkg/MultiLogicFlagTest.java:21: Warning: "
                + "メソッドのbooleanパラメータで、クラスの状態変更を切り替えています。\n"
                + "多重責務(役割)のメソッドは、テスタビリティを下げるので、ifブロック処理を別メソッドに分離することをおすすめします。 [MultiFunctionFlagStructure]\n"
                + "        if (isSuccess) \n"
                + "        ^\n"
                + "0 errors, 2 warnings\n";

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

}
