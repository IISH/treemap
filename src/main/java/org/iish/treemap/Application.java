package org.iish.treemap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.iish.treemap.config.AcceptAllTrustManager;
import org.iish.treemap.config.Config;
import org.iish.treemap.config.StandardDataset;
import org.iish.treemap.config.TreemapModule;
import org.iish.treemap.labour.LabourRelations;
import org.iish.treemap.labour.LabourRelationsXlsxReader;
import org.iish.treemap.labour.LabourTreeMapBuilder;
import org.iish.treemap.labour.TimePeriods;
import org.iish.treemap.model.TabularData;
import org.iish.treemap.util.XlsxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.servlet.SparkApplication;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.*;

import static spark.Spark.*;

/**
 * Sets up the Treemap application.
 */
public class Application implements SparkApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Injector injector;
    private String key;
    private StandardDataset standardDataset;
    private LabourTreeMapBuilder labourTreeMapBuilder;

    /**
     * Run the application from the command line with the packaged Jetty servlet container.
     *
     * @param args The application arguments.
     */
    public static void main(String[] args) {
        port(Integer.parseInt(System.getProperty("port", "8080")));
        new Application().init();
    }

    /**
     * Initializes the application.
     */
    @Override
    public void init() {
        AcceptAllTrustManager.init();
        setUpInjector();
        loadIntoMemory();
        setUpPaths();
    }

    /**
     * Sets up the Guice dependency injection injector.
     */
    private void setUpInjector() {
        this.injector = Guice.createInjector(new TreemapModule());
        this.key = injector.getInstance(Config.class).key;
        this.standardDataset = injector.getInstance(StandardDataset.class);
        this.labourTreeMapBuilder = injector.getInstance(LabourTreeMapBuilder.class);
    }

    /**
     * Sets up the various routes.
     */
    private void setUpPaths() {
        staticFileLocation("/public");

        get("/labour/files", (req, res) -> labourTreeMapBuilder.getFiles(req), GSON::toJson);

        get("/labour/columns", (req, res) -> labourTreeMapBuilder.getColumns(req), GSON::toJson);

        get("/labour/treemap", (req, res) -> labourTreeMapBuilder.getTreemap(req), GSON::toJson);

        post("/upload", this::uploadDataset);

        after((req, res) -> {
            res.type("text/json");
            res.header("Content-Encoding", "gzip");
        });

        exception(Exception.class, (e, req, res) -> {
            LOGGER.error(e.getMessage(), e);
            res.status(400);
            res.body(e.getMessage());
        });
    }

    /**
     * The provided dataset should be loaded into memory.
     */
    private void loadIntoMemory() {
        String path = System.getProperty("dataset", null);
        if ((path != null) && new File(path).exists()) {
            LOGGER.info("Attempting to load dataset from " + path);

            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(path))) {
                TabularData data = (TabularData) objectInputStream.readObject();
                standardDataset.setDataset(data);
            }
            catch (Exception e) {
                LOGGER.error("Failed to load stored dataset to memory!", e);
            }
        }
    }

    /**
     * Replace the provided dataset with the uploaded dataset.
     *
     * @param req The request.
     * @param res The response.
     * @return Ok.
     * @throws IOException
     * @throws ServletException
     * @throws XlsxException
     */
    private String uploadDataset(Request req, Response res) throws IOException, ServletException, XlsxException {
        MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
        req.attribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);

        if (!key.equals(req.queryParams("key")))
            halt(401, "Invalid upload key!");

        try (InputStream inputStream = req.raw().getPart("excel").getInputStream()) {
            LabourRelationsXlsxReader xlsxReader = new LabourRelationsXlsxReader(
                    injector.getInstance(Config.class),
                    injector.getInstance(LabourRelations.class),
                    injector.getInstance(TimePeriods.class),
                    inputStream
            );
            TabularData data = xlsxReader.getData();

            String path = System.getProperty("dataset", null);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(path));
            objectOutputStream.writeObject(data);
            objectOutputStream.close();
        }

        res.redirect("/index.html");
        return "OK!";
    }
}
