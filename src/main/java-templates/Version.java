package com.pearson.statsagg.controller;

public final class Version {
    private static final String VERSION = "${project.version}";
    private static final String GROUP_ID = "${project.groupId}";
    private static final String ARTIFACT_ID = "${project.artifactId}";
    private static final String BUILD_TIMESTAMP = "${timestamp}";

    public static String getProjectVersion() {
        return VERSION;
    }
    
    public static String getProjectGroupId() {
        return GROUP_ID;
    }
    
    public static String getProjectArtifactId() {
        return ARTIFACT_ID;
    }
    
    public static String getBuildTimestamp() {
        return BUILD_TIMESTAMP;
    }
    
}

