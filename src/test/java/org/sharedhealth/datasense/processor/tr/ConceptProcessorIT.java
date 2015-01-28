package org.sharedhealth.datasense.processor.tr;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sharedhealth.datasense.helpers.DatabaseHelper;
import org.sharedhealth.datasense.helpers.TestConfig;
import org.sharedhealth.datasense.launch.DatabaseConfig;
import org.sharedhealth.datasense.model.tr.TrConcept;
import org.sharedhealth.datasense.model.tr.TrReferenceTerm;
import org.sharedhealth.datasense.repository.ConceptDao;
import org.sharedhealth.datasense.repository.ReferenceTermDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("/test-shr-datasense.properties")
@ContextConfiguration(classes = {TestConfig.class, DatabaseConfig.class})
public class ConceptProcessorIT {
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private ConceptProcessor conceptProcessor;
    @Autowired
    private ReferenceTermDao referenceTermDao;
    @Autowired
    private ConceptDao conceptDao;

    @After
    public void tearDown() throws Exception {
        DatabaseHelper.clearDatasenseTables(jdbcTemplate);
    }

    @Test
    public void shouldSaveConcept() throws Exception {
        TrConcept trConcept = getTrConcept();
        conceptProcessor.process(trConcept);
        TrConcept savedConcept = conceptDao.findByConceptUuid(trConcept.getConceptUuid());
        assertEquals(trConcept.getConceptUuid(), savedConcept.getConceptUuid());
        assertEquals(trConcept.getName(), savedConcept.getName());
        assertEquals(trConcept.getConceptClass(), savedConcept.getConceptClass());
    }

    @Test
    public void shouldUpdateConcept() throws Exception {
        TrConcept trConcept = getTrConcept();
        conceptProcessor.process(trConcept);
        assertNotNull(conceptDao.findByConceptUuid(trConcept.getConceptUuid()));

        String newName = "new Name";
        String newClass = "new Class";
        trConcept.setName(newName);
        trConcept.setConceptClass(newClass);
        conceptProcessor.process(trConcept);
        TrConcept savedConcept = conceptDao.findByConceptUuid(trConcept.getConceptUuid());
        assertEquals(newName, savedConcept.getName());
        assertEquals(newClass, savedConcept.getConceptClass());
    }

    @Test
    public void shouldSaveConceptReferenceTermMaps() throws Exception {
        TrConcept trConcept = getTrConceptWithReferenceTerm();
        TrReferenceTerm trReferenceTerm = trConcept.getReferenceTermMaps().get(0);
        conceptProcessor.process(trConcept);
        assertNotNull(conceptDao.findByConceptUuid(trConcept.getConceptUuid()));
        assertNotNull(referenceTermDao.findByReferenceTermUuid(trReferenceTerm.getReferenceTermUuid()));
        List<TrReferenceTerm> conceptTermMaps = conceptDao.findConceptTermMaps(trConcept.getConceptUuid());
        assertEquals(1, conceptTermMaps.size());
        TrReferenceTerm fetchedReferenceTerm = conceptTermMaps.get(0);
        assertEquals(trReferenceTerm.getReferenceTermUuid(), fetchedReferenceTerm.getReferenceTermUuid());
        assertEquals(trReferenceTerm.getRelationshipType(), fetchedReferenceTerm.getRelationshipType());
    }

    @Test
    public void shouldUpdateConceptReferenceTermMaps() throws Exception {
        TrConcept trConcept = getTrConceptWithReferenceTerm();
        TrReferenceTerm trReferenceTerm = trConcept.getReferenceTermMaps().get(0);
        conceptProcessor.process(trConcept);

        String relationshipType = "new Relationship Type";
        trReferenceTerm.setRelationshipType(relationshipType);
        conceptProcessor.process(trConcept);
        List<TrReferenceTerm> conceptTermMaps = conceptDao.findConceptTermMaps(trConcept.getConceptUuid());
        assertEquals(1, conceptTermMaps.size());
        TrReferenceTerm fetchedReferenceTerm = conceptTermMaps.get(0);
        assertEquals(relationshipType, fetchedReferenceTerm.getRelationshipType());
    }

    private TrConcept getTrConceptWithReferenceTerm() {
        TrConcept trConcept = getTrConcept();
        TrReferenceTerm referenceTerm = new TrReferenceTerm();
        referenceTerm.setReferenceTermUuid("refUuid");
        referenceTerm.setName("refName");
        referenceTerm.setCode("refCode");
        referenceTerm.setSource("refSource");
        referenceTerm.setRelationshipType("refRelationType");
        trConcept.setReferenceTermMaps(asList(referenceTerm));
        return trConcept;
    }

    private TrConcept getTrConcept() {
        TrConcept trConcept = new TrConcept();
        trConcept.setConceptUuid("uuid");
        trConcept.setName("name");
        trConcept.setConceptClass("class");
        return trConcept;
    }
}