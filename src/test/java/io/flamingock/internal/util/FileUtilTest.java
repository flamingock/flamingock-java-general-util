/*
 * Copyright 2026 Flamingock (https://www.flamingock.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.flamingock.internal.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldReturnYamlAndYmlFilesIgnoringCase() throws Exception {
        Files.createFile(tempDir.resolve("alpha.yaml"));
        Files.createFile(tempDir.resolve("bravo.yml"));
        Files.createFile(tempDir.resolve("charlie.YAML"));
        Files.createFile(tempDir.resolve("delta.YmL"));
        Files.createFile(tempDir.resolve("echo.txt"));

        List<String> fileNames = FileUtil.getAllYamlFiles(tempDir.toFile())
                .stream()
                .map(File::getName)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(4, fileNames.size());
        assertEquals("alpha.yaml", fileNames.get(0));
        assertEquals("bravo.yml", fileNames.get(1));
        assertEquals("charlie.YAML", fileNames.get(2));
        assertEquals("delta.YmL", fileNames.get(3));
    }
}
