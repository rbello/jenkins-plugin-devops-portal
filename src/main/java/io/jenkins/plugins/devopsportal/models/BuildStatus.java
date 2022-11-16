package io.jenkins.plugins.devopsportal.models;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.util.CopyOnWriteList;
import io.jenkins.plugins.devopsportal.Messages;
import io.jenkins.plugins.devopsportal.utils.JenkinsUtils;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * A persistent record of the progress of build activities for a software release.
 *
 * @author Rémi BELLO {@literal <remi@evolya.fr>}
 */
public class BuildStatus implements Describable<BuildStatus>, Serializable {

    private String applicationName;
    private String applicationVersion;

    private String buildJob;
    private String buildNumber;
    private String buildURL;
    private String buildBranch;
    private String buildCommit;
    private long buildTimestamp; // seconds

    /*private final List<BuildActivity> buildActivities;
    private final List<UnitTestActivity> unitTestActivities;
    private final List<QualityAuditActivity> qualityAuditActivities;
    private final List<PerformanceTestActivity> performanceTestActivities;
    private final List<ImageReleaseActivity> imageReleaseActivities;*/

    private final Map<ActivityCategory, List<AbstractActivity>> activities;

    @DataBoundConstructor
    public BuildStatus() {
        /*this.buildActivities = new ArrayList<>();
        this.unitTestActivities = new ArrayList<>();
        this.qualityAuditActivities = new ArrayList<>();
        this.performanceTestActivities = new ArrayList<>();
        this.imageReleaseActivities = new ArrayList<>();*/
        activities = new HashMap<>();
        for (ActivityCategory category : ActivityCategory.values()) {
            activities.put(category, new ArrayList<>());
        }
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

    public String getBuildJob() {
        return buildJob;
    }

    @DataBoundSetter
    public void setBuildJob(String buildJob) {
        this.buildJob = buildJob;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    @DataBoundSetter
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBuildURL() {
        return buildURL;
    }

    @DataBoundSetter
    public void setBuildURL(String buildURL) {
        this.buildURL = buildURL;
    }

    public String getBuildBranch() {
        return buildBranch;
    }

    @DataBoundSetter
    public void setBuildBranch(String buildBranch) {
        this.buildBranch = buildBranch;
    }

    public String getBuildCommit() {
        return buildCommit;
    }

    @DataBoundSetter
    public void setBuildCommit(String buildCommit) {
        this.buildCommit = buildCommit;
    }

    public long getBuildTimestamp() {
        return buildTimestamp;
    }

    @DataBoundSetter
    public void setBuildTimestamp(long buildTimestamp) {
        this.buildTimestamp = buildTimestamp;
    }

    public Map<ActivityCategory, List<AbstractActivity>> getActivities() {
        return activities;
    }

    /*
        public List<BuildActivity> getBuildActivities() {
            return buildActivities;
        }

        public void addBuildActivity(BuildActivity buildActivity) {
            if (buildActivity != null) {
                this.buildActivities.add(buildActivity);
            }
        }

        public List<UnitTestActivity> getUnitTestActivities() {
            return unitTestActivities;
        }

        public void addUnitTestActivity(UnitTestActivity unitTestActivity) {
            if (unitTestActivity != null) {
                this.unitTestActivities.add(unitTestActivity);
            }
        }

        public List<QualityAuditActivity> getQualityAuditActivities() {
            return qualityAuditActivities;
        }

        public void addQualityAuditActivity(QualityAuditActivity qualityAuditActivity) {
            if (qualityAuditActivity != null) {
                this.qualityAuditActivities.add(qualityAuditActivity);
            }
        }

        public List<PerformanceTestActivity> getPerformanceTestActivities() {
            return performanceTestActivities;
        }

        public void addPerformanceTestActivity(PerformanceTestActivity performanceTestActivity) {
            if (performanceTestActivity != null) {
                this.performanceTestActivities.add(performanceTestActivity);
            }
        }

        public List<ImageReleaseActivity> getImageReleaseActivities() {
            return imageReleaseActivities;
        }

        @DataBoundSetter
        public void addImageReleaseActivity(ImageReleaseActivity imageReleaseActivity) {
            if (imageReleaseActivity != null) {
                this.imageReleaseActivities.add(imageReleaseActivity);
            }
        }
    */
    public boolean isBuildBranchPresent() {
        return buildBranch != null && !buildBranch.isEmpty();
    }

    public boolean isBuildCommitPresent() {
        return buildCommit != null && !buildCommit.isEmpty();
    }

    @Override
    public Descriptor<BuildStatus> getDescriptor() {
        return Jenkins.get().getDescriptorByType(BuildStatus.DescriptorImpl.class);
    }

    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;
        final BuildStatus other = (BuildStatus) that;
        return new EqualsBuilder()
                .append(applicationName, other.applicationName)
                .append(applicationVersion, other.applicationVersion)
                .append(buildBranch, other.buildBranch)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(applicationName)
                .append(applicationVersion)
                .append(buildJob)
                .append(buildNumber)
                .append(buildURL)
                .append(buildBranch)
                .append(buildCommit)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append(applicationName)
                .append(applicationVersion)
                .append(buildJob)
                .append(buildNumber)
                .append(buildURL)
                .append(buildBranch)
                .append(buildCommit)
                .toString();
    }

    public String getBuildStatusClass() {
        Run<?, ?> job = JenkinsUtils.getBuild(buildJob, buildBranch, buildNumber).orElse(null);
        if (job != null) {
            return job.getBuildStatusIconClassName();
        }
        return "icon-disabled";
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractActivity> void updateActivity(String applicationComponent, ActivityCategory category, Consumer<T> updater) {
        T activity = (T) activities.getOrDefault(category, new ArrayList<>())
                .stream()
                .filter(item -> item.getApplicationComponent().equals(applicationComponent))
                .findFirst()
                .orElse(null);
        if (activity == null) {
            try {
                assert category.getClazz() != null;
                activity = (T) category.getClazz().getConstructor(String.class).newInstance(applicationComponent);
                activities.get(category).add(activity);
            }
            catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        updater.accept(activity);
        getDescriptor().save();
    }

    public List<AbstractActivity> getActivitiesByCategory(ActivityCategory category) {
        return this.activities.get(category);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildStatus> implements Serializable {

        private final CopyOnWriteList<BuildStatus> buildStatus = new CopyOnWriteList<>();

        public DescriptorImpl() {
            super(BuildStatus.class);
            load();
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.BuildStatus_DisplayName();
        }

        public List<BuildStatus> getBuildStatus() {
            List<BuildStatus> retVal = new ArrayList<>(buildStatus.getView());
            retVal.sort(Comparator.comparing(BuildStatus::getApplicationName));
            return retVal;
        }

        public void update(String applicationName, String applicationVersion, Consumer<BuildStatus> updater) {
            BuildStatus status = buildStatus
                    .getView()
                    .stream()
                    .filter(item -> applicationName.trim().equals(item.getApplicationName()))
                    .filter(item -> applicationVersion.trim().equals(item.getApplicationVersion()))
                    .findFirst()
                    .orElse(null);
            if (status == null) {
                status = new BuildStatus();
                status.setApplicationName(applicationName.trim());
                status.setApplicationVersion(applicationVersion.trim());
                buildStatus.add(status);
            }
            updater.accept(status);
            status.setBuildTimestamp(Instant.now().getEpochSecond());
            save();
        }

        public boolean isApplicationExists(String applicationName) {
            return getBuildStatus().stream().anyMatch(item -> applicationName.equals(item.getApplicationName()));
        }

    }

}