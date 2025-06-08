# DefaultApi

All URIs are relative to *http://localhost:8080*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createTemplate**](DefaultApi.md#createTemplate) | **POST** /api/templates | Create a new template |
| [**deleteTemplate**](DefaultApi.md#deleteTemplate) | **DELETE** /api/templates/{id} | Delete template |
| [**getAllTemplates**](DefaultApi.md#getAllTemplates) | **GET** /api/templates | Get all templates |
| [**getTemplate**](DefaultApi.md#getTemplate) | **GET** /api/templates/search | Search template by name, type, and language |
| [**getTemplateById**](DefaultApi.md#getTemplateById) | **GET** /api/templates/{id} | Get template by ID |
| [**getTemplatesByTag**](DefaultApi.md#getTemplatesByTag) | **GET** /api/templates/tag/{tag} | Get templates by tag |
| [**getTemplatesByTypeAndLanguage**](DefaultApi.md#getTemplatesByTypeAndLanguage) | **GET** /api/templates/type/{type}/language/{language} | Get templates by type and language |
| [**seedTemplates**](DefaultApi.md#seedTemplates) | **POST** /api/templates/seed | Seed default templates |


<a id="createTemplate"></a>
# **createTemplate**
> Template createTemplate(template)

Create a new template

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    Template template = new Template(); // Template | 
    try {
      Template result = apiInstance.createTemplate(template);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#createTemplate");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **template** | [**Template**](Template.md)|  | |

### Return type

[**Template**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Created template |  -  |
| **400** | Invalid input |  -  |

<a id="deleteTemplate"></a>
# **deleteTemplate**
> deleteTemplate(id)

Delete template

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    String id = "id_example"; // String | 
    try {
      apiInstance.deleteTemplate(id);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#deleteTemplate");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **String**|  | |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Template deleted |  -  |

<a id="getAllTemplates"></a>
# **getAllTemplates**
> List&lt;Template&gt; getAllTemplates()

Get all templates

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    try {
      List<Template> result = apiInstance.getAllTemplates();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getAllTemplates");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Template&gt;**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of templates |  -  |

<a id="getTemplate"></a>
# **getTemplate**
> Template getTemplate(name, type, language)

Search template by name, type, and language

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    String name = "name_example"; // String | 
    String type = "type_example"; // String | 
    String language = "language_example"; // String | 
    try {
      Template result = apiInstance.getTemplate(name, type, language);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getTemplate");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **name** | **String**|  | |
| **type** | **String**|  | |
| **language** | **String**|  | |

### Return type

[**Template**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Template found |  -  |
| **404** | Template not found |  -  |

<a id="getTemplateById"></a>
# **getTemplateById**
> Template getTemplateById(id)

Get template by ID

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    String id = "id_example"; // String | 
    try {
      Template result = apiInstance.getTemplateById(id);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getTemplateById");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | **String**|  | |

### Return type

[**Template**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Template found |  -  |
| **404** | Template not found |  -  |

<a id="getTemplatesByTag"></a>
# **getTemplatesByTag**
> List&lt;Template&gt; getTemplatesByTag(tag)

Get templates by tag

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    String tag = "tag_example"; // String | 
    try {
      List<Template> result = apiInstance.getTemplatesByTag(tag);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getTemplatesByTag");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **tag** | **String**|  | |

### Return type

[**List&lt;Template&gt;**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of templates |  -  |

<a id="getTemplatesByTypeAndLanguage"></a>
# **getTemplatesByTypeAndLanguage**
> List&lt;Template&gt; getTemplatesByTypeAndLanguage(type, language)

Get templates by type and language

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    String type = "type_example"; // String | 
    String language = "language_example"; // String | 
    try {
      List<Template> result = apiInstance.getTemplatesByTypeAndLanguage(type, language);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#getTemplatesByTypeAndLanguage");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **type** | **String**|  | |
| **language** | **String**|  | |

### Return type

[**List&lt;Template&gt;**](Template.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | List of templates |  -  |

<a id="seedTemplates"></a>
# **seedTemplates**
> seedTemplates()

Seed default templates

### Example
```java
// Import classes:
import com.batty.forgex.template.client.ApiClient;
import com.batty.forgex.template.client.ApiException;
import com.batty.forgex.template.client.Configuration;
import com.batty.forgex.template.client.models.*;
import com.batty.forgex.template.client.api.DefaultApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8080");

    DefaultApi apiInstance = new DefaultApi(defaultClient);
    try {
      apiInstance.seedTemplates();
    } catch (ApiException e) {
      System.err.println("Exception when calling DefaultApi#seedTemplates");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Templates seeded successfully |  -  |

