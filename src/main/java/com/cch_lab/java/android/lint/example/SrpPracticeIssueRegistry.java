package com.cch_lab.java.android.lint.example;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

public class SrpPracticeIssueRegistry extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                SharingGroupClassificationDetector.SEPARATE_BY_ISOLATED_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_GROUP_ISSUE,
                SharingGroupClassificationDetector.SEPARATE_BY_SINGLE_ISSUE,
                SharingGroupClassificationDetector.TRY_SEPARATE_BY_ROLE_ISSUE);
    }
}
