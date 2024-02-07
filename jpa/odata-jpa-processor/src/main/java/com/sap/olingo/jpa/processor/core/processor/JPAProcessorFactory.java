package com.sap.olingo.jpa.processor.core.processor;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.QUERY_SERVER_DRIVEN_PAGING_GONE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.QUERY_SERVER_DRIVEN_PAGING_NOT_IMPLEMENTED;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.modify.JPAConversionHelper;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;
import com.sap.olingo.jpa.processor.core.query.JPAJoinQuery;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializerFactory;
import org.apache.olingo.server.core.uri.queryoption.SkipOptionImpl;
import org.apache.olingo.server.core.uri.queryoption.TopOptionImpl;

public final class JPAProcessorFactory {
  private final JPAODataSessionContextAccess sessionContext;
  private final JPASerializerFactory serializerFactory;
  private final OData odata;
  private final ServiceMetadata serviceMetadata;

  public JPAProcessorFactory(final OData odata, final ServiceMetadata serviceMetadata,
      final JPAODataSessionContextAccess context) {
    super();
    this.sessionContext = context;
    this.serializerFactory = new JPASerializerFactory(odata, serviceMetadata, context);
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  public JPACUDRequestProcessor createCUDRequestProcessor(final UriInfo uriInfo, final ContentType responseFormat,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPAODataInternalRequestContext(uriInfo, serializerFactory
        .createCUDSerializer(responseFormat, uriInfo, Optional.ofNullable(header.get(HttpHeader.ODATA_MAX_VERSION))),
        context, header);

    return new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, new JPAConversionHelper());
  }

  public JPACUDRequestProcessor createCUDRequestProcessor(final UriInfo uriInfo,
      final JPAODataRequestContextAccess context, final Map<String, List<String>> header) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPAODataInternalRequestContext(uriInfo, context, header);

    return new JPACUDRequestProcessor(odata, serviceMetadata, requestContext, new JPAConversionHelper());
  }

  public JPAActionRequestProcessor createActionProcessor(final UriInfo uriInfo, final ContentType responseFormat,
      final Map<String, List<String>> header, final JPAODataRequestContextAccess context) throws ODataException {

    final JPAODataRequestContextAccess requestContext = new JPAODataInternalRequestContext(uriInfo,
        responseFormat != null ? serializerFactory.createSerializer(responseFormat, uriInfo, Optional.ofNullable(header
            .get(HttpHeader.ODATA_MAX_VERSION))) : null, context, header);

    return new JPAActionRequestProcessor(odata, requestContext);

  }

  public JPARequestProcessor createProcessor(final UriInfo uriInfo, final ContentType responseFormat,
      final Map<String, List<String>> header, final JPAODataRequestContextAccess context) throws ODataException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResource lastItem = resourceParts.get(resourceParts.size() - 1);
    final JPAODataPage page = getPage(header, uriInfo, context);
    JPAODataRequestContextAccess requestContext;
    try {
      requestContext = new JPAODataInternalRequestContext(page, serializerFactory
          .createSerializer(responseFormat, page.uriInfo(), Optional.ofNullable(header.get(
              HttpHeader.ODATA_MAX_VERSION))), context, header);
    } catch (final ODataJPAIllegalAccessException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    switch (lastItem.getKind()) {
      case count:
        return new JPACountRequestProcessor(odata, requestContext);
      case function:
        checkFunctionPathSupported(resourceParts);
        return new JPAFunctionRequestProcessor(odata, requestContext);
      case complexProperty, primitiveProperty, navigationProperty, entitySet, singleton, value:
        checkNavigationPathSupported(resourceParts);
        return new JPANavigationRequestProcessor(odata, serviceMetadata, requestContext);
      default:
        throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED, lastItem.getKind().toString());
    }
  }

  private void checkFunctionPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    if (resourceParts.size() > 2)
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_FUNC_WITH_NAVI,
          HttpStatusCode.NOT_IMPLEMENTED);
  }

  private void checkNavigationPathSupported(final List<UriResource> resourceParts) throws ODataApplicationException {
    for (final UriResource resourceItem : resourceParts) {
      if (resourceItem.getKind() != UriResourceKind.complexProperty
          && resourceItem.getKind() != UriResourceKind.primitiveProperty
          && resourceItem.getKind() != UriResourceKind.navigationProperty
          && resourceItem.getKind() != UriResourceKind.entitySet
          && resourceItem.getKind() != UriResourceKind.singleton
          && resourceItem.getKind() != UriResourceKind.value)
        throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
            HttpStatusCode.NOT_IMPLEMENTED, resourceItem.getKind().toString());
    }
  }

  private JPAODataPage getPage(final Map<String, List<String>> headers, final UriInfo uriInfo,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    JPAODataPage page = new JPAODataPage(uriInfo, 0, Integer.MAX_VALUE, null);
    // Server-Driven-Paging
    if (serverDrivenPaging(uriInfo)) {
      final String skipToken = skipToken(uriInfo);
      if (skipToken != null && !skipToken.isEmpty()) {
        page = sessionContext.getPagingProvider().getNextPage(skipToken);
        if (page == null)
          throw new ODataJPAProcessorException(QUERY_SERVER_DRIVEN_PAGING_GONE, HttpStatusCode.GONE, skipToken);
      } else {
        final JPACountQuery countQuery = new JPAJoinQuery(odata, new JPAODataInternalRequestContext(uriInfo,
            requestContext, headers));
        final Integer preferredPageSize = getPreferredPageSize(headers);
        final JPAODataPage firstPage = sessionContext.getPagingProvider().getFirstPage(uriInfo, preferredPageSize,
            countQuery, requestContext.getEntityManager());
        page = firstPage != null ? firstPage : page;
      }
    }

    boolean hasTileIDFilter = uriInfo.getFilterOption() != null &&
            uriInfo.getFilterOption().getExpression() != null &&
            uriInfo.getFilterOption().getExpression().toString().contains("TileID");

    boolean containsGeoIntersectsFilter = uriInfo.getFilterOption() != null &&
            uriInfo.getFilterOption().getExpression() != null &&
            uriInfo.getFilterOption().getExpression().toString().contains("geo.intersects");

    boolean hasRequiredFilters = uriInfo.getFilterOption() != null &&
            (hasTileIDFilter || containsGeoIntersectsFilter);

    // Verifica la presenza di filtri necessari
    if (uriInfo.getFilterOption() != null && !hasRequiredFilters) {
      throw new ODataException("Errore: la query deve contenere il filtro TileID o geo.intersects");
    }

    TopOptionImpl topOption = uriInfo.getSystemQueryOptions().stream()
            .filter(option -> option.getKind() == SystemQueryOptionKind.TOP)
            .map(option -> (TopOptionImpl) option)
            .findFirst()
            .orElse(new TopOptionImpl().setValue(20));

    topOption.setText(String.valueOf(topOption.getValue()));

    SkipOptionImpl skipOption = uriInfo.getSystemQueryOptions().stream()
            .filter(option -> option.getKind() == SystemQueryOptionKind.SKIP)
            .map(option -> (SkipOptionImpl) option)
            .findFirst()
            .orElse(new SkipOptionImpl().setValue(0));

    page = new JPAODataPage(uriInfo, skipOption.getValue(),  topOption.getValue(),null);
    return page;
  }

  private Integer getPreferredPageSize(final Map<String, List<String>> headers) throws ODataJPAProcessorException {

    final List<String> preferredHeaders = getHeader("Prefer", headers);
    if (preferredHeaders != null) {
      for (final String header : preferredHeaders) {
        if (header.startsWith("odata.maxpagesize")) {
          try {
            return Integer.valueOf((header.split("=")[1]));
          } catch (final NumberFormatException e) {
            throw new ODataJPAProcessorException(e, HttpStatusCode.BAD_REQUEST);
          }
        }
      }
    }
    return null;
  }

  private boolean serverDrivenPaging(final UriInfo uriInfo) throws ODataJPAProcessorException {

    for (final SystemQueryOption option : uriInfo.getSystemQueryOptions()) {
      if (option.getKind() == SystemQueryOptionKind.SKIPTOKEN
          && sessionContext.getPagingProvider() == null)
        throw new ODataJPAProcessorException(QUERY_SERVER_DRIVEN_PAGING_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED);
    }
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    return sessionContext.getPagingProvider() != null
        && resourceParts.get(resourceParts.size() - 1).getKind() != UriResourceKind.function;
  }

  private String skipToken(final UriInfo uriInfo) {
    for (final SystemQueryOption option : uriInfo.getSystemQueryOptions()) {
      if (option.getKind() == SystemQueryOptionKind.SKIPTOKEN)
        return option.getText();
    }
    return null;
  }

  private List<String> getHeader(final String name, final Map<String, List<String>> headers) {
    for (final Entry<String, List<String>> header : headers.entrySet()) {
      if (header.getKey().equalsIgnoreCase(name)) {
        return header.getValue();
      }
    }
    return Collections.emptyList();
  }
}
