/*
Copyright 2018 FZI Forschungszentrum Informatik

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.streampipes.connect.adapter.generic.protocol.stream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streampipes.connect.SendToPipeline;
import org.streampipes.connect.adapter.generic.format.Format;
import org.streampipes.connect.adapter.generic.format.Parser;
import org.streampipes.connect.adapter.generic.guess.SchemaGuesser;
import org.streampipes.connect.adapter.generic.pipeline.AdapterPipeline;
import org.streampipes.connect.adapter.generic.protocol.Protocol;
import org.streampipes.connect.adapter.generic.sdk.ParameterExtractor;
import org.streampipes.connect.exception.AdapterException;
import org.streampipes.model.connect.grounding.ProtocolDescription;
import org.streampipes.model.connect.guess.GuessSchema;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.model.staticproperty.AnyStaticProperty;
import org.streampipes.model.staticproperty.FreeTextStaticProperty;
import org.streampipes.model.staticproperty.Option;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class HDFSProtocol extends Protocol {

    public static final String ID = "https://streampipes.org/vocabulary/v1/protocol/stream/HDFS";

    private static String INTERVAL_PROPERTY = "intervalProperty";
    private static String URL_PROPERTY = "urlProperty";
    private static String USER_PROPERTY = "userProperty";
    private static String PASSWORD_PROPERTY = "passwordProperty";
    private static String DATA_PATH_PROPERTY = "dataPathProperty";
    private static String RECURSIVELY_PROPERTY = "recursively";
    private static String OPTIONS = "optionsFile";

    private long intervalProperty;
    private String dataPathProperty;
    private String urlProperty;
    private String userProperty;
    private String passwordProperty;
    private boolean recursively;

    private ScheduledExecutorService scheduler;
    private Logger logger = LoggerFactory.getLogger(HDFSProtocol.class);

    private long knownNewestFileDate;

    public HDFSProtocol() {

    }

    public HDFSProtocol(Parser parser, Format format, long intervalProperty, String dataPathProperty, String urlProperty, boolean recursively) {
        super(parser, format);
        this.intervalProperty = intervalProperty;
        this.dataPathProperty = dataPathProperty;
        this.urlProperty = urlProperty;
        this.recursively = recursively;
    }

    @Override
    public Protocol getInstance(ProtocolDescription protocolDescription, Parser parser, Format format) {
        ParameterExtractor extractor = new ParameterExtractor(protocolDescription.getConfig());
        long intervalProperty = Long.parseLong(extractor.singleValue(INTERVAL_PROPERTY));
        String urlProperty = extractor.singleValue(URL_PROPERTY);
        //    String userProperty = extractor.singleValue(USER_PROPERTY);
        //    String passwordProperty = extractor.singleValue(PASSWORD_PROPERTY);
        String dataPathProperty = extractor.singleValue(DATA_PATH_PROPERTY);

//        boolean recursively = extractor.selectedMultiValues(RECURSIVELY_PROPERTY).stream()
//                .anyMatch(o -> o.equals("recursively"));

        return new HDFSProtocol(parser, format, intervalProperty, dataPathProperty, urlProperty, recursively);

    }

    @Override
    public ProtocolDescription declareModel() {
        ProtocolDescription pd = new ProtocolDescription(ID, "HDFS", "This is the " +
                "description for the HDFS Stream protocol.");

        FreeTextStaticProperty intervalProperty = new FreeTextStaticProperty(INTERVAL_PROPERTY, "Interval", "This property " +
                "defines the pull interval in seconds.");
        FreeTextStaticProperty urlProperty = new FreeTextStaticProperty(URL_PROPERTY, "HDFS-Server URL e.g. hdfs://server:8020",
                "This property defines the HDFS URL e.g. hdfs://server:8020");
        //     FreeTextStaticProperty userNameProperty = new FreeTextStaticProperty(USER_PROPERTY, "Username",
        //            "The Username to login");
        //    FreeTextStaticProperty passwordProperty = new FreeTextStaticProperty(PASSWORD_PROPERTY, "Password",
        //            "The Password to login");
        FreeTextStaticProperty dataPathProperty = new FreeTextStaticProperty(DATA_PATH_PROPERTY, "Data Path",
                "The Data Path which should be watched");


        // AnyStaticProperty offset = new AnyStaticProperty(OPTIONS, "Options for Folders", "");
        // offset.setOptions(Arrays.asList(new Option("Search Recursively","recursively")));


        pd.setSourceType("STREAM");

        pd.setIconUrl("hdfs.png");
        pd.addConfig(urlProperty);
        pd.addConfig(intervalProperty);
        //   pd.addConfig(userNameProperty);
        //    pd.addConfig(passwordProperty);
        pd.addConfig(dataPathProperty);

        pd.setAppId(ID);

        return pd;
    }

    @Override
    public GuessSchema getGuessSchema() throws AdapterException {
        int n = 2;
        GuessSchema result = null;

        InputStream inputStream = getInputStreamFromFile(getFiles().get(0));
        if (inputStream == null)
            throw new AdapterException("Could not receive data from file: " + dataPathProperty);

        List<byte[]> dataByte = parser.parseNEvents(inputStream, n);
        if (dataByte.size() < n) {
            logger.error("Error in HDFS Protocol! Required: " + n + " elements but the resource just had: " +
                    dataByte.size());

            dataByte.addAll(dataByte);
        }
        EventSchema eventSchema = parser.getEventSchema(dataByte);
        result = SchemaGuesser.guessSchma(eventSchema, getNElements(n));


        return result;
    }

    @Override
    public List<Map<String, Object>> getNElements(int n) throws AdapterException {
        List<Map<String, Object>> result = new ArrayList<>();

        InputStream inputStream = getInputStreamFromFile(getFiles().get(0));
        if (inputStream == null)
            throw new AdapterException("Could not receive data from file: " + dataPathProperty);

        List<byte[]> dataByte = parser.parseNEvents(inputStream, n);

        // Check that result size is n. Currently just an error is logged. Maybe change to an exception
        if (dataByte.size() < n) {
            logger.error("Error in  HDFS Protocol! User required: " + n + " elements but the resource just had: " +
                    dataByte.size());
        }

        for (byte[] b : dataByte) {
            result.add(format.parse(b));
        }

        return result;

    }

    @Override
    public void run(AdapterPipeline adapterPipeline) {
        logger.info("Start HDFS Adapter");

        this.knownNewestFileDate = 0;

        final Runnable errorThread = () -> {
            executeProtocolLogic(adapterPipeline);
        };


        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(errorThread, 0, TimeUnit.MILLISECONDS);
    }


    private void executeProtocolLogic(AdapterPipeline adapterPipeline) {
        final Runnable task = () -> {
            SendToPipeline stk = new SendToPipeline(format, adapterPipeline);

            List<LocatedFileStatus> files = getFiles(this.knownNewestFileDate);
            if (files.size() > 0) {
                this.knownNewestFileDate = files.get(files.size() - 1).getModificationTime();
                logger.info("+++ New files found, newest file Date: " + this.knownNewestFileDate + " (in milliseconds form 1970)");
            } else
                logger.info("No new files found");
            files.forEach(file -> parser.parse(getInputStreamFromFile(file), stk));
        };


        scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(task, 1, this.intervalProperty, TimeUnit.SECONDS);
        try {
            handle.get();
        } catch (ExecutionException e ) {
            logger.error("Error", e);
        } catch (InterruptedException e) {
            logger.error("Error", e);
        }
    }


    @Override
    public void stop() {
        scheduler.shutdownNow();
    }

    @Override
    public String getId() {
        return ID;
    }


    private List<LocatedFileStatus> getFiles(long startDate) {
        List<LocatedFileStatus> files = getFiles();

        files = files
                .parallelStream()
                .filter(ftpFile -> ftpFile.getModificationTime() > startDate)
                .sorted(((o1, o2) -> (((Long) o2.getModificationTime()).compareTo((Long) (o1.getModificationTime())))))
                .collect(Collectors.toList());

        return files;

    }

    public List<LocatedFileStatus> getFiles() {
        List<LocatedFileStatus> files = new ArrayList<>();

        FileSystem fs = getFilesSystem();
        Path hdfsreadpath = new Path(this.dataPathProperty);

        RemoteIterator<LocatedFileStatus> iter = null;
        try {
            iter = fs.listFiles(hdfsreadpath, this.recursively);
            while (iter.hasNext())
                files.add(iter.next());
        } catch (IOException e) {
            logger.error(e.toString());
        } finally {
            try {
                fs.close();
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }
        return files;
    }


    public static List<String> getFileNames(FileSystem fs, String route) throws IOException {
        List<String> result = new ArrayList<>();

        if (route.endsWith("/")) {
            Path tmp = new Path(route);
            RemoteIterator<LocatedFileStatus> i = fs.listFiles(tmp, false);

            while (i.hasNext()) {
                String path = i.next().getPath().toString();
                if (path.endsWith("/")) {
                    result.addAll(getFileNames(fs, path));
                } else {
                    result.add(path);
                }
            }
        } else {
            result.add(route);
        }

        return result;
    }

    private FileSystem getFilesSystem() {
        FileSystem fs = null;
        Configuration conf = getConfigutation();

        try {
            fs = FileSystem.get(URI.create(this.urlProperty), conf);
        } catch (IOException e) {
            logger.error(e.toString());
        }
        return fs;
    }

    private Configuration getConfigutation() {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", this.urlProperty);
        conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
        conf.set("dfs.client.use.datanode.hostname", "true");
        System.setProperty("HADOOP_USER_NAME", "hdfs");
        System.setProperty("hadoop.home.dir", "/");

        return conf;
    }

    private FSDataInputStream getInputStreamFromFile(LocatedFileStatus locatedFileStatus) {
        FileSystem fs = getFilesSystem();
        FSDataInputStream inputStream = null;
        try {
            inputStream = fs.open(locatedFileStatus.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }


}
