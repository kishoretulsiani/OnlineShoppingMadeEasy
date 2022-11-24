package org.shopping.company.gateway.constants;


public class GatewayConstant {

    //32KB header size
    public static final int MAX_HEADER_SIZE = 32768;
    public static final String REQUEST_BODY = "request_body";
    public static final String X_TRACKING_ID = "x-tracking-id";
    public static final String TRACKING_ID = "tracking_id";
    public static final String RESPONSE_PAYLOAD = "response_payload";
    public static final String HTTP_STATUS = "http_status";
    public static final String USER_AUTHORIZATION = "User-Authorization";
    public static final String COOKIE = "Cookie";
    public static final String ALLOW_EXPOSED_HEADERS = "tracking_id,x-amex-tracking-id,x-user-device";
    public static final String ALLOW_HEADERS = "tracking_id,x-amex-tracking-id,Authorization,Content-Type,x-user-device,device_type,x-browser-user-agent,x_forwarded_for ";
    public static final String ACCESS_CONTROL_EXPOSED_HEADERS = "Access-Control-Expose-Headers";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String APPLICATION_JSON = "application/json";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String ACCESS_CONTROL_ALLOW_METHOD = "Access-Control-Allow-Methods";
    public static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    public static final String ALLOW_METHOD_METHODS = "GET, POST, OPTIONS, PUT";
    public static final String ALLOW_ORIGIN = "*";
    public static final String ALLOW_CREDENTIALS = "false";
    public static final String ORIGIN = "Origin";
    public static final String REQUEST_HEADERS = "request_headers";
    public static final String REQUEST_QUERY_PARAMETERS = "request_query_parameter";
    public static final String REQUEST_PATH_PARAMETERS = "request_path_parameter";
    public static final String REQUEST_URI = "request_uri";
    public static final String INCOMING_REQUEST_DATA = "incoming_request_data";
    public static final String JWT_COOKIE_NAME = "JWT_COOKIE_NAME";
    public static final int HTTP_STATUS_200 = 200;
    public static final String ERROR_CODE_MAPPING_FILE_NAME = "errorcodemappings.properties";
    public static final String HEALTH_CHECK_DOC_KEY = "crypto::healthcheck";
    public static final String IDC_CLIENT_ID = "idc_client_id";
    public static final String IDC_CLIENT_SECRET = "idc_client_secret";
    public static final String CBIS_CLIENT_ID = "clientId";
    public static final String CBIS_CLIENT_SECRET = "clientSecret";
    public static final String MARKET_ASSET_NAME = "market";
    public static final String JOURNEY_ID = "journeyId";


}
