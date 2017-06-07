package org.sharedhealth.datasense.processor.tr;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
import org.sharedhealth.datasense.repository.ReferenceTermDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {DatabaseConfig.class, TestConfig.class})
public class ReferenceTermProcessorIT {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ReferenceTermProcessor referenceTermProcessor;

    @Autowired
    private ReferenceTermDao referenceTermDao;

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldSaveReferenceTerm() throws Exception {
        TrReferenceTerm trReferenceTerm = getTrReferenceTerm();
        referenceTermProcessor.process(trReferenceTerm);
        TrReferenceTerm savedReferenceTerm = referenceTermDao.findByReferenceTermUuid(trReferenceTerm.getReferenceTermUuid());
        assertEquals(trReferenceTerm.getCode(), savedReferenceTerm.getCode());
        assertEquals(trReferenceTerm.getName(), savedReferenceTerm.getName());
        assertEquals(trReferenceTerm.getSource(), savedReferenceTerm.getSource());
    }

    @Test
    public void shouldUpdateReferenceTerm() throws Exception {
        TrReferenceTerm trReferenceTerm = getTrReferenceTerm();
        referenceTermProcessor.process(trReferenceTerm);
        assertNotNull(referenceTermDao.findByReferenceTermUuid(trReferenceTerm.getReferenceTermUuid()));

        trReferenceTerm.setName("newRefName");
        trReferenceTerm.setSource("newRefSource");
        trReferenceTerm.setCode("newRefCode");
        referenceTermProcessor.process(trReferenceTerm);
        TrReferenceTerm savedReferenceTerm = referenceTermDao.findByReferenceTermUuid(trReferenceTerm.getReferenceTermUuid());
        assertEquals(trReferenceTerm.getCode(), savedReferenceTerm.getCode());
        assertEquals(trReferenceTerm.getName(), savedReferenceTerm.getName());
        assertEquals(trReferenceTerm.getSource(), savedReferenceTerm.getSource());
    }

    private TrReferenceTerm getTrReferenceTerm() {
        TrReferenceTerm trReferenceTerm = new TrReferenceTerm();
        trReferenceTerm.setReferenceTermUuid("refUuid");
        trReferenceTerm.setName("refName");
        trReferenceTerm.setCode("refCode");
        trReferenceTerm.setSource("refSource");
        return trReferenceTerm;
    }
}