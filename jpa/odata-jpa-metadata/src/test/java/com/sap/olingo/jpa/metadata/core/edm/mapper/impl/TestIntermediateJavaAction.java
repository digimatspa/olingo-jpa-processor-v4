package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.junit.Before;
import org.junit.Test;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaActions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaEmConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaPrivateConstructor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.ExampleJavaTwoParameterConstructor;

public class TestIntermediateJavaAction extends TestMappingRoot {
  private TestHelper helper;

  @Before
  public void setup() throws ODataJPAModelException {
    helper = new TestHelper(emf.getMetamodel(), PUNIT_NAME);
  }

  @Test
  public void checkInternalNameEqualMethodName() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("unboundWithImport", act.getInternalName());
  }

  @Test
  public void checkInternalNameGiven() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("unboundWithImport", act.getInternalName());
  }

  @Test
  public void checkExternalNameEqualMethodName() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertEquals("UnboundWithImport", act.getExternalName());
  }

  @Test
  public void checkReturnsFalseForIsBound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertFalse(act.getEdmItem().isBound());
  }

  @Test
  public void checkReturnsTrueForIsBound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals(PUNIT_NAME + ".Person", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  public void checkReturnsEntitySetPathForBound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundWithEntitySetPath");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals("Person/Roles", act.getEdmItem().getEntitySetPath());
  }

  @Test
  public void checkReturnsGivenEntitySetTypeIfBound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertTrue(act.getEdmItem().isBound());
    assertEquals(PUNIT_NAME + ".Person", act.getEdmItem().getParameters().get(0).getTypeFQN()
        .getFullQualifiedNameAsString());
  }

  @Test
  public void checkReturnsExternalName() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertEquals("BoundNoImport", act.getEdmItem().getName());
  }

  @Test
  public void checkReturnsTrueForHasActionImportIfUnbound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertTrue(act.hasImport());
  }

  @Test
  public void checkReturnsFalseForHasActionImportIfNotSet() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertFalse(act.hasImport());
  }

  @Test
  public void checkReturnsReturnTypeConvertedPrimitiveReturnType() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getReturnType());
    assertEquals("Edm.Int32", act.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkReturnsReturnTypeNullForVoid() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getEdmItem());
    assertNull(act.getEdmItem().getReturnType());
  }

  @Test
  public void checkReturnsReturnTypeEmbeddableType() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEmbeddable");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkReturnsEntityTypeAsReturnType() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEntity");

    assertEquals("com.sap.olingo.jpa.Person", act.getEdmItem().getReturnType().getType());
  }

  @Test
  public void checkReturnsReturnTypeCollectionOfPrimitive() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnCollection");

    assertEquals("Edm.String", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  public void checkReturnsReturnTypeCollectionOfEmbeddable() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnEmbeddableCollection");

    assertEquals("com.sap.olingo.jpa.ChangeInformation", act.getEdmItem().getReturnType().getType());
    assertTrue(act.getEdmItem().getReturnType().isCollection());
  }

  @Test
  public void checkReturnsReturnTypeFacetForNumbers() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundReturnFacet");
    assertFalse(act.getEdmItem().getReturnType().isNullable());
    assertEquals(Integer.valueOf(20), act.getEdmItem().getReturnType().getPrecision());
    assertEquals(Integer.valueOf(5), act.getEdmItem().getReturnType().getScale());
  }

  @Test
  public void checkReturnsReturnTypeFacetForStringsAndGeo() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "calculateLocation");

    assertEquals(Integer.valueOf(60), act.getEdmItem().getReturnType().getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getReturnType().getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getReturnType().getSrid().toString());
  }

  @Test
  public void checkReturnsParameterConvertPrimitiveTypes() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");

    assertNotNull(act.getEdmItem());
    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(2, act.getEdmItem().getParameters().size());
    assertNotNull(act.getEdmItem().getParameter("A"));
    assertNotNull(act.getEdmItem().getParameter("B"));
    assertEquals("Edm.Int16", act.getEdmItem().getParameter("A").getType());
    assertEquals("Edm.Int32", act.getEdmItem().getParameter("B").getType());
  }

  @Test
  public void checkReturnsParameterFacetForNumbers() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundNoImport");

    assertNotNull(act.getParameter());
    assertEquals(Integer.valueOf(34), act.getParameter().get(1).getPrecision());
    assertEquals(Integer.valueOf(10), act.getParameter().get(1).getScale());

    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(Integer.valueOf(34), act.getEdmItem().getParameters().get(1).getPrecision());
    assertEquals(Integer.valueOf(10), act.getEdmItem().getParameters().get(1).getScale());
  }

  @Test
  public void checkReturnsParameterFacetForStringsAndGeo() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "calculateLocation");

    assertNotNull(act.getParameter());
    assertEquals(Integer.valueOf(100), act.getParameter().get(0).getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getParameter().get(0).getSrid().getDimension());
    assertEquals("4326", act.getParameter().get(0).getSrid().toString());

    assertNotNull(act.getEdmItem().getParameters());
    assertEquals(Integer.valueOf(100), act.getEdmItem().getParameters().get(0).getMaxLength());
    assertEquals(Dimension.GEOGRAPHY, act.getEdmItem().getParameters().get(0).getSrid().getDimension());
    assertEquals("4326", act.getEdmItem().getParameters().get(0).getSrid().toString());
  }

  @Test
  public void checkExceptConstructorWithoutParameter() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "unboundWithImport");
    act.getEdmItem();
    assertNotNull(act.getConstructor());
  }

  @Test
  public void checkExceptConstructorWithEntityManagerParameter() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaEmConstructor.class, "mul");

    assertNotNull(act.getConstructor());
    assertEquals(1, act.getConstructor().getParameterTypes().length);
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionForNonPrimitiveParameter() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "errorNonPrimitiveParameter");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExceptionIfCollectionAndReturnTypeEmpty() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "returnCollectionWithoutReturnType");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnPrivateConstructor() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaPrivateConstructor.class, "mul");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnNoConstructorAsSpecified() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaTwoParameterConstructor.class, "mul");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnIsBoundWithoutEntityTypeParameter() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundWithOutBindingParameter");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnIsBoundWithoutParameter() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundWithOutParameter");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnIsBoundBindingParameterNotFirst() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "boundBindingParameterSecondParameter");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnEntitySetGivenUnbound() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "errorUnboundWithEntitySetPath");
    act.getEdmItem();
  }

  @Test(expected = ODataJPAModelException.class)
  public void checkThrowsExcpetionOnEntitySetGivenNoEntityReturnType() throws ODataJPAModelException {
    IntermediateJavaAction act = createAction(ExampleJavaActions.class, "errorPrimitiveTypeWithEntitySetPath");
    act.getEdmItem();
  }

  private IntermediateJavaAction createAction(Class<? extends ODataAction> clazz, String method)
      throws ODataJPAModelException {
    for (Method m : Arrays.asList(clazz.getMethods())) {
      EdmAction actionDescribtion = m.getAnnotation(EdmAction.class);
      if (actionDescribtion != null && method.equals(m.getName())) {
        return new IntermediateJavaAction(new JPAEdmNameBuilder(PUNIT_NAME), actionDescribtion, m, helper.schema);
      }
    }
    return null;
  }
}
