package com.poly.poc.utils;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProps {

    String pValue = "";


    public String getPropValues(String propName) throws IOException {
        String propfileName = "config.properties";
        try (InputStream ins = getClass().getClassLoader().getResourceAsStream(propfileName)) {
        //try (InputStream ins = new FileInputStream(propfileName)) {
            Properties prop = new Properties();

            if (ins != null) {
                prop.load(ins);
            } else {
                throw new FileNotFoundException("property file " + propfileName + " not found");
            }

            pValue = prop.getProperty(propName);

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        return pValue;
    }

    public void loadLog4jprops() throws IOException {
        String propfileName = "log4j.properties";
        Properties prop = new Properties();
        try (InputStream ins = getClass().getClassLoader().getResourceAsStream(propfileName)) {
        //try (InputStream ins = new FileInputStream(propfileName)) {
            {
                prop.load(ins);
                LogManager.resetConfiguration();
                PropertyConfigurator.configure(prop);
            }
        } catch (FileNotFoundException e) {
            System.out.println("log4j configuration file not found");
        }
    }


    public void setPropValues(String key, String value) {
        String propfileName = "config.properties";
        try {
//            PropertiesConfiguration prop = new PropertiesConfiguration(propfileName);
//
//            prop.setProperty(key, value);
//            prop.save();
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setFileName(propfileName)
                                    .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            Configuration config = builder.getConfiguration();
            config.setProperty(key, value);
            builder.save();


        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

}
