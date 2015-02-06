/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Matthieu Chaffotte
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "description", "fields", "uniqueConstraints", "queries", "indexes" })
public class BusinessObject {

    @XmlID
    @XmlAttribute(required = true)
    private String qualifiedName;

    @XmlElement
    private String description;

    @XmlElementWrapper(name = "fields", required = true)
    @XmlElements({ @XmlElement(name = "field", type = SimpleField.class, required = true),
            @XmlElement(name = "relationField", type = RelationField.class, required = true) })
    private List<Field> fields;

    @XmlElementWrapper(name = "uniqueConstraints")
    @XmlElement(name = "uniqueConstraint")
    private List<UniqueConstraint> uniqueConstraints;

    @XmlElementWrapper(name = "indexes")
    @XmlElement(name = "index", required = false)
    private List<Index> indexes;

    @XmlElementWrapper(name = "queries")
    @XmlElement(name = "query")
    private List<Query> queries;

    public BusinessObject() {
        fields = new ArrayList<Field>();
        uniqueConstraints = new ArrayList<UniqueConstraint>();
        queries = new ArrayList<Query>();
        indexes = new ArrayList<Index>();
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(final String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(final List<Field> fields) {
        this.fields = fields;
    }

    public void addField(final Field field) {
        fields.add(field);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addUniqueConstraint(UniqueConstraint uniqueConstraint) {
        uniqueConstraints.add(uniqueConstraint);
    }

    public UniqueConstraint addUniqueConstraint(final String name, final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames cannot be null or empty");
        }
        final UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setName(name);
        uniqueConstraint.setFieldNames(Arrays.asList(fieldNames));

        addUniqueConstraint(uniqueConstraint);
        return uniqueConstraint;
    }

    public List<UniqueConstraint> getUniqueConstraints() {
        return uniqueConstraints;
    }

    public void setUniqueConstraints(final List<UniqueConstraint> uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    public Query addQuery(final String name, final String queryContent, final String returnType) {
        final Query query = new Query(name, queryContent, returnType);
        queries.add(query);
        return query;
    }

    public List<Query> getQueries() {
        return queries;
    }

    public void setQueries(final List<Query> queries) {
        this.queries = queries;
    }

    public void addIndex(Index index) {
        indexes.add(index);
    }

    public Index addIndex(final String name, final String... fieldNames) {
        if (fieldNames == null || fieldNames.length == 0) {
            throw new IllegalArgumentException("fieldNames cannot be null or empty");
        }
        final Index index = new Index();
        index.setName(name);

        index.setFieldNames(Arrays.asList(fieldNames));
        addIndex(index);
        return index;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(final List<Index> indexes) {
        this.indexes = indexes;
    }

    public List<BusinessObject> getReferencedBusinessObjectsByComposition() {
        List<BusinessObject> refs = new ArrayList<BusinessObject>();
        for (Field field : fields) {
            if (isACompositionField(field)) {
                refs.add(((RelationField) field).getReference());
            }
        }
        return refs;
    }

    private boolean isACompositionField(Field field) {
        return field instanceof RelationField && Type.COMPOSITION == ((RelationField) field).getType();
    }

    public String getSimpleName() {
        String simpleName = qualifiedName;
        if (simpleName != null && simpleName.indexOf(".") != -1) {
            final String[] split = simpleName.split("\\.");
            simpleName = split[split.length - 1];
        }
        return simpleName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(description).append(fields).append(indexes).append(qualifiedName).append(queries).append(uniqueConstraints)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BusinessObject) {
            final BusinessObject other = (BusinessObject) obj;
            return new EqualsBuilder().append(description, other.description).append(fields, other.fields).append(indexes, other.indexes)
                    .append(qualifiedName, other.qualifiedName).append(queries, other.queries).append(uniqueConstraints, other.uniqueConstraints).isEquals();
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("description", description)
                .append("fields", fields)
                .append("indexes", indexes)
                .append("qualifiedName", qualifiedName)
                .append("queries", queries)
                .append("uniqueConstraints", uniqueConstraints)
                .toString();
    }

}