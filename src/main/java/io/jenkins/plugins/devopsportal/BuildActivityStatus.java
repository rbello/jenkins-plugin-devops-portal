package io.jenkins.plugins.devopsportal;

import hudson.model.Result;

/**
 * List of the different states of build activities.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public enum BuildActivityStatus {

    PENDING("Pending"),
    DONE("Done"),
    UNSTABLE("Unstable"),
    FAIL("Fail");

    private final String label;

    BuildActivityStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static BuildActivityStatus getDefault() {
        return PENDING;
    }

    public static BuildActivityStatus fromResult(Result result) {
        if (result == null) return PENDING;
        if (result == Result.ABORTED) return FAIL;
        if (result == Result.FAILURE) return FAIL;
        if (result == Result.SUCCESS) return DONE;
        if (result == Result.NOT_BUILT) return PENDING;
        if (result == Result.UNSTABLE) return UNSTABLE;
        return PENDING;
    }

    public static boolean exists(String activity) {
        try {
            BuildActivityStatus.valueOf(activity);
            return true;
        }
        catch (Exception ex) {
            return false;
        }
    }

}
