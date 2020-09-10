/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */


package fr.cnes.regards.framework.dump;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.gson.Gson;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;

/**
 * The purpose of this service is to created a zip of zips. These zips contain a limited number of json files
 * distributed according to a temporal tree structure
 * @author Iliana Ghazali
 */
@Service
public class DumpService {

    @Autowired
    private Gson gson;

    @Value("${spring.application.name}")
    private String microservice;

    public static final String FOLDER_PATH_PATTERN = "yyyy/MM/dd";

    public static final Logger LOGGER = LoggerFactory.getLogger(DumpService.class);

    /**
     * Check if jsonNames are unique in dumpCollection
     * @param dumpCollection objects to dump
     * @return list of dumpIds corresponding to dumpObjects with duplicated jsonNames
     */
    public List<String> checkUniqueJsonNames(List<ObjectDump> dumpCollection) {
        List<String> listDuplicatedDumps = new ArrayList<>();
        // Create multimap with jsonNames
        ImmutableListMultimap<String, ObjectDump> dumpMultimap = Multimaps
                .index(dumpCollection, ObjectDump::getJsonName);
        // If duplicated keys were found, jsonNames are not uniques in collection
        if (dumpCollection.size() != dumpMultimap.keySet().size()) {
            dumpMultimap.asMap().forEach((key, collection) -> {
                if (collection.size() > 1) {
                    // add dumpIds with duplicated jsonNames
                    collection.forEach(obj -> listDuplicatedDumps.add(obj.getDumpId()));
                }
            });
        }
        return listDuplicatedDumps;
    }

    /**
     * Generate a zip from list of object dumps
     * @param zipCollection list of objects to dump
     * @param tmpDumpLocation temporary location to write zip
     * @throws IOException
     */
    public void generateJsonZip(List<ObjectDump> zipCollection, String tmpDumpLocation) throws IOException {
      /*  // Sort dump collection by date
        Collections.sort(zipCollection);*/ //FIXME : to keep ??

        // Check if dump location exists
        List<String> listZipNames = new ArrayList<>();
        File folder = new File(tmpDumpLocation);
        if (folder.exists()) {
            // create list of zip names
            File[] zips = folder.listFiles();
            for (File zip : zips) {
                listZipNames.add(zip.getName());
            }
        } else {
            //create tmpDumpLocation
            folder.mkdir();
        }

        // Create zip
        // local vars
        DateTimeFormatter folderPathFormatter = DateTimeFormatter.ofPattern(FOLDER_PATH_PATTERN);
        String filename, filePath, fileContent, zipName;
        ZipEntry fileEntry;
        int indexName;

        // generate zip name
        String firstDate = OffsetDateTimeAdapter.format(zipCollection.get(0).getCreationDate());
        String lastDate = OffsetDateTimeAdapter.format(zipCollection.get(zipCollection.size() - 1).getCreationDate());
        zipName = firstDate + "_" + lastDate + ".zip";
        // handle not unique names
        if (listZipNames.contains(zipName)) {
            indexName = 0;
            do {
                zipName = firstDate + "_" + lastDate + "_" + indexName + ".zip";
                indexName++;
            } while (listZipNames.contains(zipName));
        }

        // add json files to zip
        try (ByteArrayOutputStream tmpZipByte = new ByteArrayOutputStream();
                ZipOutputStream tmpZip = new ZipOutputStream(new BufferedOutputStream(tmpZipByte))) {
            // add all json files to zip in memory
            for (ObjectDump objectDump : zipCollection) {
                filename = objectDump.getJsonName() + ".json";
                filePath = folderPathFormatter.format(objectDump.getCreationDate()) + "/" + filename;
                fileContent = this.gson.toJson(objectDump.getJsonContent());
                // Add File to Sub Zip
                fileEntry = new ZipEntry(filePath);
                tmpZip.putNextEntry(fileEntry);
                tmpZip.write(fileContent.getBytes());
                tmpZip.closeEntry();
            }
            tmpZip.close();

            // write zip
            try (FileOutputStream zip = new FileOutputStream(new File(tmpDumpLocation + "/" + zipName))) {
                zip.write(tmpZipByte.toByteArray());
            }
        }
    }

    /**
     * Generate a dump (a zip of zips)
     * @param dumpLocation location of the dump
     * @param tmpDumpLocation location of the temporary zips to dump
     * @param dumpDate date when a request was created to dump objects
     * @throws IOException
     */
    public void generateDump(String dumpLocation, String tmpDumpLocation, OffsetDateTime dumpDate) throws IOException {
        // Get all zips to dump
        File tmpDumpFolder = new File(tmpDumpLocation);
        File[] zipArray = tmpDumpFolder.listFiles();

        // If tmpDumpFolder is not empty, zip all content
        if (zipArray != null) {
            ZipEntry zipEntry;
            FileInputStream fileInputStream;
            File currentZip;
            byte[] buf = new byte[1024];
            int bytesRead;

            // Create dump
            String dumpName = "dump_json_" + microservice + "_" + OffsetDateTimeAdapter.format(dumpDate);
            Files.createDirectories(Paths.get(dumpLocation));
            int zipArrayLength = zipArray.length;

            try (ZipOutputStream dumpZip = new ZipOutputStream(
                    new FileOutputStream(new File(dumpLocation + "/" + dumpName + ".zip")))) {
                // Zip content from tmpDumpFolder
                for (int i = 0; i < zipArrayLength; i++) {
                    // Get zip file
                    currentZip = zipArray[i];
                    zipEntry = new ZipEntry(currentZip.getName());
                    dumpZip.putNextEntry(zipEntry);
                    // Read the input file by chucks of 1024 bytes and write the read bytes to the zip stream
                    fileInputStream = new FileInputStream(currentZip);
                    while ((bytesRead = fileInputStream.read(buf)) > 0) {
                        dumpZip.write(buf, 0, bytesRead);
                    }
                    dumpZip.closeEntry();
                }
            }
        }
    }
}

