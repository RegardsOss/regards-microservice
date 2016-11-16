/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.framework.jpa.json.test.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import fr.cnes.regards.framework.jpa.json.JsonBinaryType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TypeDefs({ @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) })
@Entity(name = "T_TEST_ENTITY")
@SequenceGenerator(name = "testEntitySequence", initialValue = 1, sequenceName = "SEQ_PLUGIN_PARAMETER")
public class TestEntity {

    /**
     * Unique id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "testEntitySequence")
    private Long id;

    /**
     * jsonb field
     */
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private JsonbEntity jsonbEntity;

    public TestEntity() {
        super();
    }

    public TestEntity(JsonbEntity pJsonbEntity) {
        this();
        jsonbEntity = pJsonbEntity;
    }

    public TestEntity(Long pId, JsonbEntity pJsonbEntity) {
        this(pJsonbEntity);
        id = pId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long pId) {
        id = pId;
    }

    public JsonbEntity getJsonbEntity() {
        return jsonbEntity;
    }

    public void setJsonbEntity(JsonbEntity pJsonbEntity) {
        jsonbEntity = pJsonbEntity;
    }

    @Override
    public String toString() {
        return "TestEntity { id = " + id + ", jsonbEntity = " + jsonbEntity + "}";
    }

    @Override
    public boolean equals(Object pOther) {
        return (pOther instanceof TestEntity) && ((TestEntity) pOther).id.equals(id)
                && ((TestEntity) pOther).jsonbEntity.equals(jsonbEntity);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
