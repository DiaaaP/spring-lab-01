package kz.iitu.springlab.transaction;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifiesHistoryRerunAndNewIndexMigration() {
        List<MigrationRow> history = jdbcTemplate.query(
                """
                SELECT "version", "description", "success"
                FROM "flyway_schema_history"
                WHERE "version" IS NOT NULL
                ORDER BY "installed_rank"
                """,
                (resultSet, rowNumber) -> new MigrationRow(
                        resultSet.getString("version"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("success")
                )
        );

        history.forEach(row -> System.out.printf(
                "FLYWAY HISTORY: V%s %s success=%s%n",
                row.version(),
                row.description(),
                row.success()
        ));

        assertThat(history).extracting(MigrationRow::version).containsExactly("1", "2", "3", "4", "5", "6");
        assertThat(history).allMatch(MigrationRow::success);

        String currentVersion = flyway.info().current().getVersion().getVersion();
        System.out.printf("SCHEMA VERSION AFTER STARTUP: %s%n", currentVersion);
        assertThat(currentVersion).isEqualTo("6");

        MigrateResult rerun = flyway.migrate();
        System.out.printf(
                "RE-RUN RESULT: migrationsExecuted=%d%n",
                rerun.migrationsExecuted
        );
        assertThat(rerun.migrationsExecuted).isZero();

        Integer indexCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM INFORMATION_SCHEMA.INDEXES
                WHERE TABLE_NAME = 'ACCOUNTS'
                  AND INDEX_NAME = 'IDX_ACCOUNTS_OWNER_NAME'
                """,
                Integer.class
        );

        boolean indexExists = indexCount != null && indexCount == 1;
        System.out.printf(
                "INDEX CHECK: IDX_ACCOUNTS_OWNER_NAME exists=%s%n",
                indexExists
        );
        assertThat(indexExists).isTrue();
    }

    private record MigrationRow(String version, String description, boolean success) {
    }
}
