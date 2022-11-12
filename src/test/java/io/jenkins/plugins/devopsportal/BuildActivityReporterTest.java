package io.jenkins.plugins.devopsportal;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class BuildActivityReporterTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    final String applicationName = "My Application";
    final String applicationVersion = "1.0.3";
    final String activity = "BUILD";

    @Test
    public void testConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new BuildActivityReporter(applicationName, applicationVersion, activity, "DONE"));
        project = jenkins.configRoundtrip(project);
        jenkins.assertEqualDataBoundBeans(
            new BuildActivityReporter(applicationName, applicationVersion, activity, "DONE"),
            project.getBuildersList().get(0)
        );
    }

    @Test
    public void testConfigRoundtripStatus() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter builder = new BuildActivityReporter(applicationName, applicationVersion, activity, "FAIL");
        project.getBuildersList().add(builder);
        project = jenkins.configRoundtrip(project);

        BuildActivityReporter lhs = new BuildActivityReporter(applicationName, applicationVersion, activity, "FAIL");
        jenkins.assertEqualDataBoundBeans(lhs, project.getBuildersList().get(0));
    }

    @Test
    public void testBuildStatusManual() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        BuildActivityReporter builder = new BuildActivityReporter(applicationName, applicationVersion, "UT", "UNSTABLE");
        project.getBuildersList().add(builder);

        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        jenkins.assertLogContains("Report build activity 'UT' to status 'UNSTABLE' for application 'My Application' version 1.0.3", build);
    }

    @Test
    public void testScriptedPipeline() throws Exception {
        String agentLabel = "my-agent";
        jenkins.createOnlineSlave(Label.get(agentLabel));
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "test-scripted-pipeline");
        String pipelineScript
                = "node {\n"
                //+ "  reportBuildActivity(applicationName: '" + applicationName + "', applicationVersion: '"
                //+ applicationVersion + "', activity: '" + activity + "', status: 'DONE')\n"
                + "  reportBuildActivity '" + applicationName + "', '" + applicationVersion + "', '" + activity + "', 'DONE'\n"
                + "}";
        job.setDefinition(new CpsFlowDefinition(pipelineScript, true));
        WorkflowRun completedBuild = jenkins.assertBuildStatusSuccess(job.scheduleBuild2(0));
        String expectedString = "OK TODO";
        jenkins.assertLogContains(expectedString, completedBuild);
    }

}