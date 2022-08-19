package sttp.apispec.openapi

import sttp.apispec.{
  ExampleValue,
  ExtensionValue,
  ExternalDocumentation,
  Reference,
  ReferenceOr,
  Schema,
  SecurityRequirement,
  SecurityScheme,
  Tag
}

import scala.collection.immutable.ListMap

final case class OpenAPI(
    openapi: String = "3.1.0",
    info: Info,
    jsonSchemaDialect: Option[String] = None,
    tags: List[Tag] = Nil,
    servers: List[Server] = Nil,
    paths: Paths = Paths.Empty,
    webhooks: Option[Map[String, ReferenceOr[PathItem]]] = None,
    components: Option[Components] = None,
    security: List[SecurityRequirement] = Nil,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def addPathItem(path: String, pathItem: PathItem): OpenAPI = {
    copy(paths = paths.addPathItem(path, pathItem))
  }
  def openapi(updated: String): OpenAPI = copy(openapi = updated)
  def info(updated: Info): OpenAPI = copy(info = updated)
  def paths(updated: Paths): OpenAPI = copy(paths = updated)
  def components(updated: Components): OpenAPI = copy(components = Some(updated))

  def servers(s: List[Server]): OpenAPI = copy(servers = s)
  def addServer(server: Server): OpenAPI = copy(servers = servers ++ List(server))
  def addServer(url: String): OpenAPI = copy(servers = servers ++ List(Server(url)))

  def tags(t: List[Tag]): OpenAPI = copy(tags = t)

  def jsonSchemaDialect(d: Option[String]): OpenAPI = copy(jsonSchemaDialect = d)

  def webhooks(wh: Option[Map[String, ReferenceOr[PathItem]]]): OpenAPI = copy(webhooks = wh)

  def security(updated: List[SecurityRequirement]): OpenAPI = copy(security = updated)
  def addSecurity(updated: SecurityRequirement): OpenAPI = copy(security = security ++ List(updated))
  def addExtension(key: String, value: ExtensionValue): OpenAPI = copy(extensions = extensions.updated(key, value))
  def extensions(updated: ListMap[String, ExtensionValue]): OpenAPI = copy(extensions = updated)
}

final case class Info(
    title: String,
    version: String,
    summary: Option[String] = None,
    description: Option[String] = None,
    termsOfService: Option[String] = None,
    contact: Option[Contact] = None,
    license: Option[License] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def title(updated: String): Info = copy(title = updated)
  def version(updated: String): Info = copy(version = updated)
  def summary(updated: String): Info = copy(summary = Some(updated))
  def description(updated: String): Info = copy(description = Some(updated))
  def termsOfService(updated: String): Info = copy(termsOfService = Some(updated))
  def contact(updated: Contact): Info = copy(contact = Some(updated))
  def license(updated: License): Info = copy(license = Some(updated))

  def addExtension(key: String, value: ExtensionValue): Info = copy(extensions = extensions.updated(key, value))
  def extensions(updated: ListMap[String, ExtensionValue]): Info = copy(extensions = updated)
}

final case class Contact(
    name: Option[String] = None,
    email: Option[String] = None,
    url: Option[String] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def name(updated: String): Contact = copy(name = Some(updated))
  def email(updated: String): Contact = copy(email = Some(updated))
  def url(updated: String): Contact = copy(url = Some(updated))
  def addExtension(key: String, value: ExtensionValue): Contact = copy(extensions = extensions.updated(key, value))
  def extensions(updated: ListMap[String, ExtensionValue]): Contact = copy(extensions = updated)
}

object Contact {
  val Empty: Contact = Contact()
}

final case class License(
    name: String,
    url: Option[String],
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def name(updated: String): License = copy(name = updated)
  def url(updated: String): License = copy(url = Some(updated))
  def addExtension(key: String, value: ExtensionValue): License = copy(extensions = extensions.updated(key, value))
  def extensions(updated: ListMap[String, ExtensionValue]): License = copy(extensions = updated)
}

final case class Server(
    url: String,
    description: Option[String] = None,
    variables: Option[ListMap[String, ServerVariable]] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def url(updated: String): Server = copy(url = updated)
  def addExtension(key: String, value: ExtensionValue): Server = copy(extensions = extensions.updated(key, value))

  def description(d: String): Server = copy(description = Some(d))
  def variables(vars: (String, ServerVariable)*): Server = copy(variables = Some(ListMap(vars: _*)))
  def extensions(updated: ListMap[String, ExtensionValue]): Server = copy(extensions = updated)
}

final case class ServerVariable(
    `enum`: Option[List[String]],
    default: String,
    description: Option[String],
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  require(
    `enum`.fold(true)(_.contains(default)),
    "ServerVariable#default must be one of the values in enum if enum is defined"
  )

  def `enum`(d: List[String]): ServerVariable = {
    require(d.contains(default), "ServerVariable#default must be one of the values in enum if enum is defined")
    copy(`enum` = Some(d))
  }
  def default(d: String): ServerVariable = {
    require(
      `enum`.fold(true)(_.contains(d)),
      "ServerVariable#default must be one of the values in enum if enum is defined"
    )
    copy(default = d)
  }

  def description(d: String): ServerVariable = copy(description = Some(d))
  def extensions(updated: ListMap[String, ExtensionValue]): ServerVariable = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): ServerVariable =
    copy(extensions = extensions.updated(key, value))
}

final case class Components(
    schemas: ListMap[String, ReferenceOr[Schema]] = ListMap.empty,
    responses: ListMap[String, ReferenceOr[Response]] = ListMap.empty,
    parameters: ListMap[String, ReferenceOr[Parameter]] = ListMap.empty,
    examples: ListMap[String, ReferenceOr[Example]] = ListMap.empty,
    requestBodies: ListMap[String, ReferenceOr[RequestBody]] = ListMap.empty,
    headers: ListMap[String, ReferenceOr[Header]] = ListMap.empty,
    securitySchemes: ListMap[String, ReferenceOr[SecurityScheme]] = ListMap.empty,
    links: ListMap[String, ReferenceOr[Link]] = ListMap.empty,
    callbacks: ListMap[String, ReferenceOr[Callback]] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def addSchema(key: String, schema: Schema): Components = copy(schemas = schemas.updated(key, Right(schema)))
  def getLocalSchema(key: String): Option[Schema] = schemas.get(key).flatMap(_.toOption)
  def getReferenceToSchema(key: String): Option[Reference] =
    schemas.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/schemas/$key")))
  def addSecurityScheme(key: String, scheme: SecurityScheme): Components =
    copy(securitySchemes = securitySchemes.updated(key, Right(scheme)))
  def getLocalSecurityScheme(key: String): Option[SecurityScheme] = securitySchemes.get(key).flatMap(_.toOption)
  def getReferenceToSecurityScheme(key: String): Option[Reference] =
    securitySchemes.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/securitySchemes/$key")))
  def schemas(updated: ListMap[String, ReferenceOr[Schema]]): Components = copy(schemas = updated)
  def securitySchemes(updated: ListMap[String, ReferenceOr[SecurityScheme]]): Components =
    copy(securitySchemes = updated)
  def addResponse(key: String, response: Response): Components =
    copy(responses = responses.updated(key, Right(response)))
  def getLocalResponse(key: String): Option[Response] = responses.get(key).flatMap(_.toOption)
  def getReferenceToResponse(key: String): Option[Reference] =
    responses.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/responses/$key")))
  def responses(updated: ListMap[String, ReferenceOr[Response]]): Components = copy(responses = updated)
  def addParameter(key: String, parameter: Parameter): Components =
    copy(parameters = parameters.updated(key, Right(parameter)))
  def getLocalParameter(key: String): Option[Parameter] = parameters.get(key).flatMap(_.toOption)
  def getReferenceToParameter(key: String): Option[Reference] =
    parameters.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/parameters/$key")))
  def parameters(updated: ListMap[String, ReferenceOr[Parameter]]): Components = copy(parameters = updated)
  def addExample(key: String, example: Example): Components = copy(examples = examples.updated(key, Right(example)))
  def getLocalExample(key: String): Option[Example] = examples.get(key).flatMap(_.toOption)
  def getReferenceToExample(key: String): Option[Reference] =
    examples.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/examples/$key")))
  def examples(updated: ListMap[String, ReferenceOr[Example]]): Components = copy(examples = updated)
  def addRequestBody(key: String, requestBody: RequestBody): Components =
    copy(requestBodies = requestBodies.updated(key, Right(requestBody)))
  def getLocalRequestBody(key: String): Option[RequestBody] = requestBodies.get(key).flatMap(_.toOption)
  def getReferenceToRequestBody(key: String): Option[Reference] =
    requestBodies.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/requestBodies/$key")))
  def requestBodies(updated: ListMap[String, ReferenceOr[RequestBody]]): Components = copy(requestBodies = updated)
  def addHeader(key: String, header: Header): Components = copy(headers = headers.updated(key, Right(header)))
  def getLocalHeader(key: String): Option[Header] = headers.get(key).flatMap(_.toOption)
  def getReferenceToHeader(key: String): Option[Reference] =
    headers.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/headers/$key")))
  def headers(updated: ListMap[String, ReferenceOr[Header]]): Components = copy(headers = updated)
  def addLink(key: String, link: Link): Components = copy(links = links.updated(key, Right(link)))
  def getLocalLink(key: String): Option[Link] = links.get(key).flatMap(_.toOption)
  def getReferenceToLink(key: String): Option[Reference] =
    links.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/links/$key")))
  def links(updated: ListMap[String, ReferenceOr[Link]]): Components = copy(links = updated)
  def addCallback(key: String, callback: Callback): Components =
    copy(callbacks = callbacks.updated(key, Right(callback)))
  def getLocalCallback(key: String): Option[Callback] = callbacks.get(key).flatMap(_.toOption)
  def getReferenceToCallback(key: String): Option[Reference] =
    callbacks.get(key).map(refOr => refOr.fold(identity, _ => Reference(s"#/components/callbacks/$key")))
  def callbacks(updated: ListMap[String, ReferenceOr[Callback]]): Components = copy(callbacks = updated)
  def extensions(updated: ListMap[String, ExtensionValue]): Components = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Components = copy(extensions = extensions.updated(key, value))
}

object Components {
  val Empty: Components = Components()
}

final case class Paths(
    pathItems: ListMap[String, PathItem] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {

  def addPathItem(path: String, pathItem: PathItem): Paths = {
    val pathItem2 = pathItems.get(path) match {
      case None           => pathItem
      case Some(existing) => existing.mergeWith(pathItem)
    }
    val newPathItems = pathItems + (path -> pathItem2)
    copy(pathItems = newPathItems)
  }
  def pathItems(updated: ListMap[String, PathItem]): Paths = copy(pathItems = updated)
  def extensions(updated: ListMap[String, ExtensionValue]): Paths = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Paths = copy(extensions = extensions.updated(key, value))
}

object Paths {
  val Empty: Paths = Paths(pathItems = ListMap.empty, extensions = ListMap.empty)
}

final case class PathItem(
    ref: Option[Reference] = None,
    summary: Option[String] = None,
    description: Option[String] = None,
    get: Option[Operation] = None,
    put: Option[Operation] = None,
    post: Option[Operation] = None,
    delete: Option[Operation] = None,
    options: Option[Operation] = None,
    head: Option[Operation] = None,
    patch: Option[Operation] = None,
    trace: Option[Operation] = None,
    servers: List[Server] = List.empty,
    parameters: List[ReferenceOr[Parameter]] = List.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def mergeWith(other: PathItem): PathItem = {
    PathItem(
      ref = None,
      summary = None,
      description = None,
      get = get.orElse(other.get),
      put = put.orElse(other.put),
      post = post.orElse(other.post),
      delete = delete.orElse(other.delete),
      options = options.orElse(other.options),
      head = head.orElse(other.head),
      patch = patch.orElse(other.patch),
      trace = trace.orElse(other.trace),
      servers = List.empty,
      parameters = List.empty
    )
  }

  def ref(updated: Reference): PathItem = copy(ref = Some(updated))
  def summary(updated: String): PathItem = copy(summary = Some(updated))
  def description(updated: String): PathItem = copy(description = Some(updated))
  def get(updated: Operation): PathItem = copy(get = Some(updated))
  def put(updated: Operation): PathItem = copy(put = Some(updated))
  def post(updated: Operation): PathItem = copy(post = Some(updated))
  def delete(updated: Operation): PathItem = copy(delete = Some(updated))
  def deleteNoBody: PathItem = delete(Operation.Empty)
  def options(updated: Operation): PathItem = copy(options = Some(updated))
  def head(updated: Operation): PathItem = copy(head = Some(updated))
  def headNoBody: PathItem = head(Operation.Empty)
  def patch(updated: Operation): PathItem = copy(patch = Some(updated))
  def trace(updated: Operation): PathItem = copy(trace = Some(updated))
  def addServer(server: Server): PathItem = copy(servers = servers ++ List(server))
  def servers(updated: List[Server]): PathItem = copy(servers = updated)
  def parameters(updated: List[ReferenceOr[Parameter]]): PathItem = copy(parameters = updated)
  def addParameter(param: Parameter): PathItem = copy(parameters = parameters ++ List(Right(param)))
  def extensions(updated: ListMap[String, ExtensionValue]): PathItem = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): PathItem = copy(extensions = extensions.updated(key, value))
}

final case class Operation(
    tags: List[String] = List.empty,
    summary: Option[String] = None,
    description: Option[String] = None,
    externalDocs: Option[ExternalDocumentation] = None,
    operationId: Option[String] = None,
    parameters: List[ReferenceOr[Parameter]] = List.empty,
    requestBody: Option[ReferenceOr[RequestBody]] = None,
    responses: Responses = Responses.Empty,
    callbacks: ListMap[String, ReferenceOr[Callback]] = ListMap.empty,
    deprecated: Option[Boolean] = None,
    security: List[SecurityRequirement] = List.empty,
    servers: List[Server] = List.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {

  def addTag(updated: String): Operation = copy(tags = tags ++ List(updated))
  def summary(updated: String): Operation = copy(summary = Some(updated))
  def description(updated: String): Operation = copy(description = Some(updated))
  def externalDocs(updated: ExternalDocumentation): Operation = copy(externalDocs = Some(updated))
  def operationId(updated: String): Operation = copy(operationId = Some(updated))
  def requestBody(updated: RequestBody): Operation = copy(requestBody = Some(Right(updated)))
  def addParameter(param: Parameter): Operation = copy(parameters = parameters ++ List(Right(param)))
  def addResponse(status: Int, updated: Response): Operation = copy(responses = responses.addResponse(status, updated))
  def addDefaultResponse(updated: Response): Operation = copy(responses = responses.addDefault(updated))
  def addCallback(key: String, callback: Callback): Operation =
    copy(callbacks = callbacks.updated(key, Right(callback)))
  def addCallbackReference(key: String, referenceKey: String): Operation =
    copy(callbacks = callbacks.updated(key, Left(Reference(s"#/components/callbacks/$referenceKey"))))
  def callbacks(updated: ListMap[String, ReferenceOr[Callback]]): Operation = copy(callbacks = updated)
  def deprecated(updated: Boolean): Operation = copy(deprecated = Some(updated))
  def security(updated: List[SecurityRequirement]): Operation = copy(security = updated)
  def addSecurity(updated: SecurityRequirement): Operation = copy(security = security ++ List(updated))
  def servers(updated: List[Server]): Operation = copy(servers = updated)
  def addServer(server: Server): Operation = copy(servers = servers ++ List(server))
  def extensions(updated: ListMap[String, ExtensionValue]): Operation = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Operation = copy(extensions = extensions.updated(key, value))
}

object Operation {
  val Empty: Operation = Operation()
}

final case class Parameter(
    name: String,
    in: ParameterIn,
    description: Option[String] = None,
    required: Option[Boolean] = None,
    deprecated: Option[Boolean] = None,
    allowEmptyValue: Option[Boolean] = None,
    style: Option[ParameterStyle] = None,
    explode: Option[Boolean] = None,
    allowReserved: Option[Boolean] = None,
    schema: Option[ReferenceOr[Schema]],
    example: Option[ExampleValue] = None,
    examples: ListMap[String, ReferenceOr[Example]] = ListMap.empty,
    content: ListMap[String, MediaType] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def name(updated: String): Parameter = copy(name = updated)
  def in(updated: ParameterIn): Parameter = copy(in = updated)
  def description(updated: String): Parameter = copy(description = Some(updated))
  def required(updated: Boolean): Parameter = copy(required = Some(updated))
  def deprecated(updated: Boolean): Parameter = copy(deprecated = Some(updated))
  def allowEmptyValue(updated: Boolean): Parameter = copy(allowEmptyValue = Some(updated))
  def style(updated: ParameterStyle): Parameter = copy(style = Some(updated))
  def explode(updated: Boolean): Parameter = copy(explode = Some(updated))
  def allowReserved(updated: Boolean): Parameter = copy(allowReserved = Some(updated))
  def schema(updated: Schema): Parameter = copy(schema = Some(Right(updated)))
  def example(updated: ExampleValue): Parameter = copy(example = Some(updated))
  def examples(updated: ListMap[String, ReferenceOr[Example]]): Parameter = copy(examples = updated)
  def addExample(key: String, updated: Example): Parameter = copy(examples = examples.updated(key, Right(updated)))
  def addMediaType(contentType: String, mediaType: MediaType): Parameter =
    copy(content = content.updated(contentType, mediaType))
  def extensions(updated: ListMap[String, ExtensionValue]): Parameter = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Parameter = copy(extensions = extensions.updated(key, value))
}

sealed abstract class ParameterIn(val value: String)
object ParameterIn {
  case object Query extends ParameterIn("query")
  case object Header extends ParameterIn("header")
  case object Path extends ParameterIn("path")
  case object Cookie extends ParameterIn("cookie")
}

sealed abstract class ParameterStyle(val value: String)
object ParameterStyle extends Enumeration {
  case object Simple extends ParameterStyle("simple")
  case object Form extends ParameterStyle("form")
  case object Matrix extends ParameterStyle("matrix")
  case object Label extends ParameterStyle("label")
  case object SpaceDelimited extends ParameterStyle("spaceDelimited")
  case object PipeDelimited extends ParameterStyle("pipeDelimited")
  case object DeepObject extends ParameterStyle("deepObject")
}

final case class RequestBody(
    description: Option[String] = None,
    content: ListMap[String, MediaType] = ListMap.empty,
    required: Option[Boolean] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def content(updated: ListMap[String, MediaType]): RequestBody = copy(content = updated)
  def addMediaType(contentType: String, updated: MediaType): RequestBody =
    copy(content = content.updated(contentType, updated))
  def extensions(updated: ListMap[String, ExtensionValue]): RequestBody = copy(extensions = updated)
  def required(boolean: Boolean): RequestBody = copy(required = Some(boolean))
  def addExtension(key: String, value: ExtensionValue): RequestBody = copy(extensions = extensions.updated(key, value))
}
object RequestBody {
  val Empty: RequestBody = RequestBody()
}

final case class MediaType(
    schema: Option[ReferenceOr[Schema]] = None,
    example: Option[ExampleValue] = None,
    examples: ListMap[String, ReferenceOr[Example]] = ListMap.empty,
    encoding: ListMap[String, Encoding] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def schema(updated: Schema): MediaType = copy(schema = Some(Right(updated)))
  def example(updated: ExampleValue): MediaType = copy(example = Some(updated))
  def examples(updated: ListMap[String, ReferenceOr[Example]]): MediaType = copy(examples = updated)
  def addExample(key: String, updated: Example): MediaType = copy(examples = examples.updated(key, Right(updated)))
  def extensions(updated: ListMap[String, ExtensionValue]): MediaType = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): MediaType = copy(extensions = extensions.updated(key, value))
}

object MediaType {
  val Empty: MediaType = MediaType()
}

final case class Encoding(
    contentType: Option[String] = None,
    headers: ListMap[String, ReferenceOr[Header]] = ListMap.empty,
    style: Option[ParameterStyle] = None,
    explode: Option[Boolean] = None,
    allowReserved: Option[Boolean] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def contentType(updated: String): Encoding = copy(contentType = Some(updated))
  def headers(updated: ListMap[String, ReferenceOr[Header]]): Encoding = copy(headers = updated)
  def addHeader(key: String, header: Header): Encoding = copy(headers = headers.updated(key, Right(header)))
  def style(updated: ParameterStyle): Encoding = copy(style = Some(updated))
  def explode(updated: Boolean): Encoding = copy(explode = Some(updated))
  def allowReserved(updated: Boolean): Encoding = copy(allowReserved = Some(updated))
  def extensions(updated: ListMap[String, ExtensionValue]): Encoding = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Encoding = copy(extensions = extensions.updated(key, value))
}

object Encoding {
  val Empty: Encoding = Encoding()
}

sealed trait ResponsesKey
case object ResponsesDefaultKey extends ResponsesKey
final case class ResponsesCodeKey(code: Int) extends ResponsesKey
final case class ResponsesRangeKey(range: Int) extends ResponsesKey

final case class Response(
    description: String = "",
    headers: ListMap[String, ReferenceOr[Header]] = ListMap.empty,
    content: ListMap[String, MediaType] = ListMap.empty,
    links: ListMap[String, ReferenceOr[Link]] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def description(updated: String): Response = copy(description = updated)
  def addHeader(key: String, header: Header): Response = copy(headers = headers.updated(key, Right(header)))
  def addMediaType(contentType: String, updated: MediaType): Response =
    copy(content = content.updated(contentType, updated))
  def headers(updated: ListMap[String, ReferenceOr[Header]]): Response = copy(headers = updated)
  def content(updated: ListMap[String, MediaType]): Response = copy(content = updated)
  def addLink(key: String, link: Link): Response = copy(links = links.updated(key, Right(link)))
  def links(updated: ListMap[String, ReferenceOr[Link]]): Response = copy(links = updated)
  def extensions(updated: ListMap[String, ExtensionValue]): Response = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Response = copy(extensions = extensions.updated(key, value))
}

object Response {
  val Empty: Response = Response()
}

final case class Responses(
    responses: ListMap[ResponsesKey, ReferenceOr[Response]] = ListMap.empty,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def responses(updated: ListMap[ResponsesKey, ReferenceOr[Response]]): Responses = copy(responses = updated)
  def extensions(updated: ListMap[String, ExtensionValue]): Responses = copy(extensions = updated)
  def addResponse(status: Int, response: Response): Responses =
    copy(responses = responses.updated(ResponsesCodeKey(status), Right(response)))
  def addDefault(response: Response): Responses =
    copy(responses = responses.updated(ResponsesDefaultKey, Right(response)))
  def addExtension(key: String, value: ExtensionValue): Responses = copy(extensions = extensions.updated(key, value))
}

object Responses {
  val Empty: Responses = Responses(ListMap.empty, ListMap.empty)
}

final case class Example(
    summary: Option[String] = None,
    description: Option[String] = None,
    value: Option[ExampleValue] = None,
    externalValue: Option[String] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def summary(updated: String): Example = copy(summary = Some(updated))
  def description(updated: String): Example = copy(description = Some(updated))
  def value(updated: ExampleValue): Example = copy(value = Some(updated))
  def externalValue(updated: String): Example = copy(externalValue = Some(updated))
  def extensions(updated: ListMap[String, ExtensionValue]): Example = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Example = copy(extensions = extensions.updated(key, value))
}

object Example {
  val Empty: Example = Example()
}

final case class Header(
    description: Option[String] = None,
    required: Option[Boolean] = None,
    deprecated: Option[Boolean] = None,
    allowEmptyValue: Option[Boolean] = None,
    style: Option[ParameterStyle] = None,
    explode: Option[Boolean] = None,
    allowReserved: Option[Boolean] = None,
    schema: Option[ReferenceOr[Schema]] = None,
    example: Option[ExampleValue] = None,
    examples: ListMap[String, ReferenceOr[Example]] = ListMap.empty,
    content: ListMap[String, MediaType] = ListMap.empty
) {
  def description(updated: String): Header = copy(description = Some(updated))
  def required(updated: Boolean): Header = copy(required = Some(updated))
  def deprecated(updated: Boolean): Header = copy(deprecated = Some(updated))
  def allowEmptyValue(updated: Boolean): Header = copy(allowEmptyValue = Some(updated))
  def style(updated: ParameterStyle): Header = copy(style = Some(updated))
  def explode(updated: Boolean): Header = copy(explode = Some(updated))
  def allowReserved(updated: Boolean): Header = copy(allowReserved = Some(updated))
  def schema(updated: Schema): Header = copy(schema = Some(Right(updated)))
  def example(updated: ExampleValue): Header = copy(example = Some(updated))
  def examples(updated: ListMap[String, ReferenceOr[Example]]): Header = copy(examples = updated)
  def addExample(key: String, updated: Example): Header = copy(examples = examples.updated(key, Right(updated)))
  def addMediaType(contentType: String, mediaType: MediaType): Header =
    copy(content = content.updated(contentType, mediaType))
}

object Header {
  val Empty: Header = Header()
}

final case class Link(
    operationRef: Option[String] = None,
    operationId: Option[String] = None,
    parameters: ListMap[String, String] = ListMap.empty,
    requestBody: Option[String] = None,
    description: Option[String] = None,
    server: Option[Server] = None,
    extensions: ListMap[String, ExtensionValue] = ListMap.empty
) {
  def operationRef(updated: String): Link = copy(operationRef = Some(updated))
  def operationId(updated: String): Link = copy(operationId = Some(updated))
  def parameters(updated: ListMap[String, String]): Link = copy(parameters = updated)
  def addParameter(key: String, value: String): Link = copy(parameters = parameters.updated(key, value))
  def description(updated: String): Link = copy(description = Some(updated))
  def server(updated: Server): Link = copy(server = Some(updated))
  def extensions(updated: ListMap[String, ExtensionValue]): Link = copy(extensions = updated)
  def addExtension(key: String, value: ExtensionValue): Link = copy(extensions = extensions.updated(key, value))
}

object Link {
  val Empty: Link = Link()
}

final case class Callback(pathItems: ListMap[String, ReferenceOr[PathItem]] = ListMap.empty) {
  def paths(updated: ListMap[String, ReferenceOr[PathItem]]): Callback = copy(pathItems = updated)
  def addPathItem(key: String, value: PathItem): Callback = copy(pathItems = pathItems.updated(key, Right(value)))
}

object Callback {
  val Empty: Callback = Callback()
}
