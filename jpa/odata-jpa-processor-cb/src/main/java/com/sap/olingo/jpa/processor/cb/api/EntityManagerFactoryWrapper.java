package com.sap.olingo.jpa.processor.cb.api;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;
import com.sap.olingo.jpa.processor.cb.impl.EntityManagerWrapper;

public final class EntityManagerFactoryWrapper implements EntityManagerFactory {
  private final EntityManagerFactory emf;
  private final JPAServiceDocument sd;
  private final ProcessorSqlPatternProvider pattern;

  public EntityManagerFactoryWrapper(final EntityManagerFactory emf, final JPAServiceDocument sd,
      final ProcessorSqlPatternProvider pattern) {
    super();
    this.emf = emf;
    this.sd = sd;
    this.pattern = pattern;
  }

  @Override
  public EntityManager createEntityManager() {
    return new EntityManagerWrapper(emf.createEntityManager(), sd, pattern);
  }

  @Override
  public EntityManager createEntityManager(@SuppressWarnings("rawtypes") final Map map) {
    return new EntityManagerWrapper(emf.createEntityManager(map), sd, pattern);
  }

  @Override
  public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
    return new EntityManagerWrapper(emf.createEntityManager(synchronizationType), sd, pattern);
  }

  @Override
  public EntityManager createEntityManager(final SynchronizationType synchronizationType,
      @SuppressWarnings("rawtypes") final Map map) {
    return new EntityManagerWrapper(emf.createEntityManager(synchronizationType, map), sd, pattern);
  }

  @Override
  public CriteriaBuilder getCriteriaBuilder() {
    try (EntityManager em = new EntityManagerWrapper(emf.createEntityManager(), sd, pattern)) {
      return em.getCriteriaBuilder();
    }
  }

  @Override
  public Metamodel getMetamodel() {
    return emf.getMetamodel();
  }

  @Override
  public boolean isOpen() {
    return emf.isOpen();
  }

  @Override
  public void close() {
    emf.close();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Map<String, Object> getProperties() {
    return emf.getProperties();
  }

  @Override
  public Cache getCache() {
    return emf.getCache();
  }

  @Override
  public PersistenceUnitUtil getPersistenceUnitUtil() {
    return emf.getPersistenceUnitUtil();
  }

  @Override
  public PersistenceUnitTransactionType getTransactionType() {
    throw new UnsupportedOperationException();
  }

  @Override
  public SchemaManager getSchemaManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addNamedQuery(final String name, final Query query) {
    emf.addNamedQuery(name, query);
  }

  @Override
  public <T> T unwrap(final Class<T> clazz) {
    return emf.unwrap(clazz);
  }

  @Override
  public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
    emf.addNamedEntityGraph(graphName, entityGraph);
  }

  @Override
  public <R> Map<String, TypedQueryReference<R>> getNamedQueries(Class<R> aClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <E> Map<String, EntityGraph<? extends E>> getNamedEntityGraphs(Class<E> aClass) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void runInTransaction(Consumer<EntityManager> consumer) {

  }

  @Override
  public <R> R callInTransaction(Function<EntityManager, R> function) {
    throw new UnsupportedOperationException();
  }

}
