package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

class JPAInListOperatorImp<T> implements JPAInListOperator<T> {

  private final JPAOperationConverter converter;
  private final BinaryOperatorKind operator;
  private final JPAMemberOperator left;
  private final List<JPAOperator> right;

  public JPAInListOperatorImp(final JPAOperationConverter converter, final BinaryOperatorKind operator,
      final JPAMemberOperator left, final List<JPAOperator> right) {
    super();
    this.converter = converter;
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAInListOperator#get()
   */
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAInListOperator#getOperator()
   */
  @SuppressWarnings("unchecked")
  @Override
  public BinaryOperatorKind getOperator() {
    return operator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAInListOperator#getLeft()
   */
  @SuppressWarnings("unchecked")
  @Override
  public Expression<T> getLeft() throws ODataApplicationException {
    return (Expression<T>)left.get();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAInListOperator#getRight()
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<T> getRight() throws ODataApplicationException {
	List<T> res = new ArrayList<T>(right.size()+1);
	for (JPAOperator expr : right)
	  res.add((T)expr.get());
    return res;
  }

  @Override
  public String getName() {
    return operator.name();
  }
}