package be.maximvdw.modules.http;

import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpRequest
 * <p>
 * Created by maxim on 25-Dec-16.
 */
public class HttpRequest {
    private URL url = null;
    private HttpMethod httpMethod = HttpMethod.GET;
    private Map<String, String> postData = new HashMap<>();
    private String postBody = "";
    private Map<String, String> headers = new HashMap<>();
    private String uploadFileName = "";
    private File uploadFile = null;

    public HttpRequest(String url) throws MalformedURLException {
        this.url = new URL(url);
        userAgent("Mozilla/5.0");
    }

    public HttpRequest post(String newKey, String newValue) {
        postData.put(newKey, newValue);
        // Construct body
        postBody = "";
        for (String key : postData.keySet()) {
            String value = postData.get(key);
            postBody += key + "=" + value + "&";
        }
        postBody = postBody.substring(0, postBody.length() - 1);
        return this;
    }

    public HttpRequest post(String data) {
        this.postBody = data;
        return this;
    }

    public HttpRequest method(HttpMethod method) {
        this.httpMethod = method;
        return this;
    }

    public HttpRequest authorization(String authorization) {
        this.headers.put("Authorization", authorization);
        return this;
    }

    public HttpRequest contentType(String contentType) {
        this.headers.put("Content-Type", contentType);
        return this;
    }

    public HttpRequest userAgent(String userAgent) {
        this.headers.put("User-Agent", userAgent);
        return this;
    }

    public HttpRequest withFile(String name,File file){
        this.uploadFile = file;
        this.uploadFileName = name;
        return this;
    }

    public HttpResponse execute() throws IOException {
        HttpURLConnection con = null;
        try {
            if (url.getProtocol().toLowerCase().equals("https")) {
                con = (HttpsURLConnection) url.openConnection();
            } else {
                con = (HttpURLConnection) url.openConnection();
            }

            // Set request method
            con.setRequestMethod(httpMethod.name());

            for (Map.Entry<String, String> header : headers.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            if (this.uploadFile != null){
                con.setUseCaches(false);
                con.setDoOutput(true);
                con.setRequestProperty("Connection", "Keep-Alive");
                con.setRequestProperty("Cache-Control", "no-cache");
                con.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=*****");

                DataOutputStream request = new DataOutputStream(
                        con.getOutputStream());

                request.writeBytes("--*****\r\n");
                request.writeBytes("Content-Disposition: form-data; name=\"" +
                        this.getUploadFileName() + "\";filename=\"" +
                        this.getUploadFile().getName() + "\"\r\n");
                request.writeBytes("\r\n");
                Path path = Paths.get(getUploadFile().getAbsolutePath());
                byte[] data = Files.readAllBytes(path);
                request.write(data);
                request.writeBytes("\r\n");
                request.writeBytes("--*****--\r\n");
                request.flush();
                request.close();
            }
            if (!postBody.equals("")) {
                con.setDoOutput(true);
                // Send post request
                final BufferedOutputStream outputStream = new BufferedOutputStream(con.getOutputStream());
                writeAll(postBody, outputStream, Charset.defaultCharset());
                outputStream.close();
            }

            InputStream inputStream = new BufferedInputStream(con.getInputStream());

            String[] cookies = new String[0];
            if (con.getHeaderField("Set-Cookie") != null)
                cookies = con.getHeaderField("Set-Cookie").split(";");

            String response = readAll(inputStream, Charset.defaultCharset());
            inputStream.close();

            return new HttpResponse(response, con.getResponseCode(), cookies);
        } catch (final IOException e) {
            if (con != null) {
                final InputStream errorStream = con.getErrorStream();
                if (errorStream != null) {
                    String errorString = readAll(errorStream, Charset.defaultCharset());
                    return new HttpResponse(errorString, con.getResponseCode(), new String[0]);
                } else {
                    throw new IOException();
                }
            }
        }
        return null;
    }

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    /**
     * Write string to byte stream
     *
     * @param data         Source string
     * @param outputStream Output stream
     * @param charset      Convert string to bytes according to given {@link Charset}
     * @throws IOException
     */

    private static void writeAll(String data, OutputStream outputStream, Charset charset)
            throws IOException {
        if ((data != null) && (data.length() > 0)) {
            outputStream.write(data.getBytes(charset));
        }
    }

    /**
     * Read all stream byte data into {@link String}
     *
     * @param inputStream Source stream
     * @param charset     Convert bytes to chars according to given {@link Charset}
     * @return Empty {@link String} if there was no data in stream
     * @throws IOException
     */
    private static String readAll(InputStream inputStream, Charset charset) throws IOException {
        try (InputStreamReader streamReader = new InputStreamReader(inputStream, charset)) {
            return readAll(streamReader);
        }
    }

    /**
     * Read all chars into String
     *
     * @param streamReader Input stream reader
     * @return Empty {@link String} if there was no data in stream
     * @throws IOException
     */
    public static String readAll(InputStreamReader streamReader) throws IOException {
        StringWriter result = new StringWriter();
        copy(streamReader, result);
        return result.toString();
    }

    private static long copy(Reader reader, Writer writer) throws IOException {
        return copy(reader, writer, new char[DEFAULT_BUFFER_SIZE]);
    }

    private static long copy(Reader reader, Writer writer, char[] buffer)
            throws IOException {
        assert buffer != null;
        assert buffer.length > 0;

        long result = 0;

        int read = reader.read(buffer);

        while (read > 0) {
            writer.write(buffer, 0, read);
            result += read;
            read = reader.read(buffer);
        }

        return result;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }
}
