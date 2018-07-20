/**
 * Copyright 2017 Pivotal Software, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link IOUtils}.
 *
 * @author Johnny Lim
 */
class IOUtilsTest {

    @Test
    public void testToString() {
        String expected = "This is a sample.";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(expected.getBytes());

        assertThat(IOUtils.toString(inputStream)).isEqualTo(expected);
    }

    @Test
    public void testToStringWithCharset() {
        String expected = "This is a sample.";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));

        assertThat(IOUtils.toString(inputStream, StandardCharsets.UTF_8)).isEqualTo(expected);
    }

}
