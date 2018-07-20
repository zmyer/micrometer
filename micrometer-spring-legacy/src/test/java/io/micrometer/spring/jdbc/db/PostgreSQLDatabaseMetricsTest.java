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
package io.micrometer.spring.jdbc.db;

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kristof Depypere
 */
public class PostgreSQLDatabaseMetricsTest {
    private static final String DATABASE_NAME = "test";
    private static final String FUNCTIONAL_COUNTER_KEY = "key";
    private DataSource dataSource = new HikariDataSource();
    private MeterRegistry registry = new SimpleMeterRegistry();

    @Test
    public void shouldRegisterPostesMetrics() {
        PostgreSQLDatabaseMetrics postgreSQLDatabaseMetrics = new PostgreSQLDatabaseMetrics(dataSource, DATABASE_NAME);
        postgreSQLDatabaseMetrics.bindTo(registry);

        registry.get("postgres.size").tag("database", DATABASE_NAME).gauge();
        registry.get("postgres.connections").tag("database", DATABASE_NAME).gauge();
        registry.get("postgres.rows.fetched").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.rows.inserted").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.rows.updated").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.rows.deleted").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.rows.dead").tag("database", DATABASE_NAME).gauge();

        registry.get("postgres.blocks.hits").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.blocks.reads").tag("database", DATABASE_NAME).functionCounter();

        registry.get("postgres.temp.writes").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.locks").tag("database", DATABASE_NAME).gauge();

        registry.get("postgres.transactions").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.checkpoints.timed").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.checkpoints.requested").tag("database", DATABASE_NAME).functionCounter();

        registry.get("postgres.buffers.checkpoint").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.buffers.clean").tag("database", DATABASE_NAME).functionCounter();
        registry.get("postgres.buffers.backend").tag("database", DATABASE_NAME).functionCounter();
    }

    @Test
    public void shouldBridgePgStatReset() {
        PostgreSQLDatabaseMetrics postgreSQLDatabaseMetrics = new PostgreSQLDatabaseMetrics(dataSource, DATABASE_NAME);
        postgreSQLDatabaseMetrics.bindTo(registry);

        postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 5);
        postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 10);

        //first reset
        Double result = postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 5);

        //then
        assertThat(result).isEqualTo(15);
    }

    @Test
    public void shouldBridgeDoublePgStatReset() {
        PostgreSQLDatabaseMetrics postgreSQLDatabaseMetrics = new PostgreSQLDatabaseMetrics(dataSource, DATABASE_NAME);
        postgreSQLDatabaseMetrics.bindTo(registry);

        postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 5);
        postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 10);

        //first reset
        postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 3);

        //second reset
        Double result = postgreSQLDatabaseMetrics.resettableFunctionalCounter(FUNCTIONAL_COUNTER_KEY, () -> 1);

        assertThat(result).isEqualTo(14);
    }
}
