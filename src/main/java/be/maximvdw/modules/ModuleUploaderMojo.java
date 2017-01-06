package be.maximvdw.modules;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "update")
public class ModuleUploaderMojo extends AbstractMojo {

    /**
     * URL of the Modules API
     */
    @Parameter(property = "urlApi", required = false, defaultValue = "http://modules.mvdw-software.com/api/v1")
    String urlApi;


    /**
     * Access token of the project or module
     */
    @Parameter(property = "accessToken", required = true)
    String accessToken;

    @Parameter(property = "projectId", required = true)
    int projectId;

    @Parameter(property = "moduleName", required = true)
    String moduleName;

    @Parameter(property = "moduleAuthor", required = true)
    String moduleAuthor;

    @Parameter(property = "moduleDescription", required = true)
    String moduleDescription;

    @Parameter(property = "moduleVersion", required = true)
    String moduleVersion;

    public void execute() throws MojoExecutionException {
        getLog().info("MVdW-Software Module Uploader");
        getLog().info("Using API: " + urlApi);
        getLog().info("Project ID: " + projectId);
        getLog().info("Module name: " + moduleName);
        getLog().info("Module author: " + moduleAuthor);
        getLog().info("Module description: " + moduleDescription);
        getLog().info("Module version: " + moduleVersion);
    }
}