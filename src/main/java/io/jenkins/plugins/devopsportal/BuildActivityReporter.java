package io.jenkins.plugins.devopsportal;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Build step of a project used to record a change of state of a development activity.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildActivityReporter extends Builder implements SimpleBuildStep {

    private String applicationName;
    private String applicationVersion;
    private String activity;
    private String status;

    @DataBoundConstructor
    public BuildActivityReporter(String applicationName, String applicationVersion, String activity, String status) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
        setActivity(activity);
        setStatus(status);
    }

    public String getApplicationName() {
        return applicationName;
    }

    @DataBoundSetter
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    @DataBoundSetter
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public BuildActivities getActivity() {
        return BuildActivities.valueOf(activity);
    }

    @DataBoundSetter
    public void setActivity(String activity) {
        this.activity = activity;
    }

    public BuildActivityStatus getStatus() {
        return BuildActivityStatus.valueOf(status);
    }

    @DataBoundSetter
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public void perform(@NonNull Run<?, ?> run, @NonNull FilePath workspace, @NonNull EnvVars env,
                        @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {

        if (StringUtils.trimToNull(applicationName) == null) {
            listener.getLogger().println("Unable to report build activity: application name is missing");
            return;
        }
        if (StringUtils.trimToNull(applicationVersion) == null) {
            listener.getLogger().println("Unable to report build activity: application version is missing");
            return;
        }
        if (activity == null) {
            listener.getLogger().println("Unable to report build activity: activity is missing");
            return;
        }
        if (!BuildActivities.exists(activity)) {
            listener.getLogger().println("Unable to report build activity: activity is invalid");
            return;
        }
        if (status == null) {
            listener.getLogger().println("Unable to report build activity: status is missing");
            return;
        }
        if (!BuildActivityStatus.exists(status)) {
            listener.getLogger().println("Unable to report build activity: status is invalid");
            return;
        }

        listener.getLogger().printf(
                "Report build activity '%s' to status '%s' for application '%s' version %s%n",
                activity,
                status,
                applicationName,
                applicationVersion
        );

        if (getBuildStatusDescriptor() == null) {
            return;
        }

        getBuildStatusDescriptor().update(applicationName, applicationVersion, record -> {
            record.setBuildJob(env.get("JOB_NAME"));
            record.setBuildNumber(env.get("BUILD_NUMBER"));
            record.setBuildURL(env.get("RUN_DISPLAY_URL"));
            record.setBuildBranch("");// TODO
            record.setBuildCommit("");//TODO
            record.setActivityStatus(activity, status);
        });

    }

    public BuildStatus.DescriptorImpl getBuildStatusDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildActivityReporter.DescriptorImpl.class);
    }

    @Symbol("reportBuildActivity")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckApplicationName(@QueryParameter String applicationName) {
            if (applicationName.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApplicationVersion(@QueryParameter String applicationVersion) {
            if (applicationVersion.trim().isEmpty()) {
                return FormValidation.error(Messages.FormValidation_Error_EmptyProperty());
            }
            return FormValidation.ok();
        }

        public ListBoxModel doFillActivityItems() {
            ListBoxModel list = new ListBoxModel();
            for (BuildActivities activity : BuildActivities.values()) {
                list.add(activity.getLabel(), activity.name());
            }
            return list;
        }

        public ListBoxModel doFillStatusItems() {
            ListBoxModel list = new ListBoxModel();
            for (BuildActivityStatus status : BuildActivityStatus.values()) {
                list.add(status.getLabel(), status.name());
            }
            return list;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildActivityReporter_DisplayName();
        }

    }

}
