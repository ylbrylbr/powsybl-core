/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSource;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public abstract class AbstractDataStoreTest {

    private FileSystem fileSystem;

    protected Path testDir;

    protected DataStore dataStore;

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
        dataStore = createDataStore();
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    protected boolean appendTest() {
        return true;
    }

    protected abstract DataStore createDataStore() throws IOException;

    @Test
    public void test() throws IOException {
        writeThenReadTest("test.txt");
        dataSourceConversionTest("test.txt");
    }

    private void writeThenReadTest(String entry) throws IOException {

        // write file
        try (OutputStream os = dataStore.newOutputStream(entry, false)) {
            os.write("line1".getBytes(StandardCharsets.UTF_8));
        }

        // check files exists
        assertTrue(dataStore.getEntryNames().contains(entry));

        // check all listed names exist and we can read them
        for (String name : dataStore.getEntryNames()) {

            try (InputStream is = dataStore.newInputStream(name)) {
                // Ok, some content is available
            } catch (IOException x) {
                fail(name);
            }
        }

        // check content is ok
        try (InputStream is = dataStore.newInputStream(entry)) {
            assertEquals("line1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }

    }

    private void dataSourceConversionTest(String entryName) throws IOException {
        DataSource dataSource = dataStore.toDataSource(entryName);
        assertTrue(dataSource.exists(entryName));

        try (InputStream is = dataSource.newInputStream(entryName)) {
            assertEquals("line1", new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8));
        }
    }

}