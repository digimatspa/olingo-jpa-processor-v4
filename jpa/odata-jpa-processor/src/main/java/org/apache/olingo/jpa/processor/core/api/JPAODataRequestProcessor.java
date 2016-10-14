package org.apache.olingo.jpa.processor.core.api;

import java.io.InputStream;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.processor.JPADeleteProcessor;
import org.apache.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPARequestProcessor;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPAODataRequestProcessor implements PrimitiveValueProcessor,
    ComplexProcessor, CountEntityCollectionProcessor, EntityProcessor, MediaEntityProcessor {
  private final EntityManager em;
  private final JPAODataSessionContextAccess context;
  private JPAProcessorFactory factory;
  private OData odata;
  private ServiceMetadata serviceMetadata;

  public JPAODataRequestProcessor(final JPAODataSessionContextAccess context, final EntityManager em) {
    super();
    this.em = em;
    this.context = context;
  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.factory = new JPAProcessorFactory(odata, serviceMetadata, context);
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public void countEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, ContentType.TEXT_PLAIN);
      p.retrieveData(request, response, ContentType.TEXT_PLAIN);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();

    Entity createdEntity = null;
    // Entity createdEntity = storage.createEntityData(edmEntitySet, requestEntity);

    // 3. serialize the response (we have to return the created entity)
    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
    // expand and select currently not supported
    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

    ODataSerializer serializer = this.odata.createSerializer(responseFormat);
    SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

    // 4. configure the response object
    response.setContent(serializedResponse.getContent());
    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void createMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Organizations('4')/Address
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    JPADeleteProcessor p;
    try {
      p = factory.createDeleteProcessor(em, uriInfo);
      p.deleteEntity(response);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }

  }

  @Override
  public void deletePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Organizations('4')/Address/Country
    // https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306
    // 11.4.9.2 Set a Value to Null:
    // A successful DELETE request to the edit URL for a structural property, or to the edit URL of the raw value of a
    // primitive property, sets the property to null. The request body is ignored and should be empty. A DELETE request
    // to a non-nullable value MUST fail and the service respond with 400 Bad Request or other appropriate error.
    // The same rules apply whether the target is the value of a regular property or the value of a dynamic property. A
    // missing dynamic property is defined to be the same as a dynamic property with value null. All dynamic properties
    // are nullable.On success, the service MUST respond with 204 No Content and an empty body.
    //
    // Nullable checked by Olingo Core
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deletePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: ../$value
    // https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752305
    // 11.4.8.2 Deleting Stream Values:
    // A successful DELETE request to the edit URL of a stream property attempts to set the property to null and results
    // in an error if the property is non-nullable. Attempting to request a stream property whose value is null results
    // in 204 No Content.
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void readComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }

  }

  @Override
  public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readPrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }

  }

  @Override
  public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void updateComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updatePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

}
