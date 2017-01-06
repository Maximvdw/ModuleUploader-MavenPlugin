package be.maximvdw.modules;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "upload")
public class ModuleUploaderMojo extends AbstractMojo
{

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

    @Parameter(property = "moduleName", required = true)
    String moduleName;

    @Parameter(property = "moduleVersion", required = true)
    String moduleVersion;

    public void execute() throws MojoExecutionException
    {
        getLog().info( "Hello, world." + urlApi + " " + accessToken );
    }
}