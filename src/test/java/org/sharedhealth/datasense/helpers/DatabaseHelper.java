package org.sharedhealth.datasense.helpers;


import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DatabaseHelper {
    public static void clearDatasenseTables(NamedParameterJdbcTemplate template) {
        template.update("delete from medication", new EmptySqlParameterSource());
        template.update("delete from diagnosis", new EmptySqlParameterSource());
        template.update("delete from facility", new EmptySqlParameterSource());
        template.update("delete from patient", new EmptySqlParameterSource());
        template.update("delete from encounter", new EmptySqlParameterSource());
    }
}
