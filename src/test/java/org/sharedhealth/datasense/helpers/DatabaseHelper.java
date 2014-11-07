package org.sharedhealth.datasense.helpers;


import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseHelper {
    public static void clearDatasenseTables(JdbcTemplate template) {
        template.update("delete from patient");
        template.update("delete from facility");
    }
}
