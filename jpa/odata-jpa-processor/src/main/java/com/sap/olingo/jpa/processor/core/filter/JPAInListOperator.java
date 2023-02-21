package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAInListOperator<T> extends JPAExpressionOperator {

  Expression<T> getLeft() throws ODataApplicationException;

  List<T> getRight() throws ODataApplicationException;

}