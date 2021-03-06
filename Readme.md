# Currency Converter

- Currency Converter is an API that converts between currencies
- It has a single endpoint `/currency/convert` and it takes the following json as a request. 
  A complete request example can be found in Test section below. 
  - `{
    "from": "",
    "to": "",
    "amount": 0
  }`
- Response message includes fields in the request along with `converted` field that represents the converted amount
- Currencies should be provided with 3-letter codes such as EUR, USD, GBP, etc. (https://www.exchangerate-api.com/docs/supported-currencies)
- 2 external exchange rate providers are used to calculate currency conversion.
  
  - Exchange Rate API: https://open.er-api.com/v6/latest/ (previously: https://api.exchangerate-api.com/v4/latest/USD)
  - Currency Layer API: http://apilayer.net/api/live (previously: https://api.exchangeratesapi.io/latest?base=EUR)
- For each conversion request one of the two external providers is chosen randomly. 
  If one provider fails or does not provide requested currency then other external provider is tried.
- If both external providers fail or an internal error occurs, `/currency/convert` endpoint returns an error with an explanation `message`
- Currency Layer API requires subscription and an access token. 
  The url and access token can be modified in `src/main/resources/application.properties` under `api.currencyLayer.url`
- Note that, this free subscription for Currency Layer API has limitations in terms of currency and number of requests. 
  Only USD conversions are available, and you can make 250 requests per month. 
  Please provide a different access token if you reach this limit or want to convert other currencies
- Exchange Rate API's url can be modified via `api.exchangeRate.url` in the `src/main/resources/application.properties` file.
  This provider has also limitations about number of requests and applies rate limiting.
- If you want to run the application on a different port you can edit `server.port=8080` in the `src/main/resources/application.properties` file

## Usage

### Build (Skip to `Run With Docker` if JDK 11 not exists)
- It's a Java project that requires Jdk 11 or higher
  
- Run the following script in root directory in order to build with Maven:

  **`mvn clean install`**
- Run the following if Maven is not installed

  **`./mvnw clean install`**

  or in Windows

  **`./mvnw.cmd clean install`**

### Run
- Once the build is completed, you can run with one of the following commands:

  **`mvn spring-boot:run`**
  
  **`java -jar target/currency-converter-0.0.1.jar`**
  
  **`./mvnw spring-boot:run`**

  **`./mvnw.cmd spring-boot:run`**


### RUN WITH DOCKER
**`docker run -p 8080:8080 alidemirsoy/currency-converter`**

### TEST
You can easily test the application by sending HTTP requests via cURL or any other Http client.
Note that you need to set Content-Type header to application/json
- An example cURL request is as follows:
  - `curl -X POST 'localhost:8080/currency/convert' -H 'Content-Type: application/json' -d '{"from": "EUR","to": "USD","amount": 10 }'`
- You can follow the logs of the application under `logs` directory in the project folder

### FUTURE WORK
### Authentication for External APIs
- Currently, there are 2 external providers for retrieving exchange rates and one of them requires an access token.
This access token never expires and is attached to url
- If these external providers require a session based authentication, 
then there's a need to handle the session tokens as these will expire after a certain period 
  and need to be created programmatically.
- In that case, we would create and cache session tokens until we receive an 'Unauthorized' error from external providers,
and re-create a session on every authentication error (or when cache is empty)

### Caching
- Free usages of external exchange rate providers do not provide instant rates 
  but provide rates that are updated less frequently such as every 24 hours. 
  That means there's an external cache that is invalidated every 24 hours.
  Hence, there's no need to perform frequent network requests for the rates that are recently requested.
- As a performance improvement and overcome usage limitations of these APIs, 
  we can easily cache currency pairs such as EURUSD and store its value for a period of time. 
- Time to Live for the stored values can be configured depending on external providers' refresh rate.
  API responses contain a timestamp that represents the time when exchange rate is stored. 
  The difference between the refresh rate and the passed time until current time would be the TTL.
  
    ` TTL = Refresh Rate of External API - (Current Time - Timestamp)`
- In order to utilize this caching, we would first check if there's a cached currency pair for every request
and use the existing cache or move on with a network request.

- Finally, we can easily cache these values in JVM in a HashMap, but it wouldn't fit for a clustered environment
as we need a central cache that every instance should share.
  Hence, it's recommended to use high available, distributed caching tools such as Redis, Hazelcast, etc.

### External Provider Health
- External providers periodically update their APIs and eventually stop supporting old versions. 
  For instance Exchange Rate API returns a field named `time_eol` that shows expected time of depreciation.
  This should create an alert in this application and necessary changes should be implemented urgently.
- Depending on the subscription to external providers, supported currencies are limited. 
  For instance Currency Layer API only supports USD currency in the free plan. 
  If free plan will remain to be used, then randomized logic to choose external providers should be enhanced to forward
  non-USD currencies directly to the other API.
- A circuit breaker mechanism can be implemented when choosing the external provider. 
  For example when the program hits the limit of max number of requests, or faces a rate limit, 
  then it would be wise to prevent requests to that external provider for a reasonable time. 
