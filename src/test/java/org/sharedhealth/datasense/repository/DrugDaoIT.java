package org.sharedhealth.datasense.repository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.tr.CodeableConcept;
import org.sharedhealth.datasense.model.tr.Coding;
import org.sharedhealth.datasense.model.tr.TrMedication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class DrugDaoIT {
    @Autowired
    private DrugDao drugDao;
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


    @Test
    public void shouldInsertANewDrug() throws Exception {
        TrMedication drug = new TrMedication();
        drug.setUuid("drug-uuid");
        drug.setName("drug-name");
        drug.setCode(getCodeableConcept("reference-code", "concept-uuid"));

        drugDao.saveOrUpdate(drug);

        assertDrug(drug);

    }

    @Test
    public void shouldUpdateAnExistingDrug() throws Exception {
        TrMedication drug = new TrMedication();
        drug.setUuid("drug-uuid");
        drug.setName("drug-name");
        drug.setCode(getCodeableConcept("reference-code", "concept-uuid"));
        drugDao.saveOrUpdate(drug);

        //update drug
        drug.setName("Brand new Drug");
        drug.setCode(getCodeableConcept("reference-code-new", "new-concept-uuid"));

        drugDao.saveOrUpdate(drug);

        assertDrug(drug);
    }

    private void assertDrug(final TrMedication drug) {
        jdbcTemplate.query("select * from drug where drug_uuid='drug-uuid'", new RowMapper<ResultSet>() {
            @Override
            public ResultSet mapRow(ResultSet rs, int rowNum) throws SQLException {
                assertEquals(drug.getUuid(), rs.getString("drug_uuid"));
                assertEquals(drug.getName(), rs.getString("name"));
                assertEquals(drug.getReferenceTermId(), rs.getString("reference_term_uuid"));
                assertEquals(drug.getAssociatedConceptId(), rs.getString("concept_uuid"));
                return rs;
            }
        });
    }

    private CodeableConcept getCodeableConcept(String code, String conceptId) {
        CodeableConcept drugCode = new CodeableConcept();
        drugCode.addCoding(getCoding("http://tr.com/openmrs/ws/rest/v1/tr/referenceterms/" + code, code));
        drugCode.addCoding(getCoding("http://tr.com/openmrs/ws/rest/v1/tr/concepts/concept-uuid", conceptId));
        return drugCode;
    }

    private Coding getCoding(String system, String code) {
        Coding coding = new Coding();
        coding.setSystem(system);
        coding.setCode(code);
        return coding;
    }

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }
}
