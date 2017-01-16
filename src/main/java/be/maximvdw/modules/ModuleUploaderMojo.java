package be.maximvdw.modules;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

@Mojo(name = "update")
public class ModuleUploaderMojo extends AbstractMojo {

    /**
     * URL of the Modules API
     */
    @Parameter(property = "urlApi", defaultValue = "http://modules.mvdw-software.com/api/v1")
    String urlApi;

    /**
     * Access token of the project or module
     */
    @Parameter(property = "accessToken", required = true)
    String accessToken;

    @Parameter(property = "projectId")
    String projectId;

    @Parameter(property = "projectName")
    String projectName;

    @Parameter(property = "moduleName")
    String moduleName;

    @Parameter(property = "moduleId")
    String moduleId;

    @Parameter(property = "moduleAuthor", required = true)
    String moduleAuthor;

    @Parameter(property = "moduleDescription", required = true)
    String moduleDescription;

    @Parameter(property = "moduleVersion", required = true)
    String moduleVersion;

    @Parameter(property = "permalink")
    String permalink;

    @Parameter(property = "screenshots")
    String[] screenshots;

    @Parameter(property = "videos")
    String[] videos;

    @Parameter(property = "constraints")
    Properties constraints;

    /**
     * Project Artifact.
     */
    @Parameter(defaultValue = "${project.artifact}", readonly = true, required = true)
    private Artifact artifact;

    public void execute() throws MojoExecutionException {
        getLog().info("MVdW-Software Module Uploader");
        getLog().info("Using API: " + urlApi);

        if (projectName != null) {
            getLog().info("Getting project id from name ...");
            projectId = getProjectId();
        }

        getLog().info("Getting module id from name ...");
        moduleId = getModuleId();
        if (moduleId == null) {
            getLog().info("Creating a new module!");
            moduleId = createModule();
        }
        getLog().info("Module id: " + moduleId);

        File projectFile = artifact.getFile();
        if (uploadFile(moduleId, projectFile)) {
            getLog().info("Module upload success!");
        }
    }

    public String getProjectId() {
        try {
            String url = urlApi + "/project/fromName/" + URLEncoder.encode(projectName, "UTF-8");
            getLog().info("Sending GET request to: " + url);
            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(document.text());
            if (responseJson.containsKey("project")) {
                return (String) ((JSONObject) responseJson.get("project")).get("id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String getModuleId() {
        try {
            String url = urlApi + "/module/" + projectId + "/fromName/" + URLEncoder.encode(moduleName, "UTF-8");
            getLog().info("Sending GET request to: " + url);
            Document document = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(document.text());
            if (responseJson.containsKey("module")) {
                return (String) ((JSONObject) responseJson.get("module")).get("id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String createModule() {
        try {
            String url = urlApi + "/project/" + projectId + "/createModule";
            getLog().info("Sending POST request to: " + url);
            Connection connection = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .data("name", moduleName)
                    .data("author", moduleAuthor)
                    .data("description", moduleDescription)
                    .data("permalink",permalink)
                    .header("Authorization", accessToken);

            if (screenshots != null) {
                for (String screenshot : screenshots) {
                    connection.data("screenshots[]", screenshot);
                }
            }
            if (videos != null) {
                for (String video : videos) {
                    connection.data("videos[]", video);
                }
            }

            Document document = connection.post();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(document.text());
            if (responseJson.containsKey("module")) {
                return (String) ((JSONObject) responseJson.get("module")).get("id");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean uploadFile(String moduleId, File file) {
        try {
            String url = urlApi + "/module/" + moduleId + "/update";
            getLog().info("Sending POST request to: " + url);
            Connection connection = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .data("name", moduleName)
                    .data("author", moduleAuthor)
                    .data("description", moduleDescription)
                    .data("permalink",permalink)
                    .data("version", moduleVersion)
                    .data("changes", "test")
                    .data("file", file.getName(), new FileInputStream(file))
                    .header("Authorization", accessToken);

            if (screenshots != null) {
                for (String screenshot : screenshots) {
                    connection.data("screenshots[]", screenshot);
                }
            }
            if (videos != null) {
                for (String video : videos) {
                    connection.data("videos[]", video);
                }
            }
            if (constraints != null){
                for (Map.Entry<Object,Object> prop : constraints.entrySet()){
                    String data = URLEncoder.encode(prop.getKey() + "=" + prop.getValue(),"UTF-8");
                    connection.data("constraints[]",data);
                }
            }

            Document document = connection.post();
            JSONParser parser = new JSONParser();
            JSONObject responseJson = (JSONObject) parser.parse(document.text());
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}