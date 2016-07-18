package org.sharedhealth.datasense.helpers;


import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DatabaseHelper {
    public static void clearDatasenseTables(NamedParameterJdbcTemplate template) {
        template.update("delete from reference_term_map", new EmptySqlParameterSource());
        template.update("delete from reference_term", new EmptySqlParameterSource());
        template.update("delete from concept", new EmptySqlParameterSource());
        template.update("delete from patient_death_details", new EmptySqlParameterSource());
        template.update("delete from observation", new EmptySqlParameterSource());
        template.update("delete from immunization", new EmptySqlParameterSource());
        template.update("delete from diagnosis", new EmptySqlParameterSource());
        template.update("delete from provider", new EmptySqlParameterSource());
        template.update("delete from facility", new EmptySqlParameterSource());
        template.update("delete from patient", new EmptySqlParameterSource());
        template.update("delete from encounter", new EmptySqlParameterSource());
        template.update("delete from drug", new EmptySqlParameterSource());
        template.update("delete from procedures", new EmptySqlParameterSource());
        template.update("delete from diagnostic_order", new EmptySqlParameterSource());
        template.update("delete from diagnostic_report", new EmptySqlParameterSource());
        template.update("delete from prescribed_drug", new EmptySqlParameterSource());
    }
}
