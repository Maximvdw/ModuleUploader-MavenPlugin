package be.maximvdw.modules;

import be.maximvdw.modules.http.HttpMethod;
import be.maximvdw.modules.http.HttpRequest;
import be.maximvdw.modules.http.HttpResponse;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.net.URLEncoder;

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

    /**
     * Project Artifact.
     */
    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;

    public void execute() throws MojoExecutionException {
        getLog().info("MVdW-Software Module Uploader");
        getLog().info("Using API: " + urlApi);
        getLog().info("Project ID: " + projectId);
        getLog().info("Module name: " + moduleName);
        getLog().info("Module author: " + moduleAuthor);
        getLog().info("Module description: " + moduleDescription);
        getLog().info("Module version: " + moduleVersion);

        getLog().info("Getting module id from name ...");
        long moduleId = getModuleId();
        if (moduleId == -1) {
            getLog().info("Creating a new module!");
            moduleId = createModule();
        }
        getLog().info("Module id: " + moduleId);

        File projectFile = artifact.getFile();
        if (uploadFile(moduleId,projectFile)){
            getLog().info("Module upload success!");
        }
    }

    public Long getModuleId() {
        try {
            String url = urlApi + "/module/" + projectId + "/fromName/" + URLEncoder.encode(moduleName, "UTF-8");
            getLog().info("Sending GET request to: " + url);
            HttpResponse response = new HttpRequest(url)
                    .execute();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(response.getSource());
            if (responseJson.containsKey("module")) {
                return (Long) ((JSONObject) responseJson.get("module")).get("id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1L;
    }

    public Long createModule() {
        try {
            String url = urlApi + "/module/" + projectId + "/create";
            getLog().info("Sending POST request to: " + url);
            HttpResponse response = new HttpRequest(url)
                    .post("name", moduleName)
                    .post("author", moduleAuthor)
                    .post("description", moduleDescription)
                    .authorization(accessToken)
                    .method(HttpMethod.POST)
                    .execute();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(response.getSource());
            if (responseJson.containsKey("module")) {
                return (Long) ((JSONObject) responseJson.get("module")).get("id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1L;
    }

    public boolean uploadFile(long moduleId, File file) {
        try {
            String url = urlApi + "/module/" + projectId + "/" + moduleId + "/update";
            getLog().info("Sending POST request to: " + url);
            HttpResponse response = new HttpRequest(url)
                    .post("version", moduleVersion)
                    .authorization(accessToken)
                    .withFile("file", file)
                    .method(HttpMethod.POST)
                    .execute();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(response.getSource());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}